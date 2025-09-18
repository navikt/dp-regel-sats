package no.nav.dagpenger.regel.sats

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import de.huxhorn.sulky.ulid.ULID
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.regel.sats.FaktaMapper.antallBarn
import no.nav.dagpenger.regel.sats.FaktaMapper.avkortetGrunnlag
import no.nav.dagpenger.regel.sats.FaktaMapper.beregningsdato
import no.nav.dagpenger.regel.sats.FaktaMapper.grunnlagBeregningsregel
import no.nav.dagpenger.regel.sats.FaktaMapper.lærling
import no.nav.dagpenger.regel.sats.FaktaMapper.regelverksdato
import java.net.URI

private val logger = KotlinLogging.logger { }
private val sikkerLogg = KotlinLogging.logger("tjenestekall")

class SatsBehovløser(
    private val sats: Sats,
    private val instrumentation: SatsInstrumentation,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
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
        const val BEHOV_ID = "behovId"
        const val PROBLEM = "system_problem"
        val rapidFilter: River.() -> Unit = {
            validate { it.requireKey(BEHOV_ID) }
            validate {
                it.requireKey(
                    GRUNNLAG_RESULTAT,
                    "$GRUNNLAG_RESULTAT.$AVKORTET_GRUNNLAG",
                    ANTALL_BARN,
                    BEREGNINGSDATO,
                )
            }
            validate {
                it.interestedIn(
                    LÆRLING,
                    REGELVERKSDATO,
                    "$GRUNNLAG_RESULTAT.$GRUNNLAG_BEREGNINGSREGEL",
                )
            }
            validate { it.forbid(SATS_RESULTAT) }
            validate { it.forbid(PROBLEM) }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        withLoggingContext("behovId" to packet["behovId"].asText()) {
            sikkerLogg.info { "Mottok behov: ${packet.toJson()}" }
            try {
                val avkortetGrunnlag = packet.avkortetGrunnlag()
                val antallBarn = packet.antallBarn()
                val beregningsdato = packet.beregningsdato()
                val erLærling = packet.lærling()
                val regelverksdato = packet.regelverksdato()
                val grunnlagBeregningsregel = packet.grunnlagBeregningsregel()
                val grunnbeløp =
                    when (grunnlagBeregningsregel) {
                        GRUNNLAG_BEREGNINGSREGEL_VERNEPLIKT -> GjeldendeGrunnbeløp().grunnbeløp(regelverksdato)
                        else -> GjeldendeGrunnbeløp().grunnbeløp(beregningsdato)
                    }

                val grunnlag =
                    Grunnlag(
                        grunnlag = avkortetGrunnlag,
                        grunnbeløp = grunnbeløp,
                    )
                val satsResult =
                    sats
                        .forDato(
                            beregningsdato = beregningsdato,
                            regelverksdato = regelverksdato,
                            lærling = erLærling,
                        ).beregn(grunnlag, antallBarn)

                logger.info {
                    "Beregnet sats for [beregningsdato=$beregningsdato, regelverksdato=$regelverksdato," +
                        " lærling=$erLærling, antallBarn=$antallBarn] [DagSats=${satsResult.dagSats}, " +
                        "UkeSats=${satsResult.ukeSats}] via regel=${satsResult.beregningsregel}"
                }
                val satsResultat =
                    SatsSubsumsjon(
                        ulidGenerator.nextULID(),
                        ulidGenerator.nextULID(),
                        REGELIDENTIFIKATOR,
                        satsResult.dagSats,
                        satsResult.ukeSats,
                        satsResult.brukt90ProsentRegel,
                        satsResult.beregningsregel,
                    )

                packet[SATS_RESULTAT] = satsResultat.toMap()

                instrumentation.satsBeregnet(
                    regelIdentifikator = REGELIDENTIFIKATOR,
                    brukt90ProsentRegel = satsResult.brukt90ProsentRegel,
                )

                context.publish(packet.toJson())
                sikkerLogg.info { "Løste behov for satsberegning: $satsResultat" }
            } catch (e: Exception) {
                val problem =
                    Problem(
                        type = URI("urn:dp:error:regel"),
                        title = "Ukjent feil ved bruk av satsregel",
                        instance = URI("urn:dp:regel:sats"),
                    )
                packet[PROBLEM] = problem.toMap
                context.publish(packet.toJson())
                throw e
            }
        }
    }
}
