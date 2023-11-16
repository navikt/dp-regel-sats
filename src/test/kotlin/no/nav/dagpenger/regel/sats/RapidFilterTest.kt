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
            gyldigTestMessage,
        )
        testListener.onPacketCalled shouldBe true

//        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$FANGST_OG_FISKE":false}""")
//        mapToFaktaFrom(behovløser.packet!!).fangstOgFiske shouldBe false
//
//        testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}"}""")
//        mapToFaktaFrom(behovløser.packet!!).fangstOgFiske shouldBe false
//
//        shouldThrow<IllegalArgumentException> {
//            testRapid.sendTestMessage("""{"$BEREGNINGSDATO":"${LocalDate.now()}","$FANGST_OG_FISKE":1}""")
//            mapToFaktaFrom(behovløser.packet!!)
//        }
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
