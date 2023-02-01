package no.nav.dagpenger.regel.sats

import io.mockk.mockk
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.regel.sats.Application.Companion.BEREGNINGSDATO
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.TopologyTestDriver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate
import java.util.Properties
import kotlin.test.assertTrue

class SatsTopologyTest {
    companion object {

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }

        val fakeSatsInstrumentation = mockk<SatsInstrumentation>()
    }

    @Test
    fun `Should ignore packet without grunnlag and antallBarn`() {
        val sats = Application(
            fakeSatsInstrumentation,
            Sats()
        )

        val packet = Packet("{}")

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }
            assertTrue { topologyTestDriver.behovOutputTopic().isEmpty }
        }
    }

    @Test
    fun `Should ignore packet with satsresultat`() {
        val sats = Application(
            fakeSatsInstrumentation,
            Sats()
        )

        val packet = Packet("{}")
        packet.putValue(Application.SATS_RESULTAT, 1)

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }
            assertTrue { topologyTestDriver.behovOutputTopic().isEmpty }
        }
    }

    @Test
    fun `Should add SatsSubsumsjon to packet with grunnlag and antallBarn `() {
        val sats = Application(
            fakeSatsInstrumentation,
            Sats()
        )

        val jsonString =
            """
            {
                grunnlagResultat: {
                    avkortet: 50000
                }
            }
            """.trimIndent()
        val packet = Packet(jsonString)
        packet.putValue(BEREGNINGSDATO, LocalDate.now())
        packet.putValue(Application.ANTALL_BARN, 0)
        packet.putValue("behovId", "ULID")

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }

            val ut = topologyTestDriver.behovOutputTopic().readValue()

            assertTrue("SatsSubsumsjon should be added") { ut.hasField(Application.SATS_RESULTAT) }
            assertEquals(
                Application.REGELIDENTIFIKATOR,
                ut.getMapValue(Application.SATS_RESULTAT)[SatsSubsumsjon.REGELIDENTIFIKATOR]
            )
        }
    }

    @Test
    fun ` Should add problem on failure`() {
        val minsteinntekt = Application(
            fakeSatsInstrumentation,
            Sats()
        )

        val packet = Packet()
        packet.putValue(BEREGNINGSDATO, LocalDate.now())
        packet.putValue("grunnlagResultat", "ERROR")
        packet.putValue("antallBarn", "ERROR")

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            topologyTestDriver.behovInputTopic().also { it.pipeInput(packet) }

            val ut = topologyTestDriver.behovOutputTopic().readValue()
            assert(ut.hasProblem())
            assertEquals(URI("urn:dp:error:regel"), ut.getProblem()!!.type)
            assertEquals(URI("urn:dp:regel:sats"), ut.getProblem()!!.instance)
        }
    }

    private fun TopologyTestDriver.behovInputTopic(): TestInputTopic<String, Packet> =
        this.createInputTopic(
            REGEL_TOPIC.name,
            REGEL_TOPIC.keySerde.serializer(),
            REGEL_TOPIC.valueSerde.serializer()
        )

    private fun TopologyTestDriver.behovOutputTopic(): TestOutputTopic<String, Packet> =
        this.createOutputTopic(
            REGEL_TOPIC.name,
            REGEL_TOPIC.keySerde.deserializer(),
            REGEL_TOPIC.valueSerde.deserializer()
        )
}
