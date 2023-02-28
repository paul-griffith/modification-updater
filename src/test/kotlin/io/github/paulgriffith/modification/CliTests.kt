package io.github.paulgriffith.modification

import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.PrintHelpMessage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.system.captureStandardErr
import io.kotest.extensions.system.captureStandardOut
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.matchers.string.shouldContain

class CliTests : FunSpec(
    {
        val cli = ModificationUpdater()
        test("No arguments") {
            shouldThrowExactly<PrintHelpMessage> {
                cli.parse(emptyList())
            }
        }

        context("Verify tests") {
            test("Single valid") {
                captureStandardOut {
                    cli.parse(
                        listOf(
                            "verify",
                            "src/test/resources/script",
                        ),
                    )
                }.shouldBeEmpty()
            }

            test("Single invalid") {
                shouldThrow<CliktError> {
                    cli.parse(
                        listOf(
                            "verify",
                            "src/test/resources/invalid",
                        ),
                    )
                }
            }

            test("Multiple") {
                val result = captureStandardErr {
                    try {
                        cli.parse(
                            listOf(
                                "verify",
                                "src/test/resources/script",
                                "src/test/resources/script2",
                                "src/test/resources/view",
                                "src/test/resources/invalid",
                            ),
                        )
                    } catch (e: CliktError) {
                        // expected
                    }
                }.lines().filter(String::isNotBlank)

                result.single().shouldContain("Signature mismatch")
            }
        }

        context("Signature tests") {
            test("Single") {
                captureStandardOut {
                    cli.parse(
                        listOf(
                            "signatures",
                            "src/test/resources/script",
                        ),
                    )
                }.trim() shouldBe "7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02"
            }

            test("Multiple") {
                val result =
                    captureStandardOut {
                        cli.parse(
                            listOf(
                                "signatures",
                                "src/test/resources/script",
                                "src/test/resources/script2",
                                "src/test/resources/view",
                            ),
                        )
                    }.lines().filter(String::isNotBlank)

                val reference = """
                    7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02
                    aa5f6ff86772d32ddad86da18914f835769ccd49e3603e8aea63f5b2fcaf7b08
                    1f2e193ab0b2be15cef750b100bf5c6906b7a92fbb5e7c4f8fb7b68e83b4eb89
                """.trimIndent().lines()

                result shouldBe reference
            }
        }

        test("Single resource update") {
            captureStandardOut {
                cli.parse(
                    listOf(
                        "update",
                        "--dry-run",
                        "--actor",
                        "test",
                        "--timestamp",
                        "2022-05-27T16:47:43Z",
                        "src/test/resources/script",
                    ),
                )
            }.trim() shouldBe
                javaClass.getResourceAsStream(
                    "/script2/resource.json",
                )!!.bufferedReader()
                    .readText()
                    .trim()
        }
    },
)
