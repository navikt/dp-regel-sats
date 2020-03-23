package no.nav.dagpenger.regel.sats.versjoner

import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.regel.sats.Grunnlag
import no.nav.dagpenger.regel.sats.Sats
import no.nav.dagpenger.regel.sats.SatsResult
import java.math.BigDecimal
import java.time.LocalDate

val satsBeregner = Sats()

fun calculateSats(grunnlag: BigDecimal, antallBarn: Int): SatsResult {
    return satsBeregner.beregn(
        grunnlag = Grunnlag(grunnlag, getGrunnbeløpForRegel(Regel.Grunnlag).forDato(LocalDate.now()).verdi),
        antallBarn = antallBarn
    )
}