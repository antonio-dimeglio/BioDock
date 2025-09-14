package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.antoniodimeglio.biodock.biodock.util.CommandResult
import io.github.antoniodimeglio.biodock.biodock.util.MockCommandExecutor
import io.github.antoniodimeglio.biodock.biodock.util.Result
import io.github.antoniodimeglio.biodock.biodock.util.StringGenerators
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import kotlin.io.path.pathString
import kotlin.io.path.writeText

class DockerServiceTest {
    val dockerService = DockerService()
    val fastqcPipeline = Pipeline(
        id="fastqc-base",
        name="FastQC",
        description="The base pipeline for fastQC.",
        command=listOf("fastqc", "--outdir=/results", "/data/*.fastq"),
        version="1.0.0",
        inputFileTypes = listOf("fastq", "fq", "fastq.gz", "fq.gz"))
    val mockNonExistingPipeline = Pipeline(
        id="mockpipeline",
        name="mockPipeline",
        description="mockPipeline",
        command=listOf(),
        version="1.0.0",
        inputFileTypes = listOf())
    val mockInvalidPipeline = Pipeline(
        id="mockPipeline",
        name="mockPipeline",
        description="mockPipeline",
        command=listOf(),
        version="1.0.0",
        inputFileTypes = listOf())
    val failingPipeline = Pipeline(
        id = "fastqc-base",
        name = "Failing FastQC",
        description = "FastQC with invalid params",
        command = listOf("fastqc", "--outdir=/results"), // Invalid commands
        version = "1.0.0",
        inputFileTypes = listOf("fastq")
    )

    @Test
    fun `complete pipeline workflow should succeed when both build and run succeed`() = runTest {
        val tempInputFolder = Files.createTempDirectory("data")
        val tempOutputFolder = Files.createTempDirectory("results")

        val targetFile = tempInputFolder.resolve("temp.fastq")
        targetFile.writeText(StringGenerators.generateSyntheticFastq())

        val pipeline = PipelineService.getAvailablePipelines().first()

        val buildResult = dockerService.buildDockerImage(pipeline)
        assertTrue(buildResult is Result.Success, "Docker build should succeed")

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

        dockerService.removeDockerImage(pipeline)
    }


    @Test
    fun `buildDockerImage should return success when dockerfile exists and is valid`() = runTest {
        val expected = Result.Success("biodock/${fastqcPipeline.id}", "Successfully built docker image: ${fastqcPipeline.id}")
        val result = dockerService.buildDockerImage(fastqcPipeline)

        assertEquals(expected, result)
    }

    @Test
    fun `buildDockerImage should return error when dockerfile does not exist`() = runTest {
        val result = dockerService.buildDockerImage(mockNonExistingPipeline)

        assertTrue(result is Result.Error, "Should return an error when dockerfile does not exist")
    }

    @Test
    fun `buildDockerImage should return error when dockerfile is invalid`() = runTest {
        val result = dockerService.buildDockerImage(mockInvalidPipeline)
        when (result) {
            is Result.Success -> fail("Expected error but got success")
            is Result.Error -> {
                assertEquals("Failed to build docker image: ERROR: failed to build: invalid tag \"biodock/mockPipeline\": repository name must be lowercase\n", result.message)
            }
        }
    }

    @Test
    fun `runPipeline should return success when image exists and command succeeds`() = runTest {
        val tempInputFolder = Files.createTempDirectory("data")
        val tempOutputFolder = Files.createTempDirectory("results")

        val targetFile = tempInputFolder.resolve("temp.fastq")
        targetFile.writeText(StringGenerators.generateSyntheticFastq())

        val pipeline = PipelineService.getAvailablePipelines().first()

        val runResult = dockerService.runPipeline(
            pipeline,
            tempInputFolder.pathString,
            tempOutputFolder.pathString
        )
        assertTrue(runResult is Result.Success, "Pipeline execution should succeed")
    }

    @Test
    fun `runPipeline should return error when image does not exist`() = runTest {
        val tempInputFolder = Files.createTempDirectory("data")
        val tempOutputFolder = Files.createTempDirectory("results")

        val targetFile = tempInputFolder.resolve("temp.fastq")
        targetFile.writeText(StringGenerators.generateSyntheticFastq())


        val result = dockerService.runPipeline(
            mockNonExistingPipeline,
            tempInputFolder.pathString,
            tempOutputFolder.pathString
        )

        assertTrue(result is Result.Error, "Pipeline execution should fail for non-existent image")
    }

    @Test
    fun `runPipeline should return error when pipeline command fails inside container`() = runTest {
        val tempInputFolder = Files.createTempDirectory("data")
        val tempOutputFolder = Files.createTempDirectory("results")

        val targetFile = tempInputFolder.resolve("temp.fastq")
        targetFile.writeText(StringGenerators.generateSyntheticFastq())

        val result = dockerService.runPipeline(
            failingPipeline,
            tempInputFolder.pathString,
            tempOutputFolder.pathString
        )

        assertTrue(result is Result.Error, "Pipeline execution should fail for invalid commands")
    }

    @Test
    fun `runPipeline should mount volumes correctly`() = runTest {
        val mockExecutor = MockCommandExecutor()
        val dockerService = DockerService(mockExecutor)

        dockerService.runPipeline(fastqcPipeline, "/host/input", "/host/output")

        val dockerRunCommand = mockExecutor.executedCommands.last()
        assertTrue(dockerRunCommand.contains("-v"))
        assertTrue(dockerRunCommand.contains("/host/input:/data"))
        assertTrue(dockerRunCommand.contains("/host/output:/results"))
    }

    @Test
    fun `runPipeline should pass correct command arguments to container`() = runTest {
        val mockExecutor = MockCommandExecutor()
        val dockerService = DockerService(mockExecutor)

        dockerService.runPipeline(fastqcPipeline, "/input", "/output")

        val command = mockExecutor.executedCommands.last()
        assertTrue(command.contains("fastqc"))
        assertTrue(command.contains("--outdir=/results"))
    }

    @Test
    fun `runPipeline should handle special characters in pipeline id`() = runTest {
        val mockExecutor = MockCommandExecutor()
        val dockerService = DockerService(mockExecutor)

        val specialPipeline = Pipeline(
            id = "my-pipeline_v1.2",
            name = "Special Pipeline",
            description = "Pipeline with special characters",
            command = listOf("echo", "test"),
            version = "1.0.0",
            inputFileTypes = listOf("txt")
        )

        dockerService.runPipeline(specialPipeline, "/input", "/output")

        val command = mockExecutor.executedCommands.last()
        assertTrue(command.contains("biodock/my-pipeline_v1.2"), "Should use pipeline id in image name")
        assertTrue(command.contains("--name"), "Should set container name")
        assertTrue(command.contains("my-pipeline_v1.2"), "Should use pipeline id as container name")
    }

    @Test
    fun `runPipeline should remove container after execution`() = runTest {
        val mockExecutor = MockCommandExecutor()
        val dockerService = DockerService(mockExecutor)

        dockerService.runPipeline(fastqcPipeline, "/input", "/output")

        val command = mockExecutor.executedCommands.last()
        assertTrue(command.contains("--rm"), "Should include --rm flag to remove container after execution")
    }

    // fetchContainerStatus Tests
    @Test
    fun `fetchContainerStatus should return container info when container exists`() = runTest {
        val mockExecutor = MockCommandExecutor()
        mockExecutor.mockResult = CommandResult(
            exitCode = 0,
            output = "abc123\tbiodock/fastqc-base\tfastqc --outdir=/data\t2025-01-15 10:30:00\tExited (0)\t\tfastqc-base",
            error = ""
        )
        val dockerService = DockerService(mockExecutor)

        val result = dockerService.fetchContainerStatus(fastqcPipeline)

        val command = mockExecutor.executedCommands.last()
        assertTrue(command.contains("docker"))
        assertTrue(command.contains("ps"))
        assertTrue(command.contains("-a"))
        assertTrue(command.contains("--filter"))
        assertTrue(command.contains("name=${fastqcPipeline.id}"))

        when (result) {
            is Result.Success -> {
                val containerInfo = result.data
                assertEquals("abc123", containerInfo.containerId)
                assertEquals("biodock/fastqc-base", containerInfo.image)
                assertEquals("fastqc --outdir=/data", containerInfo.command)
                assertEquals("Exited (0)", containerInfo.status)
            }
            is Result.Error -> fail("Expected success but got error: ${result.message}")
        }
    }

    @Test
    fun `fetchContainerStatus should return error when container not found`() = runTest {
        val mockExecutor = MockCommandExecutor()
        mockExecutor.mockResult = CommandResult(exitCode = 0, output = "", error = "")
        val dockerService = DockerService(mockExecutor)

        when (val result = dockerService.fetchContainerStatus(fastqcPipeline)) {
            is Result.Success -> fail("Expected error but got success")
            is Result.Error -> {
                assertEquals("Failed to fetch container status: ", result.message)
            }
        }
    }


    @Test
    fun `fetchContainerStatus should return error when docker command fails`() = runTest {
        val mockExecutor = MockCommandExecutor()
        mockExecutor.mockResult = CommandResult(exitCode = 1, output = "", error = "Docker daemon not running")
        val dockerService = DockerService(mockExecutor)

        when (val result = dockerService.fetchContainerStatus(fastqcPipeline)) {
            is Result.Success -> fail("Expected error but got success")
            is Result.Error -> {
                assertEquals("Failed to fetch container status: Docker daemon not running", result.message)
            }
        }
    }

    // removeDockerImage Tests
    @Test
    fun `removeDockerImage should return success when image is removed successfully`() = runTest {
        val mockExecutor = MockCommandExecutor()
        mockExecutor.mockResult = CommandResult(exitCode = 0, output = "", error = "")
        val dockerService = DockerService(mockExecutor)

        val result = dockerService.removeDockerImage(fastqcPipeline)

        val command = mockExecutor.executedCommands.last()
        assertTrue(command.contains("docker"))
        assertTrue(command.contains("rmi"))
        assertTrue(command.contains("biodock/${fastqcPipeline.id}"))
        assertTrue(result is Result.Success)
    }

    @Test
    fun `removeDockerImage should return error when removal fails`() = runTest {
        val mockExecutor = MockCommandExecutor()
        mockExecutor.mockResult = CommandResult(exitCode = 1, output = "", error = "No such image")
        val dockerService = DockerService(mockExecutor)

        val result = dockerService.removeDockerImage(fastqcPipeline)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `removeDockerImage should use correct docker rmi command`() = runTest {
        val mockExecutor = MockCommandExecutor()
        val dockerService = DockerService(mockExecutor)

        dockerService.removeDockerImage(fastqcPipeline)

        val command = mockExecutor.executedCommands.last()
        assertEquals(listOf("docker", "rmi", "biodock/fastqc-base"), command)
    }

    // expandGlobs Tests
    @Test
    fun `expandGlobs should return unchanged command when no wildcards present`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("test").pathString

        val command = listOf("fastqc", "--outdir=/results", "/data/file.fastq")
        val result = dockerService.expandGlobs(command, tempDir)

        assertEquals(command, result)
    }

    @Test
    fun `expandGlobs should expand single wildcard argument`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("test")

        // Create test files
        Files.createFile(tempDir.resolve("file1.fastq"))
        Files.createFile(tempDir.resolve("file2.fastq"))
        Files.createFile(tempDir.resolve("other.txt"))

        val command = listOf("fastqc", "*.fastq")
        val result = dockerService.expandGlobs(command, tempDir.pathString)

        assertEquals(3, result.size)
        assertEquals("fastqc", result[0])
        assertTrue(result.contains("file1.fastq") || result.contains("file2.fastq"))
        assertFalse(result.any { it.contains("other.txt") })

        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `expandGlobs should handle multiple wildcard arguments`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("test")

        // Create test files
        Files.createFile(tempDir.resolve("input1.fastq"))
        Files.createFile(tempDir.resolve("input2.fastq"))
        Files.createFile(tempDir.resolve("config.json"))

        val command = listOf("tool", "*.fastq", "*.json", "--output", "/results")
        val result = dockerService.expandGlobs(command, tempDir.pathString)

        assertTrue(result.contains("tool"))
        assertTrue(result.contains("--output"))
        assertTrue(result.contains("/results"))
        assertTrue(result.any { it.contains("input") && it.contains(".fastq") })
        assertTrue(result.any { it.contains("config.json") })

        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `expandGlobs should return original pattern when no matches found`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("test")

        // Create a file that doesn't match the pattern
        Files.createFile(tempDir.resolve("file.txt"))

        val command = listOf("tool", "*.fastq")
        val result = dockerService.expandGlobs(command, tempDir.pathString)

        assertEquals(listOf("tool", "*.fastq"), result)

        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `expandGlobs should handle path prefixes in glob patterns`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("test")

        // Create test files
        Files.createFile(tempDir.resolve("sample1.fastq"))
        Files.createFile(tempDir.resolve("sample2.fastq"))

        val command = listOf("tool", "/data/*.fastq")
        val result = dockerService.expandGlobs(command, tempDir.pathString)

        assertEquals(3, result.size)
        assertEquals("tool", result[0])
        assertTrue(result.any { it == "/data/sample1.fastq" || it == "/data/sample2.fastq" })

        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `expandGlobs should handle empty directory`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("empty")

        val command = listOf("tool", "*.fastq")
        val result = dockerService.expandGlobs(command, tempDir.pathString)

        assertEquals(listOf("tool", "*.fastq"), result)

        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `expandGlobs should handle mixed wildcard and non-wildcard arguments`() = runTest {
        val dockerService = DockerService(MockCommandExecutor())
        val tempDir = Files.createTempDirectory("test")

        // Create test files
        Files.createFile(tempDir.resolve("input.fastq"))
        Files.createFile(tempDir.resolve("data.fastq"))

        val command = listOf("fastqc", "*.fastq", "--outdir", "/results", "--threads", "4")
        val result = dockerService.expandGlobs(command, tempDir.pathString)

        assertTrue(result.contains("fastqc"))
        assertTrue(result.contains("--outdir"))
        assertTrue(result.contains("/results"))
        assertTrue(result.contains("--threads"))
        assertTrue(result.contains("4"))
        assertTrue(result.any { it.contains("input.fastq") || it.contains("data.fastq") })

        tempDir.toFile().deleteRecursively()
    }
}
