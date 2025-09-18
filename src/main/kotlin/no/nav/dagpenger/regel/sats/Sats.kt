package no.nav.dagpenger.regel.sats

import io.github.oshai.kotlinlogging.KotlinLogging
import io.prometheus.client.Counter
import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.KoronaLærlingBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import java.math.BigDecimal
import java.time.LocalDate

val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)

internal const val ANTALL_DESIMALER = 20
const val SATS_BEREGNING_BRUKT_NAVN = "sats_beregning_brukt_total"
private val satsBeregningBrukt =
    Counter.build()
        .name(SATS_BEREGNING_BRUKT_NAVN)
        .labelNames("navn")
        .help("Hvilken beregningsmetode ble brukt for å regne ut sats")
        .register()
private val logger = KotlinLogging.logger { }

class Sats {
    fun forDato(
        beregningsdato: LocalDate,
        regelverksdato: LocalDate,
        lærling: Boolean = false,
    ): Beregning =
        instrument {
            val barnetillegg =
                Barnetillegg.forDato(regelverksdato).also {
                    logger.info { "Fastsatte $it som sats for barnetillegg med dato=$regelverksdato" }
                }
            when {
                KoronaBeregning(barnetillegg).isActive(beregningsdato, regelverksdato) && !lærling ->
                    KoronaBeregning(
                        barnetillegg,
                    )

                KoronaLærlingBeregning().isActive(beregningsdato, regelverksdato) && lærling -> KoronaLærlingBeregning()
                else -> OrdinærBeregning(barnetillegg)
            }
        }
}

fun instrument(handler: () -> Beregning): Beregning =
    handler().also {
        satsBeregningBrukt
            .labels(it.javaClass.simpleName.toString())
            .inc()
    }

data class SatsResult(
    val dagSats: Int,
    val ukeSats: Int,
    val brukt90ProsentRegel: Boolean,
    val beregningsregel: Beregningsregel,
)

enum class Beregningsregel {
    ORDINAER,
    KORONA,
    KORONA_LAERLING,
}
