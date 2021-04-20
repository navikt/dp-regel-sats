package no.nav.dagpenger.regel.sats

import io.mockk.mockk
import no.nav.dagpenger.events.Packet
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OnPacketTest {
    val service = Application(mockk(relaxed = true), Sats())
    @Test
    fun `bruker lærling-koronaregler når lærling er satt til true på pakka og dato er innenfor koronaperiode`() {

        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
            this.putValue(Application.LÆRLING, true)
        }

        val outPacket = service.onPacket(packet)

        assertEquals("KORONA_LAERLING", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }

    @Test
    fun `bruker ikke lærling-koronaregel når lærling ikke er satt`() {
        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
        }

        val outPacket = service.onPacket(packet)

        assertNotEquals(
            "KORONA_LAERLING",
            outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString()
        )
    }

    @Test
    fun `bruker koronaregler når regelverksdato er fom 1 februar 2021, og beregningsdato er før 20 mars 2020`() {

        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 19))
            this.putValue(Application.REGELVERKSDATO, LocalDate.of(2021, 9, 20))
        }

        val outPacket = service.onPacket(packet)

        assertEquals("KORONA", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }

    @Test
    fun `G-en blir bestemt utifra regelverksdato`() {

        val packet = Packet("""{"grunnlagResultat": { "avkortet": 304053 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 4, 30))
            this.putValue(Application.REGELVERKSDATO, LocalDate.of(2020, 5, 1))
        }

        val outPacket = service.onPacket(packet)

        assertEquals("936", outPacket.getMapValue(Application.SATS_RESULTAT)["dagsats"].toString())
        assertEquals("KORONA", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }

    @Test
    fun `Regelverksdato settes til beregningsdato hvis regelverksdato er null`() {

        val packet = Packet("""{"grunnlagResultat": { "avkortet": 304053 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 4, 30))
        }

        val outPacket = service.onPacket(packet)

        assertEquals("933", outPacket.getMapValue(Application.SATS_RESULTAT)["dagsats"].toString())
        assertEquals("KORONA", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }
}
