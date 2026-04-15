package no.nav.dagpenger.regel.sats

import io.getunleash.Unleash
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class Grunnlag(
    val grunnlag: BigDecimal,
    private val grunnbeløp: BigDecimal,
) {
    fun getGrunnlagMellom(
        nedreGrense: Double,
        øvreGrense: Double,
    ): BigDecimal {
        val nedreTerskel = grunnbeløp.times(nedreGrense.toBigDecimal())
        val øvreTerskel = grunnbeløp.times(øvreGrense.toBigDecimal())

        return grunnlag
            .min(øvreTerskel)
            .minus(nedreTerskel)
            .max(BigDecimal.ZERO)
            .setScale(0, RoundingMode.HALF_UP)
    }
}

const val GJUSTERING_TEST_TOGGLE = "dp-g-justeringstest"

internal class GjeldendeGrunnbeløp(
    private val unleash: Unleash = Config.unleash,
) {
    internal fun grunnbeløp(dato: LocalDate): BigDecimal =
        when {
            isThisGjusteringTest(dato) -> Grunnbeløp.GjusteringsTest
            else -> getGrunnbeløpForRegel(Regel.Grunnlag).forDato(dato)
        }.verdi

    private fun isThisGjusteringTest(dato: LocalDate): Boolean {
        // Dette er G
        val gVirkning = LocalDate.of(2026, 3, 13)
        val isRegelverksdatoAfterGjustering = dato.isAfter(gVirkning.minusDays(1))
        return unleash.isEnabled(GJUSTERING_TEST_TOGGLE) && isRegelverksdatoAfterGjustering
    }
}
