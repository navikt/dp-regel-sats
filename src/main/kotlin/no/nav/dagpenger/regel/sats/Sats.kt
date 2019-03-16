package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.Service
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.common.serialization.Serdes
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
    val ulidGenerator = ULID()
    val REGELIDENTIFIKATOR = "Sats.v1"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val service = Sats(Environment())
            service.start()
        }
    }

    override fun buildTopology(): Topology {
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

        val grunnlag = behov.getGrunnlag()
        val antallBarn = behov.getAntallBarn()
        val dagsats = calculateDagSats(grunnlag)
        val ukesats = calculateUkeSats(dagsats, antallBarn)

        behov.addSatsResultat(
            SatsResultat(
                ulidGenerator.nextULID(),
                ulidGenerator.nextULID(),
                REGELIDENTIFIKATOR,
                dagsats,
                ukesats,
                check90procent(grunnlag, ukesats)
            )
        )
        return behov
    }
}

fun calculateDagSats(grunnlag: Int): Int {
    return (grunnlag.toDouble() * 0.0024).toInt()
}

fun calculateUkeSats(dagsats: Int, antallBarn: Int): Int {
    val barnetilleggSats = 17
    return ((dagsats * 5) + (barnetilleggSats * antallBarn * 5))
}

fun check90procent(dagpengeGrunnlag: Int, ukesats: Int): Boolean {
    val ukeSatsIÅr = ukesats * 52

    if (ukeSatsIÅr > (dagpengeGrunnlag / 100 * 90)) {
        return true
    }
    return false
}

fun shouldBeProcessed(behov: SubsumsjonsBehov): Boolean =
    behov.hasAntallBarn() && behov.hasGrunnlag() && behov.needsSatsResultat()
