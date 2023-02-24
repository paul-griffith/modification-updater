package io.github.paulgriffith.modification

import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION
import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION_SIGNATURE
import io.github.paulgriffith.modification.LastModification.Companion.lastModification
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import java.security.MessageDigest
import java.time.Instant

fun ProjectResource.getSignature(): String {
    return calculateContentHex(
        manifest = manifest.copy(
            attributes = manifest.attributes - LAST_MODIFICATION_SIGNATURE,
        ),
        data = data,
    )
}

private fun ResourceManifest.updateAttributes(
    actor: String?,
    time: Instant?,
    signature: String?,
): ResourceManifest {
    return copy(
        attributes = buildMap {
            putAll(attributes)
            if (signature == null) {
                remove(LAST_MODIFICATION_SIGNATURE)
            } else {
                put(LAST_MODIFICATION_SIGNATURE, JsonPrimitive(signature))
            }

            val newMod = LastModification(
                actor = actor ?: lastModification.actor,
                timestamp = time ?: lastModification.timestamp,
            )
            put(LAST_MODIFICATION, JSON.encodeToJsonElement(newMod))
        },
    )
}

fun ProjectResource.update(
    actor: String,
    time: Instant,
    force: Boolean,
): ProjectResource {
    val currentSignature = getSignature()
    val lastSignature = manifest.attributes[LAST_MODIFICATION_SIGNATURE].toString()

    if (lastSignature == currentSignature && !force) {
        return ProjectResource(manifest, data)
    }

    val tempManifest = manifest.updateAttributes(actor, time, null)
    val tempResource = ProjectResource(tempManifest, data)
    val updatedManifest = tempManifest.updateAttributes(
        actor,
        time,
        tempResource.getSignature(),
    )

    return ProjectResource(updatedManifest, data)
}

private fun calculateContentHex(
    manifest: ResourceManifest,
    data: Map<String, DataLoader>,
): String {
    return calculateContentDigest(
        manifest,
        data,
    ).toHexString()
}

private fun calculateContentDigest(
    manifest: ResourceManifest,
    data: Map<String, DataLoader>,
): ByteArray {
    return MessageDigest.getInstance("SHA-256").apply {
        update(manifest.scope.toByteArray())

        manifest.documentation?.let { documentation ->
            update(documentation.toByteArray())
        }

        update(manifest.version.toByteArray())
        update((manifest.locked ?: false).toByte())
        update(manifest.restricted.toByte())
        update(manifest.overridable.toByte())

        for (key in manifest.files.sorted()) {
            update(key.toByteArray())
            update(data[key]?.getData() ?: byteArrayOf())
        }

        for ((attribute, value) in manifest.attributes.entries.sortedBy { it.key }) {
            update(attribute.toByteArray())
            update(value.toString().toByteArray())
        }
    }.digest()
}
