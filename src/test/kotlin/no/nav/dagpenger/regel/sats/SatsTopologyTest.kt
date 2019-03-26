package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.Topics.DAGPENGER_BEHOV_PACKET_EVENT
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SatsTopologyTest {

    companion object {

        val factory = ConsumerRecordFactory<String, Packet>(
            Topics.DAGPENGER_BEHOV_PACKET_EVENT.name,
            Topics.DAGPENGER_BEHOV_PACKET_EVENT.keySerde.serializer(),
            Topics.DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun `Should ignore packet without grunnlag and antallBarn`() {
        val sats = Sats(
            Environment(
                username = "bogus",
                password = "bogus"
            )
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
        val sats = Sats(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val packet = Packet("{}")
        packet.putValue(Sats.SATS_RESULTAT, 1)

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
        val sats = Sats(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val jsonString = """
            {
                grunnlagResultat: {
                    avkortet: 50000.0
                }
            }
        """.trimIndent()
        val packet = Packet(jsonString)
        packet.putValue(Sats.ANTALL_BARN, 0)

        TopologyTestDriver(sats.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(packet)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                DAGPENGER_BEHOV_PACKET_EVENT.name,
                DAGPENGER_BEHOV_PACKET_EVENT.keySerde.deserializer(),
                DAGPENGER_BEHOV_PACKET_EVENT.valueSerde.deserializer()
            )

            assertTrue("SatsSubsumsjon should be added") { ut.value().hasField(Sats.SATS_RESULTAT) }
            assertEquals(Sats.REGELIDENTIFIKATOR, ut.value().getMapValue(Sats.SATS_RESULTAT)[SatsSubsumsjon.REGELIDENTIFIKATOR])
        }
    }
}
