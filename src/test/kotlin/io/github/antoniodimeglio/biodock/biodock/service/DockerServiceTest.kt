package io.github.antoniodimeglio.biodock.biodock.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DockerServiceTest {

    @Test
    fun test() = runTest {
        val ds = DockerService().getDockerStatus()
    }
}