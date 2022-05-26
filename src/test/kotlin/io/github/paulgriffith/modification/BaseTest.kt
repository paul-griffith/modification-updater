package io.github.paulgriffith.modification

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromStream
import java.security.MessageDigest
import java.util.*

class BaseTest : FunSpec({
    test("View") {
        getResource("/view").calculateSignature() shouldBe "1f2e193ab0b2be15cef750b100bf5c6906b7a92fbb5e7c4f8fb7b68e83b4eb89"
    }

    test("Script") {
        getResource("/script").calculateSignature() shouldBe "7ea951abc0ddc97f549f41a5670b06aa513b30e189050159f40e207cfe502b02"
    }
})

@OptIn(ExperimentalSerializationApi::class)
fun getResource(path: String): ProjectResource {
    val manifest = BaseTest::class.java.getResourceAsStream("$path/resource.json")
        .shouldNotBeNull()
        .use { resourceStream ->
            Json.decodeFromStream<ProjectResource>(resourceStream)
        }
    val data = manifest.files.associateWith { fileName ->
        BaseTest::class.java.getResourceAsStream("$path/$fileName").shouldNotBeNull().readAllBytes()
    }

    return manifest.copy(
        data = data
    )
}

object ApplicationScopeDeserializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("scope", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int {
        return when (decoder.decodeChar()) {
            'A' -> 0
            'G' -> 1
            'D' -> 2
            'C' -> 4
            else -> throw IllegalArgumentException()
        }
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeChar(
            when (value) {
                0 -> 'A'
                1 -> 'G'
                2 -> 'D'
                4 -> 'C'
                else -> throw IllegalArgumentException()
            }
        )
    }
}

@Serializable
data class ProjectResource(
    @Serializable(with = ApplicationScopeDeserializer::class)
    val scope: Int = 0,
    val version: Int = 1,
    val documentation: String? = null,
    val locked: Boolean = false,
    val restricted: Boolean = false,
    val overridable: Boolean = true,
    val files: List<String> = emptyList(),
    val attributes: Map<String, JsonElement>,
    @Transient
    val data: Map<String, ByteArray> = emptyMap(),
)

private fun ProjectResource.calculateSignature(): String {
    val withoutLastModification = copy(
        attributes = attributes - "lastModificationSignature"
    )
    val signature: ByteArray = calculateContentDigest(withoutLastModification)
    return HexFormat.of().formatHex(signature)
}

private fun Int.toByteArray(): ByteArray = byteArrayOf(
    (this shr 24).toByte(),
    (this shr 16).toByte(),
    (this shr 8).toByte(),
    this.toByte()
)

private fun Boolean.toByte(): Byte = (if (this) 1 else 0).toByte()

private fun calculateContentDigest(resource: ProjectResource): ByteArray {
    return MessageDigest.getInstance("SHA-256").apply {
        update(resource.scope.toByteArray())

        resource.documentation?.let { documentation ->
            update(documentation.toByteArray())
        }

        update(resource.version.toByteArray())
        update(resource.locked.toByte())
        update(resource.restricted.toByte())
        update(resource.overridable.toByte())

        resource.files.sorted().forEach { key ->
            update(key.toByteArray())
            val data = resource.data[key] ?: ByteArray(0)
            update(data)
        }

        resource.attributes.entries.sortedBy { it.key }.forEach { (attribute, value) ->
            update(attribute.toByteArray())
            update(value.toString().toByteArray())
        }
    }.digest()
}
