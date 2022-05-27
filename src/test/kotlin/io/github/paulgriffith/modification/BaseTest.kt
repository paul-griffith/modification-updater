package io.github.paulgriffith.modification

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class BaseTest : FunSpec({
    test("View") {
        getResource("/view").calculateSignature() shouldBe "1f2e193ab0b2be15cef750b100bf5c6906b7a92fbb5e7c4f8fb7b68e83b4eb89"
    }

    test("Script") {
        getResource("/script").calculateSignature() shouldBe "7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02"
    }
})

fun getResource(path: String): ProjectResource {
    val manifest = BaseTest::class.java.getResourceAsStream("$path/resource.json").shouldNotBeNull().toManifest()
    val data: Map<String, DataLoader> = manifest.files.associateWith { fileName ->
        DataLoader { BaseTest::class.java.getResourceAsStream("$path/$fileName").shouldNotBeNull().readAllBytes() }
    }

    return ProjectResource(manifest, data)
}
