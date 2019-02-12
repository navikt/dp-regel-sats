package no.nav.dagpenger.regel.sats

import mu.KotlinLogging
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.json.JSONObject
import java.util.Properties

private val LOGGER = KotlinLogging.logger {}

val dagpengerBehovTopic = Topic(
    Topics.DAGPENGER_BEHOV_EVENT.name,
    Serdes.StringSerde(),
    Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
)

class Sats(val env: Environment) : Service() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-sats"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = Sats(Environment())
            service.start()
        }
    }

    override fun setupStreams(): KafkaStreams {
        LOGGER.info { "Initiating start of $SERVICE_APP_ID" }
        return KafkaStreams(buildTopology(), getConfig())
    }

    internal fun buildTopology(): Topology {
        val builder = StreamsBuilder()

        val stream = builder.stream(
            dagpengerBehovTopic.name,
            Consumed.with(dagpengerBehovTopic.keySerde, dagpengerBehovTopic.valueSerde)
        )

        stream
            .peek { key, value -> LOGGER.info("Processing ${value.javaClass} with key $key") }
            .mapValues { value: JSONObject -> SubsumsjonsBehov(value) }
            .filter { _, behov -> shouldBeProcessed(behov) }
            .mapValues(this::addRegelresultat)
            .peek { key, value -> LOGGER.info("Producing ${value.javaClass} with key $key") }
            .mapValues { _, behov -> behov.jsonObject }
            .to(dagpengerBehovTopic.name, Produced.with(dagpengerBehovTopic.keySerde, dagpengerBehovTopic.valueSerde))

        return builder.build()
    }

    override fun getConfig(): Properties {
        val props = streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
        return props
    }

    private fun addRegelresultat(behov: SubsumsjonsBehov): SubsumsjonsBehov {

        val dagpengeGrunnlag = behov.getGrunnlag()
        val antallBarn = behov.getAntallBarn()
        val sats = calculateSats(dagpengeGrunnlag, antallBarn)

        behov.addSatsResultat(
            SatsResultat(
                "123",
                "456",
                "Sats.v1",
                sats
            )
        )

        return behov
    }

    private fun calculateSats(dagpengeGrunnlag: Int, antallBarn: Int): Int {
        return 200
    }
}

fun shouldBeProcessed(behov: SubsumsjonsBehov): Boolean =
    behov.hasAntallBarn() && behov.hasGrunnlag() && behov.needsSatsResultat()
