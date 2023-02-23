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
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.inputStream
import kotlin.io.path.readBytes

class ModificationUpdater : CliktCommand(
    help = "Pass path(s) to resources to update their attributes accordingly.",
    printHelpOnEmptyArgs = true
) {
    private val sigOnly: Boolean by option(
        "-s", "--signature",
        help="Prints the signature only"
    ).flag()

    private val noReplace: Boolean by option(
        "-nr", "--no-replace",
        help="Will NOT replace the resource.json file"
    ).flag()

    private val toConsole: Boolean by option(
        "-c", "--console",
        help="Prints the new file to the console"
    ).flag()

    private val resources: List<Path> by argument(
        help = "The file to target"
    ).path(
        mustExist = true,
        canBeFile = false,
        mustBeReadable = true
    ).multiple()

    private val actor by option(
        "-a",
        "--actor",
        help = "The new actor name"
    ).default("external")

    private val timestamp by option(
        "-t",
        "--timestamp",
        hidden = true // this exists for testing only
    ).convert {
        Instant.parse(it)
    }.default(Instant.now())

    override fun run() {
        resources.forEach { resourcePath ->
            val resourceFile = locateResourceFile(resourcePath)
            var manifest = resourceToManifest(resourceFile)
            val resource = getProjectResource(resourcePath, manifest)

            if (sigOnly) {
                println(resource.getSignature())
            }

            var updated: ProjectResource? = null
            if (noReplace || toConsole){
                updated = resource.update(actor, timestamp)
            }

            if (updated != null && toConsole){
                println(
                    JSON.encodeToString(
                        ResourceManifest.serializer(),
                        updated.manifest
                    )
                )
            }

            if (updated != null && !noReplace){
                writeManifest(
                    resourceFile,
                    updated.manifest
                )
            }
        }
    }

    private fun locateResourceFile(folderPath: Path) : Path{
        return folderPath.resolve("resource.json");
    }

    private fun resourceToManifest(resourceFile: Path): ResourceManifest{
        return resourceFile.inputStream().toManifest()
    }

    private fun getProjectResource(
        folderPath: Path,
        manifest: ResourceManifest
    ) : ProjectResource{
        return ProjectResource(
            manifest,
            manifest.files.associateWith {
                    fn -> DataLoader { folderPath.resolve(fn).readBytes() }
            }
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeManifest(
        resourceFile: Path,
        manifest: ResourceManifest
    ){
        JSON.encodeToStream(
            ResourceManifest.serializer(),
            manifest,
            FileOutputStream(resourceFile.toFile())
        )
    }

}

fun main(args: Array<String>) = ModificationUpdater().main(args)
