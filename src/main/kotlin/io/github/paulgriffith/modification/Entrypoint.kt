@file:JvmName("Entrypoint")

package io.github.paulgriffith.modification

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes

class ModificationUpdater : CliktCommand(
    help = "Pass path(s) to resources to update their attributes accordingly.",
    printHelpOnEmptyArgs = true,
) {
    private val signatureOnly: Boolean by option(
        "-s",
        "--signature",
        help = "Prints the signature only",
    ).flag()

    private val noReplace: Boolean by option(
        "-nr",
        "--no-replace",
        help = "Will NOT replace the resource.json file",
    ).flag()

    private val toConsole: Boolean by option(
        "-c",
        "--console",
        help = "Prints the new file to the console",
    ).flag()

    // if files and attributes are unchanged, use this
    // to force an update of the actor and timestamp
    private val shouldForceUpdate: Boolean by option(
        "--force",
        hidden = true,
    ).flag()

    private val resources: List<Path> by argument(
        help = "The file to target",
    ).path(
        mustExist = true,
        canBeFile = false,
        mustBeReadable = true,
    ).multiple()

    private val actor by option(
        "-a",
        "--actor",
        help = "The new actor name",
    ).default(System.getProperty("user.name"))

    private val timestamp by option(
        "-t",
        "--timestamp",
        hidden = true, // this exists for testing only
    ).convert { timestamp ->
        Instant.parse(timestamp)
    }.defaultLazy(value = Instant::now)

    override fun run() {
        for (resourcePath in resources) {
            try {
                val resource = if (resourcePath.isRegularFile()) {
                    resourcePath.parent.toResource()
                } else {
                    resourcePath.toResource()
                }

                if (signatureOnly) {
                    println(resource.getSignature())
                }

                var updated: ProjectResource? = null
                if (noReplace || toConsole) {
                    updated = resource.update(actor, timestamp, shouldForceUpdate)
                }

                if (updated != null && toConsole) {
                    println(JSON.encodeToString(updated.manifest))
                }

                if (updated != null && !noReplace) {
                    writeManifest(
                        resourcePath.resolve(ResourceManifest.FILENAME),
                        updated.manifest,
                    )
                }
            } catch (e: Exception) {
                throw CliktError(cause = e)
            }
        }
    }

    private fun Path.toResource(): ProjectResource {
        val manifest = resolve(ResourceManifest.FILENAME).inputStream().toManifest()
        val data = manifest.files.associateWith { fileName ->
            val filePath = resolve(fileName)
            DataLoader(filePath::readBytes)
        }
        return ProjectResource(manifest, data)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeManifest(manifestPath: Path, manifest: ResourceManifest) {
        manifestPath.outputStream().use { stream ->
            JSON.encodeToStream(manifest, stream)
        }
    }
}

fun main(args: Array<String>) = ModificationUpdater().main(args)
