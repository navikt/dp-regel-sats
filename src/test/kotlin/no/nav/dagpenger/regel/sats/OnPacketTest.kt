package no.nav.dagpenger.regel.sats

import no.nav.dagpenger.events.Packet
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OnPacketTest {
    val service = Application(Configuration(), SatsInstrumentation(), Sats())

    @Test
    fun `bruker lærling-koronaregler når lærling er satt til true på pakka og koronatoggle er på og dato er innenfor koronaperiode`() {
        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
            this.putValue(Application.KORONA_TOGGLE, true)
            this.putValue(Application.LÆRLING, true)
        }

        val outPacket = service.onPacket(packet)

        assertEquals("KORONA_LÆRLING", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }

    @Test
    fun `bruker ikke lærling-koronaregel når lærling ikke er satt`() {
        val packet = Packet("""{"grunnlagResultat": { "avkortet": 100000 }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
            this.putValue(Application.KORONA_TOGGLE, true)
        }

        val outPacket = service.onPacket(packet)

        assertNotEquals("KORONA_LÆRLING", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
    }
}

