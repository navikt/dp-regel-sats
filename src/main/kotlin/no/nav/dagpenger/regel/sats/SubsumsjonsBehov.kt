package no.nav.dagpenger.regel.sats

import org.json.JSONObject

data class SubsumsjonsBehov(val jsonObject: JSONObject) {

    fun hasDagpengegrunnlag() = jsonObject.has("dagpengeGrunnlag")

    fun hasAntallBarn() = jsonObject.has("antallBarn")

    fun needsSatsSubsumsjon() = !hasSatsSubsumsjon()

    fun getDagpengeGrunnlag() = jsonObject.getInt("dagpengeGrunnlag")

    fun getAntallBarn() = jsonObject.getInt("antallBarn")

    private fun hasSatsSubsumsjon() = jsonObject.has("satsSubsumsjon")

    fun addSatsSubsumsjon(satsSubsumsjon: SatsSubsumsjon) = jsonObject.put("satsSubsumsjon", satsSubsumsjon.build())

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

        fun satsSubsumsjon(satsSubsumsjon: SatsSubsumsjon): Builder {
            jsonObject.put("satsSubsumsjon", satsSubsumsjon.build())
            return this
        }

        fun build(): SubsumsjonsBehov = SubsumsjonsBehov(jsonObject)
    }
}

data class SatsSubsumsjon(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val dagpengeGrunnlag: Int,
    val antallBarn: Int,
    val sats: Int
) {
    fun build(): JSONObject = JSONObject()
        .put("sporingsId", sporingsId)
        .put("subsumsjonsId", subsumsjonsId)
        .put("regelidentifikator", regelidentifikator)
        .put("dagpengeGrunnlag", dagpengeGrunnlag)
        .put("antallBarn", antallBarn)
        .put("sats", sats)
}
