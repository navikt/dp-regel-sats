package no.nav.dagpenger.regel.sats

import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.TopologyTestDriver
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.json.JSONObject
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.test.assertTrue

class SatsTopologyTest {

    companion object {
        val factory = ConsumerRecordFactory<String, JSONObject>(
            dagpengerBehovTopic.name,
            dagpengerBehovTopic.keySerde.serializer(),
            dagpengerBehovTopic.valueSerde.serializer()
        )

        val config = Properties().apply {
            this[StreamsConfig.APPLICATION_ID_CONFIG] = "test"
            this[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = "dummy:1234"
        }
    }

    @Test
    fun `Should add SatsSubsumsjon to behov with dagpengeGrunnlag and antallBarn `() {
        val datalaster = Sats(
            Environment(
                username = "bogus",
                password = "bogus"
            )
        )

        val behov = SubsumsjonsBehov.Builder().dagpengeGrunnlag(500).antallBarn(1).build()

        TopologyTestDriver(datalaster.buildTopology(), config).use { topologyTestDriver ->
            val inputRecord = factory.create(behov.jsonObject)
            topologyTestDriver.pipeInput(inputRecord)

            val ut = topologyTestDriver.readOutput(
                dagpengerBehovTopic.name,
                dagpengerBehovTopic.keySerde.deserializer(),
                dagpengerBehovTopic.valueSerde.deserializer()
            )

            assertTrue("SatsSubsumsjon should be added") { SubsumsjonsBehov(ut.value()).hasSatsResultat() }
        }
    }
}
