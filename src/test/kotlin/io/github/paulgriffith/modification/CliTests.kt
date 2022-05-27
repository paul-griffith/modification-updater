package io.github.paulgriffith.modification

import com.github.ajalt.clikt.core.PrintHelpMessage
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.captureStandardOut
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class CliTests : FunSpec({
    val cli = ModificationUpdater()
    test("No arguments") {
        shouldThrowExactly<PrintHelpMessage> {
            cli.parse(emptyList())
        }
    }

    test("Signature check") {
        captureStandardOut {
            cli.parse(listOf("-s", "src/test/resources/script"))
        } shouldBe "7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02\n"
    }

    test("Multiple resources output signatures") {
        captureStandardOut {
            cli.parse(listOf("-s", "src/test/resources/script", "src/test/resources/script2", "src/test/resources/view"))
        } shouldBe """
            7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02
            aa5f6ff86772d32ddad86da18914f835769ccd49e3603e8aea63f5b2fcaf7b08
            1f2e193ab0b2be15cef750b100bf5c6906b7a92fbb5e7c4f8fb7b68e83b4eb89
        """.trimIndent() + "\n"
    }

    test("Single resource update") {
        captureStandardOut {
            cli.parse(listOf("src/test/resources/script", "--actor", "test", "--timestamp", "2022-05-27T16:47:43Z"))
        } shouldBe
            javaClass.getResourceAsStream("/script2/resource.json").shouldNotBeNull().bufferedReader().readText() + "\n"
    }
})
