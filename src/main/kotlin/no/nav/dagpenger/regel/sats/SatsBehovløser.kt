package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.regel.sats.FaktaMapper.antallBarn
import no.nav.dagpenger.regel.sats.FaktaMapper.avkortetGrunnlag
import no.nav.dagpenger.regel.sats.FaktaMapper.beregningsdato
import no.nav.dagpenger.regel.sats.FaktaMapper.grunnlagBeregningsregel
import no.nav.dagpenger.regel.sats.FaktaMapper.lærling
import no.nav.dagpenger.regel.sats.FaktaMapper.regelverksdato
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.isMissingOrNull
import java.net.URI

private val logger = KotlinLogging.logger { }

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
            validate {
                it.rejectKey(SATS_RESULTAT)
            }
        }
    }

    init {
        River(rapidsConnection).apply(rapidFilter).register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        withLoggingContext("behovId" to packet["behovId"].asText()) {
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
                    sats.forDato(
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

private fun JsonMessage.harVerdi(field: String) = !this[field].isMissingOrNull()
