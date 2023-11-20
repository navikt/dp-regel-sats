package no.nav.dagpenger.regel.sats

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.SATS_RESULTAT
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()

    private val testMessage = mapOf(
        GRUNNLAG_RESULTAT to mapOf(SatsBehovløser.AVKORTET_GRUNNLAG to 300000),
        ANTALL_BARN to "0",
        BEREGNINGSDATO to "2020-04-30",
        BEHOV_ID to "ULID",
    )

    @Test
    fun `Skal behandle pakker med alle required keys uten løsning`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            JsonMessage.newMessage(testMessage).toJson(),
        )
        testListener.onPacketCalled shouldBe true
    }

    @Test
    fun `Skal ikke behanlde pakker som mangler required keys`() {
        val testListener = TestListener(testRapid)

        testRapid.sendTestMessage("{}")
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(ANTALL_BARN) },
        )

        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(BEHOV_ID) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(GRUNNLAG_RESULTAT) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it.remove(BEREGNINGSDATO) },
        )
        testListener.onPacketCalled shouldBe false

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it[GRUNNLAG_RESULTAT] = mapOf("MikkeMus" to 34) },
        )
        testListener.onPacketCalled shouldBe false
    }

    @Test
    fun `Skal ikke behandle pakker som allerede har en løsning`() {
        val testListener = TestListener(testRapid)

        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it[SATS_RESULTAT] = "satsresultat" },
        )
        testListener.onPacketCalled shouldBe false
    }

    private fun Map<String, Any>.muterOgKonverterToJsonString(block: (map: MutableMap<String, Any>) -> Unit): String {
        val mutableMap = this.toMutableMap()
        block.invoke(mutableMap)
        return JsonMessage.newMessage(mutableMap).toJson()
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
