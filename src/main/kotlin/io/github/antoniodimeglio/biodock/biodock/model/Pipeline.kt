package io.github.antoniodimeglio.biodock.biodock.model

import kotlinx.serialization.Serializable

@Serializable
data class Pipeline(
    val id: String,
    val name: String,
    val description: String,
    val command: List<String>,
    val version: String,
    val inputFileTypes: List<String>,
)