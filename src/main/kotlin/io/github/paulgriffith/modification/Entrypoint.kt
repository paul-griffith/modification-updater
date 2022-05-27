@file:JvmName("Entrypoint")
package io.github.paulgriffith.modification

import java.time.Instant
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

fun main(args: Array<String>) {
    when (args.getOrElse(0) { "-h" }) {
        "-s" -> {
            val resource = getResource(args[1])
            println(resource.getSignature())
        }
        "-u" -> {
            val resource = getResource(args[1])
            val updated = resource.update(args[2], args.getOrNull(3)?.let { Instant.parse(it) })
            println(JSON.encodeToString(ResourceManifest.serializer(), updated.manifest))
        }
        else -> {
            println("USAGE: modification-updater flag [arguments]")
            println()
            println("-h - Shows this help message.")
            println("-s - Returns the signature of the resource file passed as the first argument.")
            println("-u - Updates the resource file passed as the first argument with a new actor and, optionally, timestamp")
        }
    }
}

private fun getResource(location: String): ProjectResource {
    val basePath = Path(location)
    val manifest = basePath.resolve("resource.json").inputStream().toManifest()
    val data = manifest.files.associateWith { fileName ->
        DataLoader { basePath.resolve(fileName).readBytes() }
    }
    return ProjectResource(manifest, data)
}
