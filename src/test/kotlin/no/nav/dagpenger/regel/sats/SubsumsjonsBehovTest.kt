package no.nav.dagpenger.regel.sats

import org.json.JSONException
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class SubsumsjonsBehovTest {

    fun jsonToBehov(json: String): SubsumsjonsBehov =
        SubsumsjonsBehov(JsonDeserializer().deserialize("", json.toByteArray()) ?: JSONObject())

    val jsonBehovMedGrunnlag = """
            {
                "otherField": "awe",
                "grunnlag": 200
            }
        """.trimIndent()

    val jsonBehovMedAnnetFelt = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

    val jsonWithAntallBarn = """
            {
                "otherField": "awe",
                "antallBarn": 2
            }
        """.trimIndent()

    val jsonWithSatsResultat = """
            {
                "otherField": "awe",
                "satsResultat": {
                    "sporingsId": "aaa",
                    "sats": 2
                }
            }
        """.trimIndent()

    @Test
    fun `hasGrunnlag returns true when grunnlag field exists in json`() {

        assertTrue(jsonToBehov(jsonBehovMedGrunnlag).hasGrunnlag())
        assertFalse(jsonToBehov(jsonBehovMedAnnetFelt).hasGrunnlag())
    }

    @Test
    fun `hasAntallBarn returns true when antallBarn field exists in json`() {

        assertTrue(jsonToBehov(jsonWithAntallBarn).hasAntallBarn())
        assertFalse(jsonToBehov(jsonBehovMedAnnetFelt).hasAntallBarn())
    }

    @Test
    fun `needsSatsResultat returns true if satsResultat field doesnt exist in json`() {

        assertTrue(jsonToBehov(jsonBehovMedAnnetFelt).needsSatsResultat())
        assertFalse(jsonToBehov(jsonWithSatsResultat).needsSatsResultat())
    }

    @Test
    fun `hasSatsResultat returns true if satsResultat field exists in json`() {

        assertTrue(jsonToBehov(jsonWithSatsResultat).hasSatsResultat())
        assertFalse(jsonToBehov(jsonBehovMedAnnetFelt).hasSatsResultat())
    }

    @Test
    fun `getGrunnlag returns value from json`() {

        assertEquals(200, jsonToBehov(jsonBehovMedGrunnlag).getGrunnlag())
        assertFailsWith(JSONException::class) {
            jsonToBehov(jsonBehovMedAnnetFelt).getGrunnlag()
        }
    }

    @Test
    fun `getAntallBarn returns value from json`() {

        assertEquals(2, jsonToBehov(jsonWithAntallBarn).getAntallBarn())
        assertFailsWith(JSONException::class) {
            jsonToBehov(jsonBehovMedAnnetFelt).getAntallBarn()
        }
    }

    @Test
    fun `addSatsResultat adds satsResult to json`() {
        val behov = SubsumsjonsBehov.Builder().build()
        behov.addSatsResultat(
            SatsResultat(
                "aa",
                "ww",
                "Sats.v1",
                500,
                2500,
                false
            ))

        assertTrue(behov.hasSatsResultat())
        assertEquals(500, behov.jsonObject.getJSONObject("satsResultat").getInt("dagsats"))
        assertEquals("aa", behov.jsonObject.getJSONObject("satsResultat").getString("sporingsId"))
        assertEquals("ww", behov.jsonObject.getJSONObject("satsResultat").getString("subsumsjonsId"))
        assertEquals("Sats.v1", behov.jsonObject.getJSONObject("satsResultat").getString("regelIdentifikator"))
    }
}
