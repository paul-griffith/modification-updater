@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    application
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.clikt)
    implementation(libs.bundles.kotest)
}

group = "io.github.paulgriffith"
version = "1.0.0-SNAPSHOT"
description = "modification-updater"

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
        archiveBaseName.set("modification-updater")
        archiveClassifier.set("")
        archiveVersion.set("")
        mergeServiceFiles()
    }
    kotlin {
        jvmToolchain(17)
    }
    test {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }
}

application {
    mainClass.set("io.github.paulgriffith.modification.Entrypoint")
}
