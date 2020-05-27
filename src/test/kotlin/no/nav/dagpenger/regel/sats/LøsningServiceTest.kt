package no.nav.dagpenger.regel.sats

import com.fasterxml.jackson.databind.JsonNode
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.dagpenger.regel.sats.LøsningService.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.LøsningService.Companion.AVKORTET_GRUNNLAG
import no.nav.dagpenger.regel.sats.LøsningService.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.LøsningService.Companion.SATS
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LøsningServiceTest {
    private val instrumentation = mockk<SatsInstrumentation>(relaxed = true)
    private val rapid = TestRapid().apply {
        LøsningService(
            this, instrumentation = instrumentation, features = Features(
                emptyMap()
            )
        )
    }

    @BeforeEach
    fun setUp() {
        rapid.reset()
    }

    @Test
    fun `skal fastsette både dag- og ukessats`() {
        rapid.sendTestMessage(
            """
            {
                "@behov": ["$SATS"],
                "@id": "32",
                "aktørId": "123",
                "$AVKORTET_GRUNNLAG": "123000",
                "$ANTALL_BARN": "5",
                "$BEREGNINGSDATO": "2020-01-01"
            }
            """.trimIndent()
        )

        val inspektør = rapid.inspektør

        assertSoftly {
            inspektør.size shouldBeExactly 1
            val message = inspektør.message(0)
            message["@behov"].map(JsonNode::asText) shouldContain SATS
            message["@løsning"].hasNonNull(SATS)
            message["@løsning"][SATS]["dagSats"].asInt() shouldBe 295
            message["@løsning"][SATS]["ukeSats"].asInt() shouldBe 1900
            message["@løsning"][SATS]["benyttet90ProsentRegel"].asBoolean() shouldBe false
        }
    }
}
