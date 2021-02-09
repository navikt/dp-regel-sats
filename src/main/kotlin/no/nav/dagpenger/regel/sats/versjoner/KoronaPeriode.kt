package no.nav.dagpenger.regel.sats.versjoner

import java.time.LocalDate
import java.time.Month

private val fom = LocalDate.of(2020, Month.MARCH, 20)

//Denne datoen skal settes til 30.september 2021 når dette blir vedtatt
private val tom = LocalDate.of(2021, Month.MARCH, 31)
private val periode = fom..tom

fun LocalDate.erKoronaPeriode() = this in periode
