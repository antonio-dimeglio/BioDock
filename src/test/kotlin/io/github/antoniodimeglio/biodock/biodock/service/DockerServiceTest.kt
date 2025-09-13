package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.util.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.pathString

class DockerServiceTest {

    val dockerService = DockerService()
    @Test
    fun `runPipeline should return success when both build and run succeed`() = runTest {
        val urlFastq = "https://zenodo.org/records/3736457/files/1_control_psbA3_2019_minq7.fastq"

        val tempInputFolder = Files.createTempDirectory("data")
        val tempOutputFolder = Files.createTempDirectory("results")

        val targetFile = tempInputFolder.resolve("temp.fastq")

        java.net.URI(urlFastq).toURL().openStream().use { input ->
            Files.copy(input, targetFile, StandardCopyOption.REPLACE_EXISTING)
        }

        val pipeline = PipelineService.getAvailablePipelines().first() // Assuming that the first is fastqc-base
        val result = dockerService.runPipeline(
            pipeline,
            tempInputFolder.pathString,
            tempOutputFolder.pathString
        )

        assertTrue(result is Result.Success, "Pipeline execution should succeed")

        val expectedHtmlFile = tempOutputFolder.resolve("temp_fastqc.html")
        val expectedZipFile = tempOutputFolder.resolve("temp_fastqc.zip")

        assertTrue(Files.exists(expectedHtmlFile), "FastQC HTML report should be generated: $expectedHtmlFile")
        assertTrue(Files.exists(expectedZipFile), "FastQC ZIP file should be generated: $expectedZipFile")

        assertTrue(Files.size(expectedHtmlFile) > 0, "HTML report should not be empty")
        assertTrue(Files.size(expectedZipFile) > 0, "ZIP file should not be empty")

        tempInputFolder.toFile().deleteRecursively()
        tempOutputFolder.toFile().deleteRecursively()
    }

    // Build Failure Cases
    @Test
    fun `runPipeline should return error when docker build fails`() { }

    @Test
    fun `runPipeline should return error when dockerfile does not exist`() { }

    // Run Failure Cases
    @Test
    fun `runPipeline should return error when docker run fails`() { }

    @Test
    fun `runPipeline should return error when pipeline command fails inside container`() { }

    // Argument Verification (what you can actually test)
    @Test
    fun `runPipeline should call docker build with correct image tag`() { }

    @Test
    fun `runPipeline should call docker run with correct volume mounts`() { }

    @Test
    fun `runPipeline should call docker run with correct pipeline commands`() { }

    // Edge Cases
    @Test
    fun `runPipeline should handle pipeline with special characters in id`() { }
}
