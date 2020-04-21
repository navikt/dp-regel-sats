package no.nav.dagpenger.regel.sats

import java.math.BigDecimal

class Dekningsgrad(private val dekningsgrad: Double, private val virkedager: Int = 260) {
    fun getDagSatsFaktor() = BigDecimal(dekningsgrad / virkedager / 100)
}
