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


class DockerService(private val commandExecutor: CommandExecutor = DefaultCommandExecutor()) {
    private val logger = KotlinLogging.logger {}

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


enum class DockerStatus {
    NOT_INSTALLED,
    NOT_RUNNING,
    RUNNING,
    ERROR
}
