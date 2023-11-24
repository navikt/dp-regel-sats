package no.nav.dagpenger.regel.sats

import mu.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDate

const val BARNETILLEGG_01_01_2024_TOGGLE = "barnetillegg-01-01-2024"

object Barnetillegg {
    private val logger = KotlinLogging.logger { }
    private val satser =
        TemporalCollection<BigDecimal>().apply {
            put(LocalDate.MIN, BigDecimal(17))

            // Defineres her: https://lovdata.no/pro/#document/SF/forskrift/1998-09-16-890/%C2%A77-1
            // Jira: https://jira.adeo.no/browse/ARENA-8016
            put(LocalDate.of(2023, 2, 1), BigDecimal(35))

            if (Config.unleash.isEnabled(BARNETILLEGG_01_01_2024_TOGGLE)) {
                logger.info { "Barnetillegg er 36kr fom 01-01-2024" }
                put(LocalDate.of(2024, 1, 1), BigDecimal(36))
            }
        }

    fun forDato(regelverksdato: LocalDate) = satser.get(regelverksdato)
}
