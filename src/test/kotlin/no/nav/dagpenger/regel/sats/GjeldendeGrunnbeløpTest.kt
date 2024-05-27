package no.nav.dagpenger.regel.sats

import io.getunleash.FakeUnleash
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class GjeldendeGrunnbeløpTest {
    @Test
    fun ` under g-justeringstest`() {
        val fakeUnleash = FakeUnleash().also { it.enableAll() }
        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp(fakeUnleash)
        gjeldendeGrunnbeløp.grunnbeløp(LocalDate.of(2024, 5, 24)) shouldBe Grunnbeløp.GjusteringsTest.verdi
    }

    @Test
    fun ` ikke g-justeringstest`() {
        val gjeldendeGrunnbeløp = GjeldendeGrunnbeløp()
        gjeldendeGrunnbeløp.grunnbeløp(LocalDate.now()) shouldBe getGrunnbeløpForRegel(Regel.Grunnlag).forDato(LocalDate.now()).verdi
    }
}
