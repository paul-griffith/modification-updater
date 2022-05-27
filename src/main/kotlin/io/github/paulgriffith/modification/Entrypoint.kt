@file:JvmName("Entrypoint")
package io.github.paulgriffith.modification

import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

fun main(args: Array<String>) {
    when (args.getOrElse(1) { "-h" }) {
        "-s" -> {
            val basePath = Path(args[2])
            val manifest = basePath.resolve("resource.json").inputStream().toManifest()
            val data = manifest.files.associateWith { fileName ->
                DataLoader { basePath.resolve(fileName).readBytes() }
            }
            println(ProjectResource(manifest, data).calculateSignature())
        }
        else -> {
            println("USAGE: modification-updater flag [arguments]")
            println()
            println("-h - Shows this help message.")
            println("-s - Returns the signature of the resource file passed as the first argument.")
        }
    }
}
