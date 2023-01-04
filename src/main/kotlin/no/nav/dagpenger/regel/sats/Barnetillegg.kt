package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.time.LocalDate

object Barnetillegg {
    fun forDato(beregningsdato: LocalDate, regelverksdato: LocalDate): BigDecimal {
        return BigDecimal(17)
    }
}
