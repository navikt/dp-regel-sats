package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.AVKORTET_GRUNNLAG
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.helse.rapids_rivers.JsonMessage
import java.math.BigDecimal

object FaktaMapper {
    fun JsonMessage.avkortetGrunnlag(): BigDecimal =
        this[GRUNNLAG_RESULTAT][AVKORTET_GRUNNLAG].asText().toBigDecimal()

    fun JsonMessage.antallBarn() {}
    fun JsonMessage.beregningsdato() {}
    fun JsonMessage.lærling() {}
    fun JsonMessage.regelverksdato() {}

    fun JsonMessage.grunnlagBeregningsregel() {
    }
}
