package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class PipelineService {
    private val logger = KotlinLogging.logger {}

    private fun listSubdirectories(path: String): List<File> {
        val dir = Paths.get(path)
        if (!Files.isDirectory(dir)) return emptyList()

        return Files.list(dir).use { stream ->
            stream.filter { Files.isDirectory(it) }
                .map { it.toFile() }
                .toList()
        }
    }

    fun getAvailablePipelines(): List<Pipeline> {
        val pipelines = mutableListOf<Pipeline>()

        listSubdirectories("pipelines/").map { dir ->
            val dockerfile = dir.resolve("Dockerfile")
            val config = dir.resolve("config.json")

            if (dockerfile.exists() && config.exists()) {
                val json = config.readText()
                val pipeline = Json.decodeFromString<Pipeline>(json)
                pipelines.add(pipeline)
            }
        }.toList()

        return pipelines
    }

    fun savePipeline(pipeline: Pipeline) {
        TODO()
    }

    fun updatePipeline(pipeline: Pipeline) {
        TODO()
    }

    fun deletePipeline(pipeline: Pipeline) {
        TODO()
    }

    fun validatePipeline(pipeline: Pipeline): ValidationResult {
        TODO()
    }
}