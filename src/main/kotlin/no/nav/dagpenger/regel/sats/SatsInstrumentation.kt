package no.nav.dagpenger.regel.sats

import io.prometheus.client.Counter

class SatsInstrumentation {
    private val regelBrukt =
        Counter
            .build()
            .namespace("dagpenger")
            .name("sats_regel_brukt")
            .help("Antall ganger det er beregnet sats")
            .labelNames(
                "regelIdentifikator",
                "brukt90ProsentRegel",
            ).register()

    fun satsBeregnet(
        regelIdentifikator: String,
        brukt90ProsentRegel: Boolean,
    ) {
        regelBrukt
            .labels(
                regelIdentifikator,
                brukt90ProsentRegel.toString(),
            ).inc()
    }
}
