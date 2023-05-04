import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
    id(Shadow.shadow) version Shadow.version
}

buildscript {
    repositories {
        jcenter()
    }
}

apply {
    plugin(Spotless.spotless)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

application {
    applicationName = "dp-regel-sats"
    mainClass.set("no.nav.dagpenger.regel.sats.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(Dagpenger.Streams)
    implementation(Dagpenger.Events)
    implementation("com.github.navikt:dp-grunnbelop:2023.05.04-15.36.3a722dae0a19")

    implementation(Moshi.moshi)
    implementation(Moshi.moshiAdapters)
    implementation(Moshi.moshiKotlin)
    implementation(Ulid.ulid)

    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.log4j2)

    implementation(Kafka.clients)
    implementation(Kafka.streams)

    implementation(Konfig.konfig)

    // unleash
    implementation("io.getunleash:unleash-client-java:8.0.0")

    implementation(Log4j2.api)
    implementation(Log4j2.core)
    implementation(Log4j2.slf4j)
    implementation(Log4j2.library("layout-template-json"))

    implementation(Kotlin.Logging.kotlinLogging)

    testImplementation(kotlin("test"))
    testImplementation(KoTest.assertions)
    testImplementation(KoTest.runner)
    testImplementation(Junit5.api)
    testImplementation(Junit5.params)
    testRuntimeOnly(Junit5.engine)
    testRuntimeOnly(Junit5.vintageEngine)

    testImplementation(Kafka.streamTestUtils)
    testImplementation(Wiremock.standalone)
    testImplementation(Mockk.mockk)
}

spotless {
    kotlin {
        ktlint(Ktlint.version)
    }
    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.kt*")
        ktlint(Ktlint.version)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showExceptions = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.3.1"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}
