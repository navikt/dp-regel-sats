package no.nav.dagpenger.regel.sats

import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.LocalDate

object Barnetillegg {
    private val logger = KotlinLogging.logger {}

    private val satser =
        TemporalCollection<BigDecimal>().apply {
            // Defineres her: https://lovdata.no/pro/#document/SF/forskrift/1998-09-16-890/%C2%A77-1
            put(LocalDate.MIN, BigDecimal(17))
            put(LocalDate.of(2023, 2, 1), BigDecimal(35))
            put(LocalDate.of(2024, 1, 1), BigDecimal(36))
            put(LocalDate.of(2025, 1, 1), BigDecimal(37))
            if (System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp") {
                val at = LocalDate.of(2025, 11, 13)
                logger.info { "Barnetillegg er 38 kr fra og med $at (i DEV)" }
                put(at, BigDecimal(38))
            }
        }

    fun forDato(regelverksdato: LocalDate) = satser.get(regelverksdato)
}
