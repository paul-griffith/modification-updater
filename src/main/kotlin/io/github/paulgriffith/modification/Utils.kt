package io.github.paulgriffith.modification

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.nio.file.Path
import java.util.*
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

internal fun Int.toByteArray(): ByteArray = byteArrayOf(
    (this shr 24).toByte(),
    (this shr 16).toByte(),
    (this shr 8).toByte(),
    this.toByte(),
)

internal fun Boolean.toByte(): Byte = (if (this) 1 else 0).toByte()

private val hexFormat = HexFormat.of()

internal fun ByteArray.encodeHex(): String = hexFormat.formatHex(this)
internal fun String.decodeHex(): ByteArray = hexFormat.parseHex(this)

@OptIn(ExperimentalSerializationApi::class)
fun InputStream.toManifest(): ResourceManifest = use(JSON::decodeFromStream)

fun Path.toResource(): ProjectResource {
    val manifest = resolve(ResourceManifest.FILENAME).inputStream().toManifest()
    val data = manifest.files.associateWith { fileName ->
        val filePath = resolve(fileName)
        DataLoader(filePath::readBytes)
    }
    return ProjectResource(manifest, data)
}
