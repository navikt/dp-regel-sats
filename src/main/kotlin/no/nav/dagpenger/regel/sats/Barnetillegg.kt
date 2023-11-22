package no.nav.dagpenger.regel.sats

import java.math.BigDecimal
import java.time.LocalDate

object Barnetillegg {
    private val satser =
        TemporalCollection<BigDecimal>().apply {
            put(LocalDate.MIN, BigDecimal(17))

            // Defineres her: https://lovdata.no/pro/#document/SF/forskrift/1998-09-16-890/%C2%A77-1
            // Jira: https://jira.adeo.no/browse/ARENA-8016
            put(LocalDate.of(2023, 2, 1), BigDecimal(35))
        }

    fun forDato(regelverksdato: LocalDate) = satser.get(regelverksdato)
}
