import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    java
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.serialization") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.3")
    implementation("com.github.ajalt.clikt", "clikt", "3.4.0")

    val kotestVersion = "5.3.0"
    testImplementation("io.kotest", "kotest-runner-junit5", kotestVersion)
    testImplementation("io.kotest", "kotest-framework-datatest", kotestVersion)
    testImplementation("io.kotest", "kotest-assertions-core", kotestVersion)
    testImplementation("io.kotest", "kotest-property", kotestVersion)
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
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs = listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

application {
    mainClass.set("io.github.paulgriffith.modification.Entrypoint")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
