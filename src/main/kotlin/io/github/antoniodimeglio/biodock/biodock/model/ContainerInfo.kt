package io.github.antoniodimeglio.biodock.biodock.model

data class ContainerInfo(
    val containerId: String,
    val image: String,
    val command: String,
    val created: String,
    val status: String,
    val ports: String,
    val names: String
)
