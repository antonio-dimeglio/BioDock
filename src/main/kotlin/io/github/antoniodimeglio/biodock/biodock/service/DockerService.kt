package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.ContainerInfo
import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.antoniodimeglio.biodock.biodock.util.CommandExecutor
import io.github.antoniodimeglio.biodock.biodock.util.DefaultCommandExecutor
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.Logger


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
        hostOutputPath: String): Boolean {

        TODO()
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
