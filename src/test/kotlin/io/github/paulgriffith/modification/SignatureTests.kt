package io.github.paulgriffith.modification

import io.github.paulgriffith.modification.LastModification.Companion.lastModification
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.time.Instant

private data class SignatureTest(
    val file: String,
    val signature: String,
    val actor: String,
    val timestamp: String,
) {
    val lastModification = LastModification(actor, Instant.parse(timestamp))
}

class SignatureTests : FunSpec({
    withData(
        nameFn = { (file, signature) -> "Signature for $file should be $signature" },
        SignatureTest(
            file = "view",
            signature = "1f2e193ab0b2be15cef750b100bf5c6906b7a92fbb5e7c4f8fb7b68e83b4eb89",
            actor = "qq",
            timestamp = "2022-05-21T00:01:56Z"
        ),
        SignatureTest(
            file = "script",
            signature = "7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02",
            actor = "qq",
            timestamp = "2022-05-26T23:20:28Z"
        ),
        SignatureTest(
            file = "script2",
            signature = "aa5f6ff86772d32ddad86da18914f835769ccd49e3603e8aea63f5b2fcaf7b08",
            actor = "test",
            timestamp = "2022-05-27T16:47:43Z"
        ),
    ) { case ->
        deserializeResource(case.file).asClue { resource ->
            resource.getSignature() shouldBe case.signature
            resource.manifest.lastModification shouldBe case.lastModification
        }
    }
})
