package no.nav.dagpenger.regel.sats

data class SatsSubsumsjon(
    val sporingsId: String,
    val subsumsjonsId: String,
    val regelidentifikator: String,
    val dagSats: Int,
    val ukeSats: Int,
    val benyttet90ProsentRegel: Boolean,
    val beregningsregel: Beregningsregel,
) {

    companion object {
        val SPORINGSID = "sporingsId"
        val SUBSUMSJONSID = "subsumsjonsId"
        val REGELIDENTIFIKATOR = "regelIdentifikator"
        val DAGSATS = "dagsats"
        val UKESATS = "ukesats"
        val BENYTTET_90PROSENT_REGEL = "benyttet90ProsentRegel"
        val BEREGNINGSREGEL = "beregningsregel"
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            SPORINGSID to sporingsId,
            SUBSUMSJONSID to subsumsjonsId,
            REGELIDENTIFIKATOR to regelidentifikator,
            DAGSATS to dagSats,
            UKESATS to ukeSats,
            BENYTTET_90PROSENT_REGEL to benyttet90ProsentRegel,
            BEREGNINGSREGEL to beregningsregel,
        )
    }
}
