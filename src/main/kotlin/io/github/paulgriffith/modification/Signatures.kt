package io.github.paulgriffith.modification

import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION
import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION_SIGNATURE
import io.github.paulgriffith.modification.LastModification.Companion.lastModification
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import java.security.MessageDigest
import java.time.Instant

private fun ResourceManifest.updateAttributes(
    actor: String?,
    time: Instant?,
    signature: ByteArray?,
): ResourceManifest {
    return copy(
        attributes = buildMap {
            putAll(attributes)
            if (signature == null) {
                remove(LAST_MODIFICATION_SIGNATURE)
            } else {
                put(LAST_MODIFICATION_SIGNATURE, JsonPrimitive(signature.encodeHex()))
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
    val currentSignature = calculateSignature()
    val lastSignature = manifest.signature

    if (lastSignature.contentEquals(currentSignature) && !force) {
        return ProjectResource(manifest, data)
    }

    val tempManifest = manifest.updateAttributes(actor, time, null)
    val tempResource = ProjectResource(tempManifest, data)
    val updatedManifest = tempManifest.updateAttributes(
        actor,
        time,
        tempResource.calculateSignature(),
    )

    return ProjectResource(updatedManifest, data)
}

fun ProjectResource.calculateSignature(): ByteArray {
    return MessageDigest.getInstance("SHA-256").apply {
        update(manifest.scope.toByteArray())

        manifest.documentation?.let { documentation ->
            update(documentation.toByteArray())
        }

        update(manifest.version.toByteArray())
        update((manifest.locked ?: false).toByte())
        update(manifest.restricted.toByte())
        update(manifest.overridable.toByte())

        for (file in manifest.files.sorted()) {
            update(file.toByteArray())
            update(data[file]?.getData() ?: byteArrayOf())
        }

        val nonSignatureAttributes = manifest.attributes - LAST_MODIFICATION_SIGNATURE
        val sortedAttributes = nonSignatureAttributes.entries
            .sortedBy { it.key }
        for ((key, value) in sortedAttributes) {
            update(key.toByteArray())
            update(value.toString().toByteArray())
        }
    }.digest()
}
