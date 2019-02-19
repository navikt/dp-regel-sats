package no.nav.dagpenger.regel.sats

data class Environment(
    val username: String = getEnvVar("SRVDP_REGEL_SATS_USERNAME"),
    val password: String = getEnvVar("SRVDP_REGEL_SATS_PASSWORD"),
    val bootstrapServersUrl: String = getEnvVar("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
    val httpPort: Int? = 8097
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
    System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
