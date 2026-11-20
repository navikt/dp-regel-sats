plugins {
    id("common")
    application
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    applicationName = "dp-regel-sats"
    mainClass.set("no.nav.dagpenger.regel.sats.ApplicationKt")
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(libs.rapids.and.rivers)
    implementation("no.nav.dagpenger:dp-grunnbelop:20261112.329.123030")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation("io.prometheus:prometheus-metrics-core:1.10.0")

    implementation(libs.konfig)

    implementation("io.getunleash:unleash-client-java:12.3.2")

    implementation(libs.kotlin.logging)

    testImplementation(kotlin("test"))
    testImplementation(libs.rapids.and.rivers.test)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
}
