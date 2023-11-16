package no.nav.dagpenger.regel.sats

import io.kotest.matchers.shouldBe
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RapidFilterTest {
    private val testRapid = TestRapid()

    fun testMessage(
        behovId: String = "behovId",
        beregningsdato: LocalDate = LocalDate.MAX,
        antallBarn: Int? = 0,
        avkortetGrunnlag: String,
    ): String {
        return """
          {
            "behovId": "$behovId",
            "beregningsDato": "$beregningsdato",
            "antallBarn": $antallBarn,
            "grunnlagResultat": {
              "avkortet": $avkortetGrunnlag
            }
          } 
        """.trimIndent()
    }

    private val gyldigTestMessage = """
            {
            "behovId": "behovId",
            "beregningsDato": "beregningsdato",
            "antallBarn": 0,
            "grunnlagResultat": {
              "avkortet": 123
              }
            }
        """

    @Test
    fun `Trenger alle required keys`() {
        val testListener = TestListener(testRapid)

        testRapid.sendTestMessage("{}")
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            """{"behovId": "behovId", "beregningsDato": "beregningsdato", 
            "grunnlagResultat": {"avkortet": 123}}""",
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            """{"antallBarn": 0, "beregningsDato": "beregningsdato",
            "grunnlagResultat": {"avkortet": 123}}""",
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage("""{"behovId": "behovId", "beregningsDato": "beregningsdato", "antallBarn": 0}""")
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage("""{"behovId": "behovId","antallBarn": 0, "grunnlagResultat": {"avkortet": 123}}""")
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            """{"behovId": "behovId", "beregningsDato": "beregningsdato", "antallBarn": 0,
            "grunnlagResultat": {"mikkeMus": 123}}""",
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            gyldigTestMessage,
        )
        testListener.onPacketCalled shouldBe true
    }

    private class TestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection).apply(
                SatsBehovløser.rapidFilter,
            ).register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            this.onPacketCalled = true
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
        }
    }
}
