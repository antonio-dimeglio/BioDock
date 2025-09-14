package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.antoniodimeglio.biodock.biodock.util.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.fileSize

object PipelineService {
    private val logger = KotlinLogging.logger {}

    fun getAvailablePipelines(pipelinePath:String = "pipelines/"): List<Pipeline> {
        return FileService.listSubdirectories(pipelinePath).mapNotNull { dir ->
            try {
                val dockerfile = dir.resolve("Dockerfile")
                val config = dir.resolve("config.json")

                if (dockerfile.exists() && config.exists()) {
                    val json = config.readText()
                    Json.decodeFromString<Pipeline>(json)
                } else {
                    null
                }
            } catch (e: Exception) {
                logger.error { "Tried to load pipeline ${dir.name}, got error: \n\t${e.message}"}
                null
            }
        }
    }

    fun savePipeline(pipeline: Pipeline, dockerFilePath: String, pipelinePath:String = "pipelines/"): Result<String> {
        val pipelineDir = Paths.get(pipelinePath, pipeline.id)
        val configFile = pipelineDir.resolve("config.json")
        val dockerFile = pipelineDir.resolve("Dockerfile")
        val sourceDockerFile = Paths.get(dockerFilePath)

        val dirExistedBefore = Files.exists(pipelineDir)

        try {
            Files.createDirectories(pipelineDir)

            val json = Json.encodeToString(pipeline)
            Files.writeString(configFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

            if (!Files.exists(sourceDockerFile)) {
                return Result.Error("Dockerfile not found at $dockerFilePath")
        }
        Files.copy(sourceDockerFile, dockerFile, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            if (!dirExistedBefore) {
                try {
                    Files.deleteIfExists(pipelineDir)
                } catch (cleanupException: Exception) {
                    logger.warn { "Failed to cleanup directory after error: ${cleanupException.message}" }
                }
            }
            return Result.Error("Error, got exception ${e.message} when trying to save pipeline.", e)
        }

        return Result.Success(pipelineDir.toString(), "Successfully saved pipeline.")
    }

    @OptIn(ExperimentalPathApi::class)
    fun deletePipeline(pipeline: Pipeline, pipelinePath: String = "pipelines/") {
        val pipelineDir = Paths.get(pipelinePath, pipeline.id)
        if (pipelineDir.exists()){
            pipelineDir.deleteRecursively()
        } else {
            logger.warn { "Could not delete ${pipeline.id} as it does not exist."}
        }
    }

    fun validatePipeline(pipeline: Pipeline, pipelineDir: String = "pipelines/"): Result<Pipeline> {
        val pipelineFolder = Paths.get(pipelineDir, pipeline.id)

        if (!pipelineFolder.exists()) {
            return Result.Error("Pipeline ${pipeline.id} does not exist.")
        }

        val json = pipelineFolder.resolve("config.json").toFile()
        val dockerfilePath = pipelineFolder.resolve("Dockerfile")

        if (!json.exists()) {
            return Result.Error("Could not find json for pipeline ${pipeline.id}.")
        }

        if (!dockerfilePath.exists()) {
            return Result.Error("Could not find dockerfile for pipeline ${pipeline.id}.")
        }
        if (dockerfilePath.fileSize() == 0L) {
            return Result.Error("Found empty dockerfile for pipeline ${pipeline.id}.")
        }

        return try {
            val content = json.readText()
            val validatedPipeline = Json.decodeFromString<Pipeline>(content)
            Result.Success(validatedPipeline, "Valid pipeline.")
        } catch (e: Exception) {
            Result.Error("Failed to parse config.json for pipeline, got error ${e.message}", e)
        }
    }
}