package io.github.paulgriffith.modification

import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION
import io.github.paulgriffith.modification.LastModification.Companion.LAST_MODIFICATION_SIGNATURE
import io.github.paulgriffith.modification.LastModification.Companion.lastModification
import kotlinx.serialization.json.JsonPrimitive
import java.security.MessageDigest
import java.time.Instant

fun ProjectResource.getSignature(): String {
    return calculateContentHex(
        manifest = manifest.copy(
            attributes = manifest.attributes - LAST_MODIFICATION_SIGNATURE
        ),
        data = data
    )
}

private fun ResourceManifest.mapAttributes(
    actor: String?,
    time: Instant?,
    digest: String?
) : ResourceManifest{

    val newMod = LastModification(
        actor = actor ?: lastModification.actor,
        timestamp = time ?: lastModification.timestamp
    )

    val newAttrs = buildMap {
        putAll(attributes)
        if (digest == null) {
            remove(LAST_MODIFICATION_SIGNATURE)
        } else {
            put(
                LAST_MODIFICATION_SIGNATURE,
                JsonPrimitive(digest)
            )
        }
        put(
            LAST_MODIFICATION,
            JSON.encodeToJsonElement(
                LastModification.serializer(),
                newMod
            )
        )
    }

    return this.copy(attributes = newAttrs)
}

fun ProjectResource.update(
    actor: String,
    time: Instant,
    force:Boolean
): ProjectResource {
    val currentSignature = this.getSignature()
    val lastSignature = manifest.attributes[LAST_MODIFICATION_SIGNATURE].toString()

    if (
        !force
        && lastSignature == currentSignature
    ){
        return ProjectResource(manifest, data)
    }

    val tempManifest = manifest.mapAttributes(actor, time, null)
    val tempProResource = ProjectResource(tempManifest, data)
    val updatedManifest = tempManifest.mapAttributes(
        actor,
        time,
        tempProResource.getSignature()
    )

    return ProjectResource(updatedManifest, data)
}

private fun calculateContentHex(
    manifest: ResourceManifest,
    data: Map<String, DataLoader>
): String {
    return calculateContentDigest(
        manifest,
        data
    ).toHexString()
}

private fun calculateContentDigest(
    manifest: ResourceManifest,
    data: Map<String, DataLoader>
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
