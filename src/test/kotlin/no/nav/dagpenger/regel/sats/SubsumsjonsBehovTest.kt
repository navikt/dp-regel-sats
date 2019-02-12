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

    @Test
    fun `hasDagpengegrunnlag returns true when grunnlag field exists in json`() {
        val jsonWithGrunnlag = """
            {
                "otherField": "awe",
                "grunnlag": 200
            }
        """.trimIndent()

        assertTrue(jsonToBehov(jsonWithGrunnlag).hasGrunnlag())
    }

    @Test
    fun `hasDagpengegrunnlag returns false when no grunnlag field in json`() {

        val jsonWithoutGrunnlag = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertFalse(jsonToBehov(jsonWithoutGrunnlag).hasGrunnlag())
    }

    @Test
    fun `hasAntallBarn returns true when antallBarn field exists in json`() {
        val jsonWithAntallBarn = """
            {
                "otherField": "awe",
                "antallBarn": 2
            }
        """.trimIndent()


        assertTrue(jsonToBehov(jsonWithAntallBarn).hasAntallBarn())
    }

    @Test
    fun `hasAntallBarn returns false when antallBarn field doesnt exist in json`() {
        val jsonWithoutAntallBarn = """
            {
                "otherField": "awe",
            }
        """.trimIndent()
        assertFalse(jsonToBehov(jsonWithoutAntallBarn).hasAntallBarn())
    }

    @Test
    fun `needsSatsResultat returns true if satsResultat field doesnt exist in json`() {
        val jsonWithoutSatsResultat = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertTrue(jsonToBehov(jsonWithoutSatsResultat).needsSatsResultat())
    }

    @Test
    fun `needsSatsResultat returns false if satsResultat field exists in json`() {
        val jsonWithSatsResultat = """
            {
                "otherField": "awe",
                "satsResultat": {
                    "sporingsId": "aaa",
                    "sats": 2
                }
            }
        """.trimIndent()
        assertFalse(jsonToBehov(jsonWithSatsResultat).needsSatsResultat())
    }

    @Test
    fun `hasSatsResultat returns false if satsResultat field doesnt exist in json`() {
        val jsonWithoutSatsResultat = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertFalse(jsonToBehov(jsonWithoutSatsResultat).hasSatsResultat())
    }

    @Test
    fun `needsSatsResultat returns true if satsResultat field exists in json`() {
        val jsonWithSatsResultat = """
            {
                "otherField": "awe",
                "satsResultat": {
                    "sporingsId": "aaa",
                    "sats": 2
                }
            }
        """.trimIndent()
        assertTrue(jsonToBehov(jsonWithSatsResultat).hasSatsResultat())
    }

    @Test
    fun `getDagpengeGrunnlag returns value from json`() {
        val json = """
            {
                "otherField": "awe",
                "grunnlag": 250
            }
        """.trimIndent()

        assertEquals(250, jsonToBehov(json).getGrunnlag())
    }

    @Test
    fun `getDagpengeGrunnlag throws JSONException if missing grunnlag field`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertFailsWith(JSONException::class) {
            jsonToBehov(json).getGrunnlag()
        }
    }

    @Test
    fun `getAntallBarn returns value from json`() {
        val json = """
            {
                "otherField": "awe",
                "antallBarn": 3
            }
        """.trimIndent()

        assertEquals(3, jsonToBehov(json).getAntallBarn())
    }

    @Test
    fun `getAntallBarn throws JSONException if missing antallBarn field`() {
        val json = """
            {
                "otherField": "awe",
            }
        """.trimIndent()

        assertFailsWith(JSONException::class) {
            jsonToBehov(json).getAntallBarn()
        }
    }

    @Test
    fun `addSatsResultat adds satsResult to json`() {
        val behov = SubsumsjonsBehov.Builder().build()
        behov.addSatsResultat(SatsResultat("aa", "ww", "Sats.v1", 500))

        assertTrue(behov.hasSatsResultat())
        assertEquals(500, behov.jsonObject.getJSONObject("satsResultat").getInt("sats"))
        assertEquals("aa", behov.jsonObject.getJSONObject("satsResultat").getString("sporingsId"))
        assertEquals("ww", behov.jsonObject.getJSONObject("satsResultat").getString("subsumsjonsId"))
        assertEquals("Sats.v1", behov.jsonObject.getJSONObject("satsResultat").getString("regelidentifikator"))
    }
}
