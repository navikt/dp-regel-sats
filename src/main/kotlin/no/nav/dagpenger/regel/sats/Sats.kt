package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import mu.KotlinLogging
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.River
import no.nav.dagpenger.streams.Topic
import no.nav.dagpenger.streams.Topics
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal

class Sats(private val env: Environment) : River() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-sats"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    val ulidGenerator = ULID()

    companion object {
        val GRUNNLAG = "grunnlag"
        val ANTALL_BARN = "antallBarn"
        val SATS_RESULTAT = "satsResultat"
        val REGELIDENTIFIKATOR = "Sats.v1"
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(SATS_RESULTAT) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG) },
            Predicate { _, packet -> packet.hasField(ANTALL_BARN) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val grunnlag = packet.getIntValue(GRUNNLAG)
        val antallBarn = packet.getIntValue(ANTALL_BARN)
        val dagsats = calculateDagSats(grunnlag)
        val ukesats = calculateUkeSats(dagsats, antallBarn)

        val satsResultat = SatsSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            dagsats,
            ukesats,
            check90procent(grunnlag, ukesats)
        )

        packet.putValue(SATS_RESULTAT, satsResultat.toMap())

        return packet
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

fun main(args: Array<String>) {
    val service = Sats(Environment())
    service.start()
}

