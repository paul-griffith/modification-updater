package io.github.paulgriffith.modification

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class ModificationTests : FunSpec(
    {
        test("Modification") {
            val initial = deserializeResource("script")
            val updated = initial.update("test", Instant.parse("2022-05-27T16:47:43Z"), true)
            updated.calculateSignature().encodeHex() shouldBe "aa5f6ff86772d32ddad86da18914f835769ccd49e3603e8aea63f5b2fcaf7b08"
        }
    },
)
