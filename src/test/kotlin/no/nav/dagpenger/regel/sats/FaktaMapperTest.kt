package no.nav.dagpenger.regel.sats

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.sats.FaktaMapper.avkortetGrunnlag
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FaktaMapperTest {
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

    @Test
    fun `mapper avkortet grunnlag riktg`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(avkortetGrunnlag = "123"))
        behovløser.packet!!.avkortetGrunnlag() shouldBe 123.toBigDecimal()

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

    @Test
    fun antallBarn() {
    }

    @Test
    fun beregningsdato() {
    }

    @Test
    fun lærling() {
    }

    @Test
    fun regelverksdato() {
    }

    @Test
    fun grunnlagBeregningsregel() {
    }

    private class OnPacketTestListener(rapidsConnection: RapidsConnection) : River.PacketListener {
        var problems: MessageProblems? = null
        var packet: JsonMessage? = null

        init {
            River(rapidsConnection).apply(
                SatsBehovløser.rapidFilter,
            ).register(this)
        }

        override fun onPacket(packet: JsonMessage, context: MessageContext) {
            this.packet = packet
        }

        override fun onError(problems: MessageProblems, context: MessageContext) {
            this.problems = problems
        }
    }
}
