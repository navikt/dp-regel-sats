package no.nav.dagpenger.regel.sats

import io.mockk.mockk
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.regel.sats.Application.Companion.BEREGNINGSDATO
import no.nav.dagpenger.streams.Topics.DAGPENGER_BEHOV_PACKET_EVENT
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.LocalDate
import java.util.Properties
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SatsTopologyTest {
    companion object {
        val factory = ConsumerRecordFactory<String, Packet>(
            DAGPENGER_BEHOV_PACKET_EVENT.name,
            DAGPENGER_BEHOV_PACKET_EVENT.keySerde.serializer(),
            DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }

        val fakeSatsInstrumentation = mockk<SatsInstrumentation>()
    }

    @Test
    fun `Should ignore packet without grunnlag and antallBarn`() {
        val sats = Application(
            Configuration(),
            fakeSatsInstrumentation,
            Sats()
        )

        val packet = Packet("{}")

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )
            assertNull(ut)
        }
    }

    @Test
    fun `Should ignore packet with satsresultat`() {
        val sats = Application(
            Configuration(),
            fakeSatsInstrumentation,
            Sats()
        )

        val packet = Packet("{}")
        packet.putValue(Application.SATS_RESULTAT, 1)

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )
            assertNull(ut)
        }
    }

    @Test
    fun `Should add SatsSubsumsjon to packet with grunnlag and antallBarn `() {
        val sats = Application(
            Configuration(),
            fakeSatsInstrumentation,
            Sats()
        )

        val jsonString = """
            {
                grunnlagResultat: {
                    avkortet: 50000
                }
            }
        """.trimIndent()
        val packet = Packet(jsonString)
        packet.putValue(BEREGNINGSDATO, LocalDate.now())
        packet.putValue(Application.ANTALL_BARN, 0)

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue("SatsSubsumsjon should be added") { ut.value().hasField(Application.SATS_RESULTAT) }
            assertEquals(
                Application.REGELIDENTIFIKATOR,
                ut.value().getMapValue(Application.SATS_RESULTAT)[SatsSubsumsjon.REGELIDENTIFIKATOR]
            )
        }
    }

    @Test
    fun ` Should add problem on failure`() {
        val minsteinntekt = Application(
            Configuration(),
            fakeSatsInstrumentation,
            Sats()
        )

        val packet = Packet()
        packet.putValue(BEREGNINGSDATO, LocalDate.now())
        packet.putValue("grunnlagResultat", "ERROR")
        packet.putValue("antallBarn", "ERROR")

        TopologyTestDriver(minsteinntekt.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assert(ut.value().hasProblem())
            assertEquals(URI("urn:dp:error:regel"), ut.value().getProblem()!!.type)
            assertEquals(URI("urn:dp:regel:sats"), ut.value().getProblem()!!.instance)
        }
    }
}
