package no.nav.dagpenger.regel.sats

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    fun hasDagpengegrunnlag() = jsonObject.has("dagpengeGrunnlag")

    fun hasAntallBarn() = jsonObject.has("antallBarn")

    fun needsSatsResultat() = !hasSatsResultat()

    fun getDagpengeGrunnlag() = jsonObject.getInt("dagpengeGrunnlag")

    fun getAntallBarn() = jsonObject.getInt("antallBarn")

    fun hasSatsResultat() = jsonObject.has("satsResultat")

    fun addSatsResultat(satsResultat: SatsResultat) = jsonObject.put("satsResultat", satsResultat.build())

    class Builder {

        val jsonObject = JSONObject()

        fun dagpengeGrunnlag(dagpengeGrunnlag: Int): Builder {
            jsonObject.put("dagpengeGrunnlag", dagpengeGrunnlag)
            return this
        }

        fun antallBarn(antallBarn: Int): Builder {
            jsonObject.put("antallBarn", antallBarn)
            return this
        }

        fun satsResultat(satsResultat: SatsResultat): Builder {
            jsonObject.put("satsResultat", satsResultat.build())
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
