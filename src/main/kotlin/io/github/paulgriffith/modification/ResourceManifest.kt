package io.github.paulgriffith.modification

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ResourceManifest(
    @Serializable(with = ApplicationScopeDeserializer::class)
    val scope: Int = 0,
    val version: Int = 1,
    val documentation: String? = null,
    val locked: Boolean = false,
    val restricted: Boolean = false,
    val overridable: Boolean = true,
    val files: List<String> = emptyList(),
    val attributes: Map<String, JsonElement>,
)

fun interface DataLoader {
    fun getData(): ByteArray
}

data class ProjectResource(
    val manifest: ResourceManifest,
    val data: Map<String, DataLoader>,
)
