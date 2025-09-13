package io.github.antoniodimeglio.biodock.biodock.service

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.listDirectoryEntries
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

        val logger = KotlinLogging.logger {}

        logger.info {
            """
                Got result: $result
                Files found: ${tempOutputFolder.listDirectoryEntries()}
            """.trimIndent()
        }
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
