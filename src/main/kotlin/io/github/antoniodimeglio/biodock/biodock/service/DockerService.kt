package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.ContainerInfo
import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.antoniodimeglio.biodock.biodock.util.CommandExecutor
import io.github.antoniodimeglio.biodock.biodock.util.DefaultCommandExecutor
import io.github.antoniodimeglio.biodock.biodock.util.Result
import io.github.oshai.kotlinlogging.KotlinLogging
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
        hostOutputPath: String): Result {
        val dockerFilePath = Path("pipelines").resolve(pipeline.id)

        val buildResult = this.commandExecutor.execute(
            "docker", "build", "-t", "biodock/${pipeline.id}", "${dockerFilePath}/."
        )

//        logger.info { buildResult }

        if (buildResult.exitCode != 0){
            return Result.Error("Failed to build docker image: ${buildResult.error}")
        }


        val runResult = this.commandExecutor.execute(
            "docker", "run", "--rm",
            "-v", "$hostInputPath:/data",
            "-v", "$hostOutputPath:/results",
            "biodock/${pipeline.id}",
            *pipeline.command.toTypedArray()
        )

        logger.info { pipeline.command }

        return if (runResult.exitCode != 0 ) {
            Result.Error("Failed to run pipeline: ${runResult.error}")
        } else {
            Result.Success("Successfully ran pipeline: ${pipeline.name}")
        }
    }

    suspend fun fetchContainerStatus(pipeline: Pipeline): ContainerInfo {
        TODO()
    }
}


enum class DockerStatus {
    NOT_INSTALLED,
    NOT_RUNNING,
    RUNNING,
    ERROR
}
