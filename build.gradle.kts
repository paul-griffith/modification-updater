plugins {
    application
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.4.1")
    implementation("com.github.ajalt.clikt", "clikt", "3.5.1")

    val kotestVersion = "5.5.5"
    testImplementation("io.kotest", "kotest-runner-junit5", kotestVersion)
    testImplementation("io.kotest", "kotest-framework-datatest", kotestVersion)
    testImplementation("io.kotest", "kotest-assertions-core", kotestVersion)
}

group = "io.github.paulgriffith"
version = "1.0-SNAPSHOT"
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
