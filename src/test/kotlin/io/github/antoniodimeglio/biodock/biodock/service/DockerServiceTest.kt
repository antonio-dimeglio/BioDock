package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.util.Result
import io.github.antoniodimeglio.biodock.biodock.util.StringGenerators
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.io.path.pathString
import kotlin.io.path.writeText

class DockerServiceTest {

    val dockerService = DockerService()

    // Complete Pipeline Workflow Tests
    @Test
    fun `complete pipeline workflow should succeed when both build and run succeed`() = runTest {
        val tempInputFolder = Files.createTempDirectory("data")
        val tempOutputFolder = Files.createTempDirectory("results")

        val targetFile = tempInputFolder.resolve("temp.fastq")
        targetFile.writeText(StringGenerators.generateSyntheticFastq())

        val pipeline = PipelineService.getAvailablePipelines().first() // Assuming that the first is fastqc-base

        // Test build step
        val buildResult = dockerService.buildDockerImage(pipeline)
        assertTrue(buildResult is Result.Success, "Docker build should succeed")

        // Test run step
        val runResult = dockerService.runPipeline(
            pipeline,
            tempInputFolder.pathString,
            tempOutputFolder.pathString
        )
        assertTrue(runResult is Result.Success, "Pipeline execution should succeed")

        val expectedHtmlFile = tempOutputFolder.resolve("temp_fastqc.html")
        val expectedZipFile = tempOutputFolder.resolve("temp_fastqc.zip")

        assertTrue(Files.exists(expectedHtmlFile), "FastQC HTML report should be generated: $expectedHtmlFile")
        assertTrue(Files.exists(expectedZipFile), "FastQC ZIP file should be generated: $expectedZipFile")

        assertTrue(Files.size(expectedHtmlFile) > 0, "HTML report should not be empty")
        assertTrue(Files.size(expectedZipFile) > 0, "ZIP file should not be empty")

        tempInputFolder.toFile().deleteRecursively()
        tempOutputFolder.toFile().deleteRecursively()
    }

    // Build Tests
    @Test
    fun `buildDockerImage should return success when dockerfile exists and is valid`() = runTest { }

    @Test
    fun `buildDockerImage should return error when dockerfile does not exist`() = runTest { }

    @Test
    fun `buildDockerImage should return error when dockerfile is invalid`() = runTest { }

    @Test
    fun `buildDockerImage should create image with correct tag`() = runTest { }

    // Run Tests (assuming image already exists)
    @Test
    fun `runPipeline should return success when image exists and command succeeds`() = runTest { }

    @Test
    fun `runPipeline should return error when image does not exist`() = runTest { }

    @Test
    fun `runPipeline should return error when pipeline command fails inside container`() = runTest { }

    @Test
    fun `runPipeline should mount volumes correctly`() = runTest { }

    @Test
    fun `runPipeline should pass correct command arguments to container`() = runTest { }

    @Test
    fun `runPipeline should handle special characters in pipeline id`() = runTest { }

    @Test
    fun `runPipeline should remove container after execution`() = runTest { }
}
