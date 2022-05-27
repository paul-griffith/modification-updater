package io.github.paulgriffith.modification

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull

fun FunSpec.deserializeResource(path: String): ProjectResource {
    val manifest = this::class.java.getResourceAsStream("/$path/resource.json").shouldNotBeNull().toManifest()
    val data: Map<String, DataLoader> = manifest.files.associateWith { fileName ->
        DataLoader { this::class.java.getResourceAsStream("/$path/$fileName").shouldNotBeNull().readAllBytes() }
    }

    return ProjectResource(manifest, data)
}
