package no.nav.dagpenger.regel.sats.versjoner

import java.time.LocalDate
import java.time.Month

private val fom = LocalDate.of(2020, Month.MARCH, 20)
private val tom = LocalDate.of(2021, Month.OCTOBER, 31)
private val periode = fom..tom

fun LocalDate.erKoronaPeriode() = this in periode
