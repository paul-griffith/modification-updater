@file:JvmName("Entrypoint")

package io.github.paulgriffith.modification

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

class ModificationUpdater : CliktCommand(help = "Pass path(s) to resources to update their attributes accordingly.", printHelpOnEmptyArgs = true) {
    private val signature: Boolean by option("-s", "--signature").flag()
    private val resources: List<Path> by argument(help = "The file to target").path(
        mustExist = true,
        canBeFile = false,
        mustBeReadable = true
    ).multiple()
    private val actor by option("-a", "--actor", help = "The new actor name").default("external")
    private val timestamp by option("-t", "--timestamp", help = "The update timestamp").convert { Instant.parse(it) }

    override fun run() {
        resources.forEach { resourcePath ->
            val resource = getResource(resourcePath)
            if (signature) {
                println(resource.getSignature())
            } else {
                val updated = resource.update(actor, timestamp)
                println(JSON.encodeToString(ResourceManifest.serializer(), updated.manifest))
            }
        }
    }

    private fun getResource(location: Path): ProjectResource {
        val manifest = location.resolve("resource.json").inputStream().toManifest()
        val data = manifest.files.associateWith { fileName ->
            DataLoader { location.resolve(fileName).readBytes() }
        }
        return ProjectResource(manifest, data)
    }
}

fun main(args: Array<String>) = ModificationUpdater().main(args)
