package no.nav.dagpenger.regel.sats

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class GjeldendeGrunnbeløpTest {

    @Test
    fun ` under g-justeringstest`() {

        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(Features(mapOf("gjustering" to true)))
        gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()) shouldBe 103500.toBigDecimal()
    }

    @Test
    fun ` ikke g-justeringstest`() {

        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(Features(mapOf("gjustering" to false)))
        gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()) shouldBe 101351.toBigDecimal()
    }
}
