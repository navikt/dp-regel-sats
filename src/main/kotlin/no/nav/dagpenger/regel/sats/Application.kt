package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import io.getunleash.Unleash
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.streams.HealthCheck
import no.nav.dagpenger.streams.KafkaAivenCredentials
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.streamConfigAiven
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal
import java.net.URI
import java.util.Properties

private val logger = KotlinLogging.logger {}
private val config = Configuration()

class Application(
    private val instrumentation: SatsInstrumentation,
    private val sats: Sats,
    public override val healthChecks: List<HealthCheck> = listOf(),
    topic: Topic<String, Packet> = config.kafka.regelTopic,
) : River(topic) {
    @Suppress("ktlint:standard:property-naming")
    override val SERVICE_APP_ID: String = config.application.id

    @Suppress("ktlint:standard:property-naming")
    override val HTTP_PORT: Int = config.application.httpPort
    private val ulidGenerator = ULID()

    companion object {
        const val REGELVERKSDATO = "regelverksdato"
        const val GRUNNLAG_RESULTAT = "grunnlagResultat"
        const val AVKORTET_GRUNNLAG = "avkortet"
        const val GRUNNLAG_BEREGNINGSREGEL = "beregningsregel"
        const val GRUNNLAG_BEREGNINGSREGEL_VERNEPLIKT = "Verneplikt"
        const val ANTALL_BARN = "antallBarn"
        const val SATS_RESULTAT = "satsResultat"
        const val REGELIDENTIFIKATOR = "Sats.v1"
        const val BEREGNINGSDATO = "beregningsDato"
        const val LÆRLING = "lærling"
        var unleash: Unleash = config.unleash
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(SATS_RESULTAT) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG_RESULTAT) },
            Predicate { _, packet -> packet.hasField(ANTALL_BARN) },
            Predicate { _, packet -> packet.hasField(BEREGNINGSDATO) },
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val behovId = packet.getStringValue("behovId")

        withLoggingContext(
            "behovId" to behovId,
        ) {
            val avkortetGrunnlag = BigDecimal(packet.getMapValue(GRUNNLAG_RESULTAT)[AVKORTET_GRUNNLAG].toString())
            val grunnlagBeregningsregel = packet.getMapValue(GRUNNLAG_RESULTAT)[GRUNNLAG_BEREGNINGSREGEL].toString()
            val antallBarn = packet.getIntValue(ANTALL_BARN)
            val beregningsdato = packet.getLocalDate(BEREGNINGSDATO)
            val erLærling = packet.getNullableBoolean(LÆRLING) == true
            val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp()
            val regelverksdato = packet.getNullableLocalDate(REGELVERKSDATO) ?: beregningsdato
            val grunnbeløp = when (grunnlagBeregningsregel) {
                GRUNNLAG_BEREGNINGSREGEL_VERNEPLIKT -> gjeldendeGrunnbeløp.grunnbeløp(regelverksdato)
                else -> gjeldendeGrunnbeløp.grunnbeløp(beregningsdato)
            }
            val grunnlag = Grunnlag(
                grunnlag = avkortetGrunnlag,
                grunnbeløp = grunnbeløp,
            )
            val satsResult = sats.forDato(
                beregningsdato = beregningsdato,
                regelverksdato = regelverksdato,
                lærling = erLærling,
            ).beregn(grunnlag, antallBarn)

            logger.info { "Beregnet sats for [beregningsdato=$beregningsdato, regelverksdato=$regelverksdato, lærling=$erLærling, antallBarn=$antallBarn] [DagSats=${satsResult.dagSats}, UkeSats=${satsResult.ukeSats}] via regel=${satsResult.beregningsregel}" }
            val satsResultat = SatsSubsumsjon(
                ulidGenerator.nextULID(),
                ulidGenerator.nextULID(),
                REGELIDENTIFIKATOR,
                satsResult.dagSats,
                satsResult.ukeSats,
                satsResult.brukt90ProsentRegel,
                satsResult.beregningsregel,
            )

            packet.putValue(SATS_RESULTAT, satsResultat.toMap())

            instrumentation.satsBeregnet(
                regelIdentifikator = REGELIDENTIFIKATOR,
                brukt90ProsentRegel = satsResult.brukt90ProsentRegel,
            )

            return packet
        }
    }

    override fun getConfig(): Properties {
        return streamConfigAiven(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = config.kafka.aivenBrokers,
            aivenCredentials = KafkaAivenCredentials(),
        )
    }

    override fun onFailure(packet: Packet, error: Throwable?): Packet {
        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av satsregel",
                instance = URI("urn:dp:regel:sats"),
            ),
        )
        return packet
    }
}

fun main() {
    logger.info { "Unleash strategy(dp-g-justeringstest) er  ${Configuration().unleash.isEnabled("dp-g-justeringstest")} " }
    val instrumentation = SatsInstrumentation()
    val sats = Sats()
    Application(
        instrumentation = instrumentation,
        sats = sats,
        healthChecks = emptyList(),
    ).start()
}
