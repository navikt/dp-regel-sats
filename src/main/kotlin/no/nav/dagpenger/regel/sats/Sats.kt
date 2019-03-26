package no.nav.dagpenger.regel.sats

import de.huxhorn.sulky.ulid.ULID
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.streams.River
import org.apache.kafka.streams.kstream.Predicate
import java.math.BigDecimal

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
        val dagsats = calculateDagSats(avkortetGrunnlag.toBigDecimal())
        val ukesats = calculateUkeSats(dagsats, antallBarn)

        val satsResultat = SatsSubsumsjon(
            ulidGenerator.nextULID(),
            ulidGenerator.nextULID(),
            REGELIDENTIFIKATOR,
            dagsats,
            ukesats,
            ukeSatsMoreThan90PercentOfGrunnlag(avkortetGrunnlag.toBigDecimal(), ukesats)
        )

        packet.putValue(SATS_RESULTAT, satsResultat.toMap())

        return packet
    }
}

fun calculateDagSats(grunnlag: BigDecimal) = grunnlag * 0.0024.toBigDecimal()

fun calculateUkeSats(dagsats: BigDecimal, antallBarn: Int): BigDecimal {
    val barnetilleggSats = 17
    return (dagsats * 5.toBigDecimal()) + (barnetilleggSats * antallBarn * 5).toBigDecimal()
}

fun ukeSatsMoreThan90PercentOfGrunnlag(dagpengeGrunnlag: BigDecimal, ukesats: BigDecimal): Boolean {
    val ukeSatsIÅr = ukesats * 52.toBigDecimal()

    return ukeSatsIÅr > (dagpengeGrunnlag / 100.toBigDecimal() * 90.toBigDecimal())
}

fun main(args: Array<String>) {
    val service = Sats(Environment())
    service.start()
}

