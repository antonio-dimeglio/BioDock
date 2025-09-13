package io.github.antoniodimeglio.biodock.biodock.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DockerServiceTest {
    @Test
    fun runPipeline() = runTest {
        val pipeline = PipelineService.getAvailablePipelines().first()
        val ds = DockerService().runPipeline(pipeline, "", "")
        println(ds)
    }
}