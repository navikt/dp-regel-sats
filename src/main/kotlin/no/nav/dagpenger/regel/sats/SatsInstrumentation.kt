package no.nav.dagpenger.regel.sats

import io.prometheus.client.Counter

class SatsInstrumentation {
    private val regelBrukt = Counter.build()
        .namespace("dagpenger")
        .name("sats_regel_brukt")
        .labelNames(
            "regelIdentifikator",
            "brukt90ProsentRegel",
            "antallBarn"
        )
        .register()

    fun satsBeregnet(
        regelIdentifikator: String,
        brukt90ProsentRegel: Boolean,
        antallBarn: Int
    ) {
        regelBrukt.labels(
            regelIdentifikator,
            brukt90ProsentRegel.toString(),
            antallBarn.toString()
        ).inc()
    }
}
