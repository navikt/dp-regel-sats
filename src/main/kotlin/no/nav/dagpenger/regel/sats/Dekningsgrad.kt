package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.math.RoundingMode

class Dekningsgrad(
    private val dekningsgrad: Double,
    private val virkedager: Int = 260,
) {
    fun getDagSatsFaktor(): BigDecimal =
        BigDecimal
            .valueOf(dekningsgrad)
            .divide(virkedager.toBigDecimal(), 4, RoundingMode.HALF_UP)
            .divide(100.toBigDecimal(), 4, RoundingMode.HALF_UP)
}
