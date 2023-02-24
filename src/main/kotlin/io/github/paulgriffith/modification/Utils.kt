package io.github.paulgriffith.modification

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream
import java.util.*

internal fun Int.toByteArray(): ByteArray = byteArrayOf(
    (this shr 24).toByte(),
    (this shr 16).toByte(),
    (this shr 8).toByte(),
    this.toByte(),
)

internal fun Boolean.toByte(): Byte = (if (this) 1 else 0).toByte()

private val hexFormat = HexFormat.of()

internal fun ByteArray.toHexString(): String = hexFormat.formatHex(this)

@OptIn(ExperimentalSerializationApi::class)
fun InputStream.toManifest(): ResourceManifest = use(JSON::decodeFromStream)
