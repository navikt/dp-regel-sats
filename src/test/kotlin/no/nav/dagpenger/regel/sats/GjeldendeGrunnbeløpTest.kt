package no.nav.dagpenger.regel.sats

import io.kotest.matchers.shouldBe
import no.finn.unleash.FakeUnleash
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class GjeldendeGrunnbeløpTest {

    @Test
    fun ` under g-justeringstest`() {

        Application.unleash = FakeUnleash().also { it.enableAll() }
        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp()
        gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()) shouldBe 103500.toBigDecimal()
    }

    @Test
    fun ` ikke g-justeringstest`() {

        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp()
        gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()) shouldBe 101351.toBigDecimal()
    }
}
