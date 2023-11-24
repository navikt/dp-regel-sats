package no.nav.dagpenger.regel.sats

import mu.KotlinLogging
import no.nav.dagpenger.regel.sats.Config.unleash
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val rapidsConnection =
        RapidApplication.Builder(
            RapidApplication.RapidApplicationConfig.fromEnv(config),
        ).build()

    init {
        rapidsConnection.register(this)
        SatsBehovløser(Sats(), SatsInstrumentation(), rapidsConnection)
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Toggle $BARNETILLEGG_01_01_2024_TOGGLE is enabled = " + unleash.isEnabled(BARNETILLEGG_01_01_2024_TOGGLE) }
        logger.info { "Starter opp dp-regel-sats" }
    }
}
