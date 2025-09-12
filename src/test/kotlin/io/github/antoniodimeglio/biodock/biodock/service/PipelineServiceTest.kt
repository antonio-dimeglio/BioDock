package io.github.antoniodimeglio.biodock.biodock.service


import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists

@OptIn(ExperimentalPathApi::class)
class PipelineServiceTest {
    val actualPipeline = Pipeline(
        id="fastqc-base",
        name="FastQC",
        description="The base pipeline for fastQC.",
        command=listOf("fastqc", "--outdir=/data/output", "/data/input/*.fastq"),
        version="1.0.0",
        inputFileTypes = listOf("fastq", "fq", "fastq.gz", "fq.gz"),
        outputFileTypes = listOf("html", "zip"),
        inputDirectory = "/data/input",
        outputDirectory = "/data/output")

    val mockPipeline = Pipeline(
        id="mockPipeline",
        name="mockPipeline",
        description="mockPipeline",
        command=listOf(),
        version="1.0.0",
        inputFileTypes = listOf(),
        outputFileTypes = listOf(),
        inputDirectory = "",
        outputDirectory = "")

    @Test
    fun `getAvailablePipelines should return all available pipelines`() {
        val tempPipelineDir = Files.createTempDirectory("test-pipelines")
        val testPipelineDir = tempPipelineDir.resolve("test-pipeline")
        Files.createDirectories(testPipelineDir)

        val dockerfile = testPipelineDir.resolve("Dockerfile")
        val configFile = testPipelineDir.resolve("config.json")

        Files.createFile(dockerfile)
        Files.writeString(configFile, Json.encodeToString(mockPipeline))

        try {
            val expected = listOf(mockPipeline)
            val result = PipelineService.getAvailablePipelines("$tempPipelineDir/")
            assertEquals(expected, result)
        } finally {
            tempPipelineDir.deleteRecursively()
        }
    }

    @Test
    fun `getAvailablePipelines should exclude malformed pipelines`() {
        val tempPipelineDir = Files.createTempDirectory("test-pipelines")
        val malformedDir = tempPipelineDir.resolve("malformed-pipeline")
        Files.createDirectories(malformedDir)

        Files.createFile(malformedDir.resolve("Dockerfile"))
        Files.writeString(malformedDir.resolve("config.json"), "invalid json")

        try {
            val result = PipelineService.getAvailablePipelines("$tempPipelineDir/")
            assertTrue(result.isEmpty())
        } finally {
            tempPipelineDir.deleteRecursively()
        }
    }

    @Test
    fun `getAvailablePipelines should return empty list for empty directory`() {
        val tempPipelineDir = Files.createTempDirectory("empty-pipelines")

        try {
            val result = PipelineService.getAvailablePipelines("$tempPipelineDir/")
            assertTrue(result.isEmpty())
        } finally {
            tempPipelineDir.deleteRecursively()
        }
    }

    @Test
    fun `savePipeline should save valid pipeline`() {
        val tempPipelineDir = Files.createTempDirectory("test-pipelines")
        val tempDockerFile = Files.createTempFile("test-dockerfile", ".dockerfile")
        Files.writeString(tempDockerFile, "FROM ubuntu:latest")

        try {
            val result = PipelineService.savePipeline(mockPipeline, tempDockerFile.toString(),
                "$tempPipelineDir/")
            assertEquals(ValidationResult.Success("Successfully saved pipeline."), result)

            val savedPipelines = PipelineService.getAvailablePipelines("$tempPipelineDir/")
            assertEquals(listOf(mockPipeline), savedPipelines)
        } finally {
            tempPipelineDir.deleteRecursively()
            tempDockerFile.deleteIfExists()
        }
    }

    @Test
    fun `savePipeline should fail when invalid dockerfile is provided`() {
        val tempPipelineDir = Files.createTempDirectory("test-pipelines")

        try {
            val expected = ValidationResult.Error("Dockerfile not found at nonexistentfile")
            val result = PipelineService.savePipeline(mockPipeline, "nonexistentfile", "$tempPipelineDir/")
            assertEquals(expected, result)
        } finally {
            tempPipelineDir.deleteRecursively()
        }
    }

    @Test
    fun `deletePipeline should correctly delete a valid pipeline`() {
        val tempPipelineDir = Files.createTempDirectory("test-pipelines")
        val tempDockerFile = Files.createTempFile("test-dockerfile", ".dockerfile")

        try {
            // First save a pipeline
            PipelineService.savePipeline(mockPipeline, tempDockerFile.toString(), "$tempPipelineDir/")

            // Verify it exists
            assertTrue(tempPipelineDir.resolve("mockPipeline").exists())

            // Delete it
            PipelineService.deletePipeline(mockPipeline, "$tempPipelineDir/")

            // Verify it's gone
            assertFalse(tempPipelineDir.resolve("mockPipeline").exists())
        } finally {
            tempPipelineDir.deleteRecursively()
            tempDockerFile.deleteIfExists()
        }
    }

    @Test
    fun `validatePipeline should return success for valid pipeline`() {
        val tempPipelineDir = Files.createTempDirectory("test-pipelines")
        val tempDockerFile = Files.createTempFile("test-dockerfile", ".dockerfile")
        Files.writeString(tempDockerFile, "FROM ubuntu:latest")

        try {
            PipelineService.savePipeline(mockPipeline, tempDockerFile.toString(), "$tempPipelineDir/")

            val result = PipelineService.validatePipeline(mockPipeline, "$tempPipelineDir/")
            assertEquals(ValidationResult.Success("Valid pipeline."), result)
        } finally {
            tempPipelineDir.deleteRecursively()
            tempDockerFile.deleteIfExists()
        }
    }
}