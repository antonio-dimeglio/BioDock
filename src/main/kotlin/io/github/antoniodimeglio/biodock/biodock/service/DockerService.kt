package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.ContainerInfo
import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.antoniodimeglio.biodock.biodock.util.CommandExecutor
import io.github.antoniodimeglio.biodock.biodock.util.DefaultCommandExecutor
import io.github.antoniodimeglio.biodock.biodock.util.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import kotlin.collections.emptyList
import kotlin.io.path.Path


/**
 * Service responsible for all Docker container operations in BioDock.
 *
 * This service provides a high-level interface for Docker operations including:
 * - Docker installation and runtime status checking
 * - Building Docker images from pipeline Dockerfiles
 * - Running containerized pipelines with proper volume mounting
 * - Managing container lifecycle and cleanup
 *
 * ## Volume Mounting Strategy
 * The service uses a standardized mounting approach:
 * - Host input directory → `/data` inside container
 * - Host output directory → `/results` inside container
 *
 * ## Error Handling
 * All methods return `Result<T>` for consistent error handling:
 * ```kotlin
 * when (val result = dockerService.runPipeline(pipeline, input, output)) {
 *     is Result.Success -> println("Success: ${result.data}")
 *     is Result.Error -> println("Error: ${result.message}")
 * }
 * ```
 *
 * @property commandExecutor Abstraction for executing system commands, allows testing with mocks
 * @constructor Creates a DockerService with optional command executor for testing
 *
 * @see CommandExecutor For system command execution interface
 * @see Pipeline For pipeline configuration structure
 * @see Result For error handling pattern
 *
 * @since 1.0.0
 * @author BioDock Development Team
 */
class DockerService(private val commandExecutor: CommandExecutor = DefaultCommandExecutor()) {
    private val logger = KotlinLogging.logger {}

    /**
     * Checks the current status of Docker installation and daemon.
     *
     * Performs a two-step verification:
     * 1. Checks if Docker is installed by running `docker --version`
     * 2. Checks if Docker daemon is running by running `docker info`
     *
     * @return [DockerStatus] indicating the current state:
     *   - [DockerStatus.RUNNING] - Docker is installed and daemon is running
     *   - [DockerStatus.NOT_RUNNING] - Docker is installed but daemon is not running
     *   - [DockerStatus.NOT_INSTALLED] - Docker is not installed
     *   - [DockerStatus.ERROR] - Error occurred during status check
     *
     * @see DockerStatus For detailed status enum documentation
     */
    suspend fun getDockerStatus(): DockerStatus {
        return try {
            val versionStatus = commandExecutor.execute("docker", "--version")
            if (versionStatus.exitCode != 0) {
                return DockerStatus.NOT_INSTALLED
            }

            val infoStatus = commandExecutor.execute("docker", "info")

            if (infoStatus.exitCode == 0) {
                DockerStatus.RUNNING
            } else {
                DockerStatus.NOT_RUNNING
            }
        } catch (e: Exception) {
            logger.warn { "Error checking Docker status: ${e.message}" }
            DockerStatus.ERROR
        }
    }

    suspend fun buildDockerImage(pipeline: Pipeline): Result<String> {
        val dockerFilePath = Path("pipelines").resolve(pipeline.id)
        val buildResult = this.commandExecutor.execute(
            "docker", "build", "-t", "biodock/${pipeline.id}", "${dockerFilePath}/."
        )

        return if (buildResult.exitCode != 0){
            Result.Error("Failed to build docker image: ${buildResult.error}")
        } else {
            Result.Success("biodock/${pipeline.id}", "Successfully built docker image: ${pipeline.id}")
        }
    }

    suspend fun removeDockerImage(pipeline: Pipeline): Result<String> {
        val removeResult = this.commandExecutor.execute(
            "docker", "rmi", "biodock/${pipeline.id}"
        )

        return if (removeResult.exitCode != 0){
            Result.Error("Failed to remove docker image: ${removeResult.error}")
        } else {
            Result.Success("biodock/${pipeline.id}", "Successfully removed docker image: ${pipeline.id}")
        }
    }

    suspend fun runPipeline(
        pipeline: Pipeline,
        hostInputPath: String,
        hostOutputPath: String): Result<String> {
        val expandedCommand = expandGlobs(pipeline.command, hostInputPath)

        val runResult = this.commandExecutor.execute(
            "docker", "run", "--rm",
            "-v", "$hostInputPath:/data",
            "-v", "$hostOutputPath:/results",
            "--name", pipeline.id,
            "biodock/${pipeline.id}",
            *expandedCommand.toTypedArray()
        )

        return if (runResult.exitCode != 0 ) {
            Result.Error("Failed to run pipeline: ${runResult.error}")
        } else {
            Result.Success(hostOutputPath, "Successfully ran pipeline: ${pipeline.name}")
        }
    }

    suspend fun fetchContainerStatus(pipeline: Pipeline): Result<ContainerInfo> {
        val statusResult = this.commandExecutor.execute(
            "docker", "ps", "-a", "--format",
            "{{.ID}}\t{{.Image}}\t{{.Command}}\t{{.CreatedAt}}\t{{.Status}}\t{{.Ports}}\t{{.Names}}",
            "--filter", "name=${pipeline.id}"
        )

        if (statusResult.exitCode != 0 || statusResult.output.isBlank()) {
            return Result.Error("Failed to fetch container status: ${statusResult.error}")
        }

        val parts = statusResult.output.trim().split("\t")
        val containerInfo = ContainerInfo(
            containerId = parts.getOrElse(0) { "" },
            image = parts.getOrElse(1) { "" },
            command = parts.getOrElse(2) { "" },
            created = parts.getOrElse(3) { "" },
            status = parts.getOrElse(4) { "" },
            ports = parts.getOrElse(5) { "" },
            names = parts.getOrElse(6) { "" }
        )
        return Result.Success(containerInfo, "Successfully fetched container status")
    }

    internal fun expandGlobs(command: List<String>, hostInputPath: String): List<String> {
        return command.flatMap { arg ->
            if (arg.contains("*")) {
                expandSingleGlob(arg, hostInputPath)
            } else {
                listOf(arg)
            }
        }
    }

    internal fun expandSingleGlob(globPattern: String, hostInputPath: String): List<String> {
        val parts = globPattern.split("/")
        val pattern = parts.last()
        val prefix = parts.dropLast(1).joinToString("/")

        if (pattern.contains("*")) {
            val regex = pattern.replace("*", ".*").toRegex()

            val matchingFiles = File(hostInputPath).listFiles()
                ?.filter { regex.matches(it.name) }
                ?.map { if (prefix.isNotEmpty()) "$prefix/${it.name}" else it.name }
                ?: emptyList()

            return matchingFiles.ifEmpty { listOf(globPattern) }
        }

        return listOf(globPattern)
    }
}


/**
 * Represents the current status of Docker installation and daemon.
 *
 * This enum is used by [DockerService.getDockerStatus] to indicate whether
 * Docker is properly installed and running, allowing the UI to provide
 * appropriate feedback to users.
 *
 * @see DockerService.getDockerStatus
 */
enum class DockerStatus {
    /** Docker is not installed on the system */
    NOT_INSTALLED,

    /** Docker is installed but the daemon is not running */
    NOT_RUNNING,

    /** Docker is installed and the daemon is running (ready for use) */
    RUNNING,

    /** An error occurred while checking Docker status */
    ERROR
}
