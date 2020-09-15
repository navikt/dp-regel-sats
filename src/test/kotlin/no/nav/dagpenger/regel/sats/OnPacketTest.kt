package no.nav.dagpenger.regel.sats

import io.mockk.mockk
import no.finn.unleash.FakeUnleash
import no.nav.dagpenger.events.Packet
import no.nav.dagpenger.grunnbelop.Grunnbeløp
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OnPacketTest {

    val unleash = FakeUnleash()
    val service = Application(
        configuration = Configuration(),
        instrumentation = mockk(relaxed = true),
        unleash = unleash,
        sats = Sats()
    )

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
    fun `G-justering av verneplikt når flagget er på `() {

        withGjustering {
            val unleash = FakeUnleash()
            val service = Application(
                configuration = Configuration(),
                instrumentation = mockk(relaxed = true),
                unleash = unleash,
                sats = Sats()
            )
            unleash.enable("dp-regel-sats.VernepliktGjustering")
            val inntekt3G = Grunnbeløp.GjusteringsTest.verdi * 3.toBigDecimal()
            val packet = Packet("""{"grunnlagResultat": { "avkortet": $inntekt3G }}""").apply {
                this.putValue(Application.ANTALL_BARN, 0)
                this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
                this.putValue(Application.VERNEPLIKT, "true")
            }

            val outPacket = service.onPacket(packet)

            assertEquals("KORONA", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
            assertEquals(936, outPacket.getMapValue(Application.SATS_RESULTAT)["dagsats"].toString().toInt())
        }
    }

    @Test
    fun `G-justering av verneplikt når flagget er av `() {

        withGjustering {
            val unleash = FakeUnleash()
            val service = Application(
                configuration = Configuration(),
                instrumentation = mockk(relaxed = true),
                unleash = unleash,
                sats = Sats()
            )
            unleash.disable("dp-regel-sats.VernepliktGjustering")
            val inntekt3G = Grunnbeløp.GjusteringsTest.verdi * 3.toBigDecimal()
            val packet = Packet("""{"grunnlagResultat": { "avkortet": $inntekt3G }}""").apply {
                this.putValue(Application.ANTALL_BARN, 0)
                this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
                this.putValue(Application.VERNEPLIKT, "true")
            }

            val outPacket = service.onPacket(packet)

            assertEquals("KORONA", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
            assertEquals(933, outPacket.getMapValue(Application.SATS_RESULTAT)["dagsats"].toString().toInt())
        }
    }

    @Test
    fun `G-justering av verneplikt når gjusteringtest er av  `() {

        val unleash = FakeUnleash()
        val service = Application(
            configuration = Configuration(),
            instrumentation = mockk(relaxed = true),
            unleash = unleash,
            sats = Sats()
        )
        unleash.enable("dp-regel-sats.VernepliktGjustering")
        val inntekt3G = Grunnbeløp.GjusteringsTest.verdi * 3.toBigDecimal()
        val packet = Packet("""{"grunnlagResultat": { "avkortet": $inntekt3G }}""").apply {
            this.putValue(Application.ANTALL_BARN, 0)
            this.putValue(Application.BEREGNINGSDATO, LocalDate.of(2020, 3, 21))
            this.putValue(Application.VERNEPLIKT, "true")
        }

        val outPacket = service.onPacket(packet)

        assertEquals("KORONA", outPacket.getMapValue(Application.SATS_RESULTAT)["beregningsregel"].toString())
        assertEquals(933, outPacket.getMapValue(Application.SATS_RESULTAT)["dagsats"].toString().toInt())
    }

    fun withGjustering(test: () -> Unit) {
        try {
            System.setProperty("feature.gjustering", "true")
            test()
        } finally {
            System.clearProperty("feature.gjustering")
        }
    }
}
