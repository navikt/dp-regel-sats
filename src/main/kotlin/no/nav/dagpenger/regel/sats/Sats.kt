package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.River
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal
import java.math.RoundingMode

class Sats(private val env: Environment) : River() {
    override val SERVICE_APP_ID: String = "dagpenger-regel-sats"
    override val HTTP_PORT: Int = env.httpPort ?: super.HTTP_PORT
    val ulidGenerator = ULID()

    companion object {
        val GRUNNLAG_RESULTAT = "grunnlagResultat"
        val AVKORTET_GRUNNLAG = "avkortet"
        val ANTALL_BARN = "antallBarn"
        val SATS_RESULTAT = "satsResultat"
        val REGELIDENTIFIKATOR = "Sats.v1"
    }

    override fun filterPredicates(): List<Predicate<String, Packet>> {
        return listOf(
            Predicate { _, packet -> !packet.hasField(SATS_RESULTAT) },
            Predicate { _, packet -> packet.hasField(GRUNNLAG_RESULTAT) },
            Predicate { _, packet -> packet.hasField(ANTALL_BARN) }
        )
    }

    override fun onPacket(packet: Packet): Packet {
        val avkortetGrunnlag = packet.getMapValue(GRUNNLAG_RESULTAT)[AVKORTET_GRUNNLAG] as Double
        val antallBarn = packet.getIntValue(ANTALL_BARN)

        val (dagSats, ukeSats, brukt90ProsentRegel) = calculateSats(BigDecimal(avkortetGrunnlag), antallBarn)

        val satsResultat = SatsSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            dagSats,
            ukeSats,
            brukt90ProsentRegel
        )

        packet.putValue(SATS_RESULTAT, satsResultat.toMap())

        return packet
    }
}

fun calculateSats(grunnlag: BigDecimal, antallBarn: Int) : Triple<BigDecimal, BigDecimal, Boolean> {
    val dagSats = grunnlag * BigDecimal(0.0024)
    val ukeSats = (dagSats + BigDecimal(antallBarn * 17)) * BigDecimal(5)
    val yearlyDagpenger = ukeSats * BigDecimal(52)

    if (yearlyDagpenger > grunnlag * BigDecimal(0.9)) {
        val redusertDagSats = (grunnlag * BigDecimal(0.9)) / BigDecimal(260)
        val redusertUkeSats = redusertDagSats * BigDecimal(5)
        return Triple(redusertDagSats.setScale(6, RoundingMode.HALF_UP), redusertUkeSats.setScale(6, RoundingMode.HALF_UP), true)
    }

    return Triple(dagSats.setScale(6, RoundingMode.HALF_UP), ukeSats.setScale(6, RoundingMode.HALF_UP), false)
}

fun main(args: Array<String>) {
    val service = Sats(Environment())
    service.start()
}

