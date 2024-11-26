package no.nav.dagpenger.regel.sats

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.MeterRegistry
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.PROBLEM
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.SATS_RESULTAT
import org.junit.jupiter.api.Test

class RapidFilterTest {
    private val testRapid = TestRapid()

    private val testMessage =
        mapOf(
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

    @Test
    fun `Skal ikke behandle pakker med problem`() {
        val testListener = TestListener(testRapid)
        testRapid.sendTestMessage(
            testMessage.muterOgKonverterToJsonString { it[PROBLEM] = "problem" },
        )
        testListener.onPacketCalled shouldBe false
    }

    private fun Map<String, Any>.muterOgKonverterToJsonString(block: (map: MutableMap<String, Any>) -> Unit): String {
        val mutableMap = this.toMutableMap()
        block.invoke(mutableMap)
        return JsonMessage.newMessage(mutableMap).toJson()
    }

    private class TestListener(
        rapidsConnection: RapidsConnection,
    ) : River.PacketListener {
        var onPacketCalled = false

        init {
            River(rapidsConnection)
                .apply(
                    SatsBehovløser.rapidFilter,
                ).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
            metadata: MessageMetadata,
            meterRegistry: MeterRegistry,
        ) {
            this.onPacketCalled = true
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
            metadata: MessageMetadata,
        ) {
        }
    }
}
