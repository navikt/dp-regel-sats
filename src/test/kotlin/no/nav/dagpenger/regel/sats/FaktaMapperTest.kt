package no.nav.dagpenger.regel.sats

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.regel.sats.FaktaMapper.antallBarn
import no.nav.dagpenger.regel.sats.FaktaMapper.avkortetGrunnlag
import no.nav.dagpenger.regel.sats.FaktaMapper.beregningsdato
import no.nav.dagpenger.regel.sats.FaktaMapper.grunnlagBeregningsregel
import no.nav.dagpenger.regel.sats.FaktaMapper.lærling
import no.nav.dagpenger.regel.sats.FaktaMapper.regelverksdato
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.ANTALL_BARN
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.AVKORTET_GRUNNLAG
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEHOV_ID
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.BEREGNINGSDATO
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_BEREGNINGSREGEL
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.GRUNNLAG_RESULTAT
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.LÆRLING
import no.nav.dagpenger.regel.sats.SatsBehovløser.Companion.REGELVERKSDATO
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class FaktaMapperTest {
    private val testRapid = TestRapid()

    private companion object {
        private fun testMessage(
            behovId: String = "behovId",
            beregningsdato: LocalDate = LocalDate.MAX,
            antallBarn: Number = 0,
            avkortetGrunnlag: Number = 100000,
            lærling: Boolean? = null,
            regelverksdato: LocalDate? = null,
            grunnlagBeregningsregel: String? = null,
        ): String {
            val testMap =
                mutableMapOf(
                    BEHOV_ID to behovId,
                    BEREGNINGSDATO to beregningsdato,
                    ANTALL_BARN to antallBarn,
                    GRUNNLAG_RESULTAT to mapOf(AVKORTET_GRUNNLAG to avkortetGrunnlag),
                )
            lærling?.let {
                testMap[LÆRLING] = lærling
            }
            regelverksdato?.let {
                testMap[REGELVERKSDATO] = regelverksdato
            }
            grunnlagBeregningsregel?.let {
                testMap[GRUNNLAG_RESULTAT] =
                    mapOf(
                        AVKORTET_GRUNNLAG to avkortetGrunnlag,
                        GRUNNLAG_BEREGNINGSREGEL to grunnlagBeregningsregel,
                    )
            }

            return JsonMessage.newMessage(testMap).toJson()
        }
    }

    @Test
    fun `mapper avkortet grunnlag riktg`() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(avkortetGrunnlag = 200000))
        behovløser.packet.avkortetGrunnlag() shouldBe BigDecimal(200000)
    }

    @Test
    fun antallBarn() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(antallBarn = 3))
        behovløser.packet.antallBarn() shouldBe 3

        testRapid.sendTestMessage(testMessage(antallBarn = 0.0))
        behovløser.packet.antallBarn() shouldBe 0

        testRapid.sendTestMessage(testMessage(antallBarn = 1.2))
        behovløser.packet.antallBarn() shouldBe 1
    }

    @Test
    fun beregningsdato() {
        val behovløser = OnPacketTestListener(testRapid)

        testRapid.sendTestMessage(testMessage(beregningsdato = LocalDate.MIN))
        behovløser.packet.beregningsdato() shouldBe LocalDate.MIN
    }

    @Test
    fun lærling() {
        val behovløser = OnPacketTestListener(testRapid)
        testRapid.sendTestMessage(testMessage(lærling = true))
        behovløser.packet.lærling() shouldBe true

        testRapid.sendTestMessage(testMessage(lærling = false))
        behovløser.packet.lærling() shouldBe false

        testRapid.sendTestMessage(testMessage(lærling = null))
        behovløser.packet.lærling() shouldBe false
    }

    @Test
    fun regelverksdato() {
        val behovløser = OnPacketTestListener(testRapid)
        testRapid.sendTestMessage(testMessage(regelverksdato = LocalDate.MIN))
        behovløser.packet.regelverksdato() shouldBe LocalDate.MIN

        testRapid.sendTestMessage(
            testMessage(
                regelverksdato = null,
                beregningsdato = LocalDate.MIN,
            ),
        )
        behovløser.packet.regelverksdato() shouldBe LocalDate.MIN
    }

    @Test
    fun grunnlagBeregningsregel() {
        val behovløser = OnPacketTestListener(testRapid)
        testRapid.sendTestMessage(testMessage(grunnlagBeregningsregel = "Langbein"))
        behovløser.packet.grunnlagBeregningsregel() shouldBe "Langbein"

        testRapid.sendTestMessage(testMessage(grunnlagBeregningsregel = null))
        behovløser.packet.grunnlagBeregningsregel() shouldBe null
    }

    private class OnPacketTestListener(
        rapidsConnection: RapidsConnection,
    ) : River.PacketListener {
        var problems: MessageProblems? = null
        lateinit var packet: JsonMessage

        init {
            River(rapidsConnection)
                .apply(
                    SatsBehovløser.rapidFilter,
                ).register(this)
        }

        override fun onPacket(
            packet: JsonMessage,
            context: MessageContext,
        ) {
            this.packet = packet
        }

        override fun onError(
            problems: MessageProblems,
            context: MessageContext,
        ) {
            this.problems = problems
        }
    }
}
