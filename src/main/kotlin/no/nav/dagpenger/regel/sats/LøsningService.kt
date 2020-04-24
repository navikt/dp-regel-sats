package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import java.math.BigDecimal
import java.time.LocalDate
import mu.KotlinLogging
import mu.withLoggingContext
import no.finn.unleash.Unleash
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.RapidsConnection.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.River.PacketListener
import no.nav.helse.rapids_rivers.asLocalDate

private val log = KotlinLogging.logger {}

class LøsningService(
    rapidsConnection: RapidsConnection,
    private val unleash: Unleash,
    private val sats: Sats = Sats(unleash),
    private val instrumentation: SatsInstrumentation = SatsInstrumentation()
) : PacketListener {
    private val ulidGenerator = ULID()

    init {
        River(rapidsConnection).apply {
            validate { it.requireAll("@behov", listOf(SATS)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey(ANTALL_BARN) }
            validate { it.requireKey(AVKORTET_GRUNNLAG) }
            validate { it.requireKey(BEREGNINGSDATO) }
            validate { it.interestedIn(LÆRLING) }
        }.register(this)
    }

    companion object {
        const val SATS = "Sats"
        const val ANTALL_BARN = "antallBarn"
        const val AVKORTET_GRUNNLAG = "avkortetGrunnlag"
        const val BEREGNINGSDATO = "beregningsDato"
        const val LÆRLING = "lærling"
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val antallBarn = packet[ANTALL_BARN].asInt()
        val avkortetGrunnlag = BigDecimal(packet[AVKORTET_GRUNNLAG].asInt())
        val beregningsdato = packet[BEREGNINGSDATO].asLocalDate()
        val lærling = packet[LÆRLING].asBoolean(false)

        withLoggingContext(
            "behovId" to packet["@id"].asText(),
            "antallBarn" to antallBarn.toString(),
            "avkortetGrunnlag" to avkortetGrunnlag.toString(),
            "beregningsdato" to beregningsdato.toString(),
            "lærling" to lærling.toString()
        ) {
            try {
                val sats = fastsettSats(
                    antallBarn = antallBarn,
                    avkortetGrunnlag = avkortetGrunnlag,
                    beregningsdato = beregningsdato,
                    lærling = lærling
                )

                packet["@løsning"] = mapOf(
                    SATS to sats
                )

                instrumentation.satsBeregnet(
                    regelIdentifikator = "Sats.v1",
                    brukt90ProsentRegel = sats.benyttet90ProsentRegel
                )

                log.info { "løser behov for ${packet["@id"].asText()}" }

                context.send(packet.toJson())
            } catch (err: Exception) {
                log.error(err) { "feil ved fastsetting av sats: ${err.message} for ${packet["@id"].asText()}" }
            }
        }
    }

    private fun fastsettSats(
        antallBarn: Int,
        avkortetGrunnlag: BigDecimal,
        beregningsdato: LocalDate,
        lærling: Boolean
    ): FastsattSats {
        val grunnlag = Grunnlag(
            grunnlag = avkortetGrunnlag,
            grunnbeløp = getGrunnbeløpForRegel(Regel.Grunnlag).forDato(beregningsdato).verdi
        )
        val satsResult = sats.forDato(beregningsdato, lærling).beregn(
            grunnlag = grunnlag,
            antallBarn = antallBarn
        )

        return FastsattSats(
            dagSats = satsResult.dagSats,
            ukeSats = satsResult.ukeSats,
            benyttet90ProsentRegel = satsResult.brukt90ProsentRegel,
            beregningsregel = satsResult.beregningsregel
        )
    }

    private data class FastsattSats(
        val dagSats: Int,
        val ukeSats: Int,
        val benyttet90ProsentRegel: Boolean,
        val beregningsregel: Beregningsregel
    )
}
