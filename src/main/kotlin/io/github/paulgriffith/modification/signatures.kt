package io.github.paulgriffith.modification

import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION
import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION_SIGNATURE
import kotlinx.serialization.json.JsonPrimitive
import java.security.MessageDigest
import java.time.Instant

fun ProjectResource.getSignature(): String {
    return calculateContentDigest(
        manifest = manifest.copy(
            attributes = manifest.attributes - LAST_MODIFICATION_SIGNATURE
        ),
        data = data
    ).toHexString()
}

fun ProjectResource.update(actor: String, time: Instant?): ProjectResource {
    val toSign = buildMap {
        putAll(manifest.attributes)
        remove(LAST_MODIFICATION_SIGNATURE)
        put(
            LAST_MODIFICATION,
            JSON.encodeToJsonElement(
                LastModification.serializer(),
                LastModification(actor, time ?: Instant.now())
            )
        )
    }
    val intermediateManifest = manifest.copy(attributes = toSign)
    val newAttributes = toSign.plus(
        LAST_MODIFICATION_SIGNATURE to JsonPrimitive(
            calculateContentDigest(intermediateManifest, data).toHexString()
        )
    )
    return copy(
        manifest = manifest.copy(
            attributes = newAttributes
        )
    )
}

private fun calculateContentDigest(manifest: ResourceManifest, data: Map<String, DataLoader>): ByteArray {
    return MessageDigest.getInstance("SHA-256").apply {
        update(manifest.scope.toByteArray())

        manifest.documentation?.let { documentation ->
            update(documentation.toByteArray())
        }

        update(manifest.version.toByteArray())
        update((manifest.locked ?: false).toByte())
        update(manifest.restricted.toByte())
        update(manifest.overridable.toByte())

        manifest.files.sorted().forEach { key ->
            update(key.toByteArray())
            val dataLoader = data[key] ?: DataLoader { ByteArray(0) }
            update(dataLoader.getData())
        }

        manifest.attributes.entries.sortedBy { it.key }.forEach { (attribute, value) ->
            update(attribute.toByteArray())
            update(value.toString().toByteArray())
        }
    }.digest()
}
