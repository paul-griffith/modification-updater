package io.github.paulgriffith.modification

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class JsonOutputTests : FunSpec(
    {
        withData(
            nameFn = { "Reserializing $it to JSON should have the same representation as the input" },
            "script",
            "view",
        ) { path ->
            val manifest = javaClass.getResourceAsStream("/$path/resource.json").shouldNotBeNull()
                .bufferedReader().readText()
            val parsed = deserializeResource(path)
            JSON.encodeToString(ResourceManifest.serializer(), parsed.manifest) shouldBe manifest
        }
    },
)
