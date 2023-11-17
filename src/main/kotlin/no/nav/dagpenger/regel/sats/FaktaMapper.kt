package no.nav.dagpenger.regel.sats

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.AVKORTET_GRUNNLAG
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.isMissingOrNull
import java.math.BigDecimal

object FaktaMapper {
    fun JsonMessage.avkortetGrunnlag(): BigDecimal =
        this[GRUNNLAG_RESULTAT][AVKORTET_GRUNNLAG].asText().toBigDecimal()

    fun JsonMessage.antallBarn() = this[ANTALL_BARN].asText().toInt()
    fun JsonMessage.beregningsdato() = this[BEREGNINGSDATO].asLocalDate()
    fun JsonMessage.lærling() = when (this.harVerdi(SatsBehovløser.LÆRLING)) {
        true -> this[SatsBehovløser.LÆRLING].asBooleanStrict()
        false -> false
    }

    fun JsonMessage.regelverksdato() = when (this.harVerdi(SatsBehovløser.REGELVERKSDATO)) {
        true -> this[SatsBehovløser.REGELVERKSDATO].asLocalDate()
        false -> this.beregningsdato()
    }

    fun JsonMessage.grunnlagBeregningsregel() =
        when (this.harVerdi("$GRUNNLAG_RESULTAT.${SatsBehovløser.GRUNNLAG_BEREGNINGSREGEL}")) {
            true -> this[GRUNNLAG_RESULTAT][SatsBehovløser.GRUNNLAG_BEREGNINGSREGEL].asText()
            false -> null
        }

    private fun JsonNode.asBooleanStrict(): Boolean =
        asText().toBooleanStrict()

    private fun JsonMessage.harVerdi(field: String) = !this[field].isMissingOrNull()
}
