package no.nav.dagpenger.regel.sats

import io.mockk.mockk
import no.nav.dagpenger.events.Packet
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OnPacketTest {
    val service = Application(Configuration(), mockk(relaxed = true), Sats())
    @Test
    fun `bruker lærling-koronaregler når lærling er satt til true på pakka og dato er innenfor koronaperiode`() {

        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.VIRKNINGSTIDSPUNKT, LocalDate.of(2020, 3, 21))
            this.putValue(Application.LÆRLING, true)
        }

        val outPacket = service.onPacket(packet)

        assertEquals("KORONA_LAERLING", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }

    @Test
    fun `bruker ikke lærling-koronaregel når lærling ikke er satt`() {
        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.VIRKNINGSTIDSPUNKT, LocalDate.of(2020, 3, 21))
        }

        val outPacket = service.onPacket(packet)

        assertNotEquals(
            "KORONA_LAERLING",
            outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString()
        )
    }
}
