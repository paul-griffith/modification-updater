package io.github.paulgriffith.modification

import java.security.MessageDigest
import java.util.*

fun ProjectResource.calculateSignature(): String {
    val withoutLastModification = copy(
        manifest = manifest.copy(
            attributes = manifest.attributes - "lastModificationSignature"
        )
    )
    val signature: ByteArray = withoutLastModification.calculateContentDigest()
    return HexFormat.of().formatHex(signature)
}

private fun ProjectResource.calculateContentDigest(): ByteArray {
    return MessageDigest.getInstance("SHA-256").apply {
        update(manifest.scope.toByteArray())

        manifest.documentation?.let { documentation ->
            update(documentation.toByteArray())
        }

        update(manifest.version.toByteArray())
        update(manifest.locked.toByte())
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
