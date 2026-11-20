package no.nav.dagpenger.regel.sats

import io.prometheus.metrics.core.metrics.Counter

class SatsInstrumentation {
    private val regelBrukt =
        Counter
            .builder()
            .name("dagpenger_sats_regel_brukt")
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
            .labelValues(
                regelIdentifikator,
                brukt90ProsentRegel.toString(),
            ).inc()
    }
}
