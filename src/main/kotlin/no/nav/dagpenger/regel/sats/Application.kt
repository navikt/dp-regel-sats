package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.finn.unleash.Unleash
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

private val sikkerlogg = KotlinLogging.logger("tjenestekall")
private val config = Configuration()

class Application(
    private val instrumentation: SatsInstrumentation,
    private val sats: Sats,
    public override val healthChecks: List<HealthCheck> = listOf(),
    topic: Topic<String, Packet> = config.kafka.regelTopic
) : River(topic) {
    override val SERVICE_APP_ID: String = config.application.id
    override val HTTP_PORT: Int = config.application.httpPort
    private val ulidGenerator = ULID()

    companion object {
        const val REGELVERKSDATO = "regelverksdato"
        const val GRUNNLAG_RESULTAT = "grunnlagResultat"
        const val AVKORTET_GRUNNLAG = "avkortet"
        const val ANTALL_BARN = "antallBarn"
        const val SATS_RESULTAT = "satsResultat"
        const val REGELIDENTIFIKATOR = "Sats.v1"
        const val BEREGNINGSDATO = "beregningsDato"
        const val LÆRLING = "lærling"
        var unleash: Unleash = setupUnleash(config.application.unleashUrl)
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(SATS_RESULTAT) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG_RESULTAT) },
            Predicate { _, packet -> packet.hasField(ANTALL_BARN) },
            Predicate { _, packet -> packet.hasField(BEREGNINGSDATO) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        sikkerlogg.info("Mottok packet: ${packet.toJson()}")

        val avkortetGrunnlag = BigDecimal(packet.getMapValue(GRUNNLAG_RESULTAT)[AVKORTET_GRUNNLAG].toString())
        val antallBarn = packet.getIntValue(ANTALL_BARN)
        val beregningsdato = packet.getLocalDate(BEREGNINGSDATO)
        val erLærling = packet.getNullableBoolean(LÆRLING) == true
        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp()
        val regelverksdato = packet.getNullableLocalDate(REGELVERKSDATO) ?: beregningsdato

        val grunnlag = Grunnlag(
            grunnlag = avkortetGrunnlag,
            grunnbeløp = gjeldendeGrunnbeløp.grunnbeløp(regelverksdato)
        )

        val satsResult = sats.forDato(
            beregningsdato = beregningsdato,
            lærling = erLærling,
            regelverksdato = regelverksdato
        ).beregn(grunnlag, antallBarn)

        val satsResultat = SatsSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            satsResult.dagSats,
            satsResult.ukeSats,
            satsResult.brukt90ProsentRegel,
            satsResult.beregningsregel
        )

        packet.putValue(SATS_RESULTAT, satsResultat.toMap())

        instrumentation.satsBeregnet(
            regelIdentifikator = REGELIDENTIFIKATOR,
            brukt90ProsentRegel = satsResult.brukt90ProsentRegel
        )
        sikkerlogg.info("Løst behov: ${packet.toJson()}")
        return packet
    }

    override fun getConfig(): Properties {
        return streamConfigAiven(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = config.kafka.aivenBrokers,
            aivenCredentials = KafkaAivenCredentials()
        )
    }

    override fun onFailure(packet: Packet, error: Throwable?): Packet {
        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av satsregel",
                instance = URI("urn:dp:regel:sats")
            )
        )
        return packet
    }
}

fun main(args: Array<String>) {
    val instrumentation = SatsInstrumentation()
    val sats = Sats()

    Application(
        instrumentation = instrumentation,
        sats = sats,
        healthChecks = emptyList()
    ).start()
}
