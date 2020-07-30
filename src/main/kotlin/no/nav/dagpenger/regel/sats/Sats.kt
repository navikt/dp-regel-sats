package no.nav.dagpenger.regel.sats

import io.prometheus.client.Counter
import no.nav.dagpenger.regel.sats.versjoner.KoronaBeregning
import no.nav.dagpenger.regel.sats.versjoner.KoronaLærlingBeregning
import no.nav.dagpenger.regel.sats.versjoner.OrdinærBeregning
import java.math.BigDecimal
import java.time.LocalDate

val dagerPerUke = BigDecimal(5)
val ukerPerÅr = BigDecimal(52)

internal const val antallDesimaler: Int = 20

const val satsBeregningBruktName = "sats_beregning_brukt"
private val satsBeregningBrukt = Counter.build()
    .name(satsBeregningBruktName)
    .labelNames("navn")
    .help("Hvilken beregningsmetode ble brukt for å regne ut sats")
    .register()

class Sats() {
    private val ordinærBeregning = OrdinærBeregning()
    private val koronaBeregning = KoronaBeregning()
    private val koronaLærlingBeregning = KoronaLærlingBeregning()

    fun forDato(beregningsdato: LocalDate, lærling: Boolean = false): Beregning =
        instrument {
            return@instrument when {
                koronaBeregning.isActive(beregningsdato) && !lærling -> koronaBeregning
                koronaBeregning.isActive(beregningsdato) && lærling -> koronaLærlingBeregning
                else -> ordinærBeregning
            }
        }
}

fun instrument(handler: () -> Beregning): Beregning =
    handler().also {
        satsBeregningBrukt
            .labels(it.javaClass.simpleName.toString())
            .inc()
    }

interface Beregning {
    fun isActive(beregningstidspunkt: LocalDate): Boolean
    fun beregn(grunnlag: Grunnlag, antallBarn: Int): SatsResult
}

data class SatsResult(
    val dagSats: Int,
    val ukeSats: Int,
    val brukt90ProsentRegel: Boolean,
    val beregningsregel: Beregningsregel
)

enum class Beregningsregel {
    ORDINAER,
    KORONA,
    KORONA_LAERLING
}
