package io.github.paulgriffith.modification

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import java.time.Instant

@Serializable
data class ResourceManifest(
    @Serializable(with = ApplicationScopeDeserializer::class)
    val scope: Int = 0,
    val version: Int = 1,
    val documentation: String? = null,
    val locked: Boolean? = null,
    val restricted: Boolean = false,
    val overridable: Boolean = true,
    val files: List<String> = emptyList(),
    val attributes: Map<String, JsonElement>,
) {
    companion object {
        const val FILENAME = "resource.json"
    }
}

@Serializable
data class LastModification(
    val actor: String,
    @Serializable(with = InstantSerializer::class)
    val timestamp: Instant,
) {
    companion object {
        val ResourceManifest.lastModification: LastModification
            get() = JSON.decodeFromJsonElement(attributes.getValue(LAST_MODIFICATION))

        const val LAST_MODIFICATION = "lastModification"
        const val LAST_MODIFICATION_SIGNATURE = "lastModificationSignature"
    }
}

// Lazy supplier of binary data to avoid keeping it in memory
fun interface DataLoader {
    fun getData(): ByteArray
}

data class ProjectResource(
    val manifest: ResourceManifest,
    val data: Map<String, DataLoader>,
)
