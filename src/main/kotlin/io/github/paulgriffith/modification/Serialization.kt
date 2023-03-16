package io.github.paulgriffith.modification

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
val JSON = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    explicitNulls = false
    encodeDefaults = true
}

object ApplicationScopeDeserializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("scope", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int {
        return when (decoder.decodeChar()) {
            'N' -> 0
            'G' -> 1
            'D' -> 2
            'C' -> 4
            'A' -> 7
            else -> throw IllegalArgumentException()
        }
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeChar(
            when (value) {
                0 -> 'N'
                1 -> 'G'
                2 -> 'D'
                4 -> 'C'
                7 -> 'A'
                else -> throw IllegalArgumentException()
            },
        )
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "java.time.Instant",
        PrimitiveKind.STRING,
    )

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .withZone(ZoneId.of("UTC"))

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.from(formatter.parse(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(formatter.format(value))
    }
}
