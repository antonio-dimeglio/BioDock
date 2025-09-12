package io.github.antoniodimeglio.biodock.biodock.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DockerServiceTest {

    @Test
    fun test() {
        val ds = DockerService().getDockerStatus()
    }
}