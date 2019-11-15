package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.events.Problem
import no.nav.dagpenger.streams.KafkaCredential
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.Topics
import no.nav.dagpenger.streams.streamConfig
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal
import java.net.URI
import java.util.Properties

class Sats(private val env: Environment, private val instrumentation: SatsInstrumentation) : River(Topics.DAGPENGER_BEHOV_PACKET_EVENT) {
    override val SERVICE_APP_ID: String = "dagpenger-regel-sats"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    private val ulidGenerator = ULID()

    companion object {
        const val GRUNNLAG_RESULTAT = "grunnlagResultat"
        const val AVKORTET_GRUNNLAG = "avkortet"
        const val ANTALL_BARN = "antallBarn"
        const val SATS_RESULTAT = "satsResultat"
        const val REGELIDENTIFIKATOR = "Sats.v1"
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(SATS_RESULTAT) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG_RESULTAT) },
            Predicate { _, packet -> packet.hasField(ANTALL_BARN) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val avkortetGrunnlag = BigDecimal(packet.getMapValue(GRUNNLAG_RESULTAT)[AVKORTET_GRUNNLAG].toString())
        val antallBarn = packet.getIntValue(ANTALL_BARN)

        val satsResult = calculateSats(avkortetGrunnlag, antallBarn)

        val satsResultat = SatsSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            satsResult.dagSats,
            satsResult.ukeSats,
            satsResult.brukt90ProsentRegel
        )

        packet.putValue(SATS_RESULTAT, satsResultat.toMap())

        instrumentation.satsBeregnet(
            regelIdentifikator = REGELIDENTIFIKATOR,
            brukt90ProsentRegel = satsResult.brukt90ProsentRegel
        )

        return packet
    }

    override fun getConfig(): Properties {
        return streamConfig(
            appId = SERVICE_APP_ID,
            bootStapServerUrl = env.bootstrapServersUrl,
            credential = KafkaCredential(env.username, env.password)
        )
    }

    override fun onFailure(packet: Packet, error: Throwable?): Packet {
        packet.addProblem(
            Problem(
                type = URI("urn:dp:error:regel"),
                title = "Ukjent feil ved bruk av satsregel",
                instance = URI("urn:dp:regel:sats")
            )
        )
        return packet
    }
}

fun main(args: Array<String>) {
    val service = Sats(Environment(), SatsInstrumentation())
    service.start()
}
