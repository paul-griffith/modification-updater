@file:JvmName("Entrypoint")

package io.github.paulgriffith.modification

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.subcommands
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
import kotlin.io.path.outputStream

private fun CliktCommand.resourcePaths() = argument(
    help = "The resource(s) to target",
).path(
    mustExist = true,
    canBeFile = false,
    mustBeReadable = true,
).multiple(required = true)

class Verify : CliktCommand(
    help = "Pass path(s) to resources to verify their signatures are correct. ",
    printHelpOnEmptyArgs = true,
) {
    private val resourcePaths: List<Path> by resourcePaths()

    private var error = false

    private fun error(path: Path, message: String) {
        error = true
        echo("$path: $message", err = true)
    }

    override fun run() {
        for (path in resourcePaths) {
            try {
                val resource = path.toResource()
                val resourceSignature = resource.manifestSignature
                if (resourceSignature == null) {
                    error(path, "No signature to verify")
                    continue
                }
                val calculatedSignature = resource.calculateSignature()
                if (!resourceSignature.contentEquals(calculatedSignature)) {
                    error(
                        path,
                        "Signature mismatch; expected ${resourceSignature.encodeHex()}, got ${calculatedSignature.encodeHex()}",
                    )
                }
            } catch (e: Exception) {
                error(path, e.message ?: e.toString())
            }
        }
        if (error) {
            throw ProgramResult(1)
        }
    }
}

class Signatures : CliktCommand(
    help = "Pass path(s) to resources to output their calculated signatures",
    printHelpOnEmptyArgs = true,
) {
    private val resourcePaths: List<Path> by resourcePaths()

    override fun run() {
        for (path in resourcePaths) {
            val resource = path.toResource()
            val calculatedSignature = resource.calculateSignature()
            println(calculatedSignature.encodeHex())
        }
    }
}

class Update : CliktCommand(
    help = "Pass path(s) to resources to output their calculated signatures",
    printHelpOnEmptyArgs = true,
) {
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

    private val dryRun by option(
        "-s",
        "--dry-run",
    ).flag(default = false)

    private val resourcePaths: List<Path> by resourcePaths()

    override fun run() {
        for (resourcePath in resourcePaths) {
            val resource = resourcePath.toResource()

            val updated: ProjectResource = resource.update(actor, timestamp, true)

            if (dryRun) {
                println(JSON.encodeToString(updated.manifest))
            } else {
                writeManifest(
                    resourcePath.resolve(ResourceManifest.FILENAME),
                    updated.manifest,
                )
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun writeManifest(manifestPath: Path, manifest: ResourceManifest) {
        manifestPath.outputStream().use { stream ->
            JSON.encodeToStream(manifest, stream)
        }
    }
}

class ModificationUpdater : CliktCommand(printHelpOnEmptyArgs = true) {
    init {
        subcommands(
            Verify(),
            Signatures(),
            Update(),
        )
    }

    override fun run() = Unit
}

fun main(args: Array<String>) = ModificationUpdater().main(args)
