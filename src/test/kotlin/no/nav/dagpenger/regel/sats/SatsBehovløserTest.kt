package no.nav.dagpenger.regel.sats

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.dagpenger.regel.sats.Beregningsregel.KORONA
import no.nav.dagpenger.regel.sats.Beregningsregel.KORONA_LAERLING
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.AVKORTET_GRUNNLAG
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_BEREGNINGSREGEL
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_BEREGNINGSREGEL_VERNEPLIKT
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.PROBLEM
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.REGELVERKSDATO
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.SATS_RESULTAT
import no.nav.dagpenger.regel.sats.SatsSubsumsjon.Companion.DAGSATS
import no.nav.dagpenger.regel.sats.SatsSubsumsjon.Companion.UKESATS
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SatsBehovløserTest {
    private val testrapid = TestRapid()
    private val sats = Sats()

    init {
        SatsBehovløser(sats, instrumentation = mockk(relaxed = true), testrapid)
    }

    @Test
    fun `Bruker lærling-koronaregler når lærling er satt til true og dato er innenfor koronaperiode`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to mapOf("avkortet" to 100000.0),
                    ANTALL_BARN to 0,
                    BEREGNINGSDATO to "2020-03-21",
                    LÆRLING to true,
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText() shouldBe KORONA_LAERLING.name
    }

    @Test
    fun `Bruker ikke lærling-koronaregel når lærling ikke er satt`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to mapOf("avkortet" to 100000.0),
                    ANTALL_BARN to 0,
                    BEREGNINGSDATO to "2020-03-21",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText() shouldNotBe KORONA_LAERLING.name
    }

    @Test
    fun `Bruker koronaregler når regelverksdato er fom 1 februar 2021, og beregningsdato er før 20 mars 2020`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to mapOf("avkortet" to 100000.0),
                    ANTALL_BARN to 0,
                    BEREGNINGSDATO to "2020-03-19",
                    REGELVERKSDATO to "2021-02-01",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText() shouldBe KORONA.name
        println(resultatPacket)
    }

    @Test
    fun `Barnetillegg skal være 35 når regelverksdato er 31-12-2023 eller tidligere`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to mapOf("avkortet" to 333333.0),
                    ANTALL_BARN to 1,
                    BEREGNINGSDATO to "2023-07-07",
                    REGELVERKSDATO to "2023-12-31",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][UKESATS].asInt() / 5 - resultatPacket[SATS_RESULTAT][DAGSATS].asInt() shouldBe 35
    }

    @Test
    fun `Barnetillegg skal være 36 når regelverksdato er 1-1-2024 eller senere`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to mapOf("avkortet" to 333333.0),
                    ANTALL_BARN to 1,
                    BEREGNINGSDATO to "2023-07-07",
                    REGELVERKSDATO to "2024-01-01",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][UKESATS].asInt() / 5 - resultatPacket[SATS_RESULTAT][DAGSATS].asInt() shouldBe 36
    }

    @Test
    fun `G-en blir som hovedregel bestemt utifra beregningsdato`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to mapOf("avkortet" to 304053.0),
                    ANTALL_BARN to 0,
                    BEREGNINGSDATO to "2020-05-01",
                    REGELVERKSDATO to "2020-04-30",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText() shouldBe KORONA.name
        resultatPacket[SATS_RESULTAT][DAGSATS].asInt() shouldBe 936
    }

    @Test
    fun `G-en blir bestemt av regelverksdato når beregningsregel for grunnlag er Verneplikt`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to
                        mapOf(
                            "avkortet" to 304053.0,
                            GRUNNLAG_BEREGNINGSREGEL to GRUNNLAG_BEREGNINGSREGEL_VERNEPLIKT,
                        ),
                    ANTALL_BARN to 0,
                    BEREGNINGSDATO to "2020-04-30",
                    REGELVERKSDATO to "2020-05-01",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText() shouldBe KORONA.name
        resultatPacket[SATS_RESULTAT][DAGSATS].asInt() shouldBe 936
    }

    @Test
    fun `Regelverksdato settes til beregningsdato hvis regelverksdato er null`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    "grunnlagResultat" to
                        mapOf(
                            "avkortet" to 304053.0,
                            GRUNNLAG_BEREGNINGSREGEL to GRUNNLAG_BEREGNINGSREGEL_VERNEPLIKT,
                        ),
                    ANTALL_BARN to 0,
                    BEREGNINGSDATO to "2020-04-30",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        testrapid.sendTestMessage(testMessage)

        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[SATS_RESULTAT][GRUNNLAG_BEREGNINGSREGEL].asText() shouldBe KORONA.name
        resultatPacket[SATS_RESULTAT][DAGSATS].asInt() shouldBe 933
    }

    @Test
    fun `Problem publiseres og appen kræsjer når feil oppstår i applikasjonen`() {
        val testMessage =
            JsonMessage.newMessage(
                map =
                mapOf(
                    GRUNNLAG_RESULTAT to mapOf(AVKORTET_GRUNNLAG to "error"),
                    ANTALL_BARN to "error",
                    BEREGNINGSDATO to "2020-04-30",
                    BEHOV_ID to "ULID",
                ),
            ).toJson()

        assertThrows<Exception> {
            testrapid.sendTestMessage(testMessage)
        }
        val resultatPacket = testrapid.inspektør.message(0)
        resultatPacket[PROBLEM] shouldNotBe null
        resultatPacket[PROBLEM]["status"].asInt() shouldBe 500
    }
}
