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

    suspend fun runPipeline(
        pipeline: Pipeline,
        hostInputPath: String,
        hostOutputPath: String,
        buildImage: Boolean = false): Result {

        if (buildImage){
            val dockerFilePath = Path("pipelines").resolve(pipeline.id)
            val buildResult = this.commandExecutor.execute(
                "docker", "build", "-t", "biodock/${pipeline.id}", "${dockerFilePath}/."
            )

            if (buildResult.exitCode != 0){
                return Result.Error("Failed to build docker image: ${buildResult.error}")
            }
        }

        val expandedCommand = expandGlobs(pipeline.command, hostInputPath)

        val runResult = this.commandExecutor.execute(
            "docker", "run", "--rm",
            "-v", "$hostInputPath:/data",
            "-v", "$hostOutputPath:/results",
            "biodock/${pipeline.id}",
            *expandedCommand.toTypedArray()
        )

        return if (runResult.exitCode != 0 ) {
            Result.Error("Failed to run pipeline: ${runResult.error}")
        } else {
            Result.Success("Successfully ran pipeline: ${pipeline.name}")
        }
    }

    suspend fun fetchContainerStatus(pipeline: Pipeline): ContainerInfo {
        TODO()
    }

    private fun expandGlobs(command: List<String>, hostInputPath: String): List<String> {
        return command.flatMap { arg ->
            if (arg.contains("*")) {
                expandSingleGlob(arg, hostInputPath)
            } else {
                listOf(arg)
            }
        }
    }

    private fun expandSingleGlob(globPattern: String, hostInputPath: String): List<String> {
        // Extract the directory and pattern from the glob
        // e.g., "/data/*.fastq" -> directory="/data", pattern="*.fastq"
        val parts = globPattern.split("/")
        val pattern = parts.last()
        val prefix = parts.dropLast(1).joinToString("/")

        if (pattern.contains("*")) {
            // Convert glob pattern to regex
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
