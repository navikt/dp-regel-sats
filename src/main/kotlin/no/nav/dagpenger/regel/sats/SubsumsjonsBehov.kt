package no.nav.dagpenger.regel.sats

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    companion object {
        val GRUNNLAG = "grunnlag"
        val ANTALL_BARN = "antallBarn"
        val SATS_RESULTAT = "satsResultat"
    }

    fun hasGrunnlag() = jsonObject.has(GRUNNLAG)

    fun hasAntallBarn() = jsonObject.has(ANTALL_BARN)

    fun needsSatsResultat() = !hasSatsResultat()

    fun getGrunnlag() = jsonObject.getInt(GRUNNLAG)

    fun getAntallBarn() = jsonObject.getInt(ANTALL_BARN)

    fun hasSatsResultat() = jsonObject.has(SATS_RESULTAT)

    fun addSatsResultat(satsResultat: SatsResultat) = jsonObject.put(SATS_RESULTAT, satsResultat.build())

    class Builder {

        val jsonObject = JSONObject()

        fun dagpengeGrunnlag(dagpengeGrunnlag: Int): Builder {
            jsonObject.put(GRUNNLAG, dagpengeGrunnlag)
            return this
        }

        fun antallBarn(antallBarn: Int): Builder {
            jsonObject.put(ANTALL_BARN, antallBarn)
            return this
        }

        fun satsResultat(satsResultat: SatsResultat): Builder {
            jsonObject.put(SATS_RESULTAT, satsResultat.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}

data class SatsResultat(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val sats: Int
) {
    fun build(): JSONObject = JSONObject()
        .put("sporingsId", sporingsId)
        .put("subsumsjonsId", subsumsjonsId)
        .put("regelidentifikator", regelidentifikator)
        .put("sats", sats)
}
