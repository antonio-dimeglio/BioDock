package io.github.antoniodimeglio.biodock.biodock.model

import io.github.antoniodimeglio.biodock.biodock.util.FileSerializer
import io.github.antoniodimeglio.biodock.biodock.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class Project(
    val id: String = UUID.randomUUID().toString(),
    var name: String,
    var description: String = "",
    @Serializable(with = LocalDateSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateSerializer::class)
    var lastModified: LocalDateTime = LocalDateTime.now(),
    val samples: MutableList<Sample> = mutableListOf(),
    @Serializable(with = FileSerializer::class)
    var workingDirectory: File = File("/BioDockProjects/NewProject/"),
    var selectedPipeline: String = "",
) {
    fun addSample(sample: Sample) {
        samples.add(sample)
        lastModified = LocalDateTime.now()
    }

    fun removeSample(sampleId: String) {
        samples.removeIf { it.id == sampleId}
        lastModified = LocalDateTime.now()
    }

    fun getSampleById(id: String): Sample? = samples.find { it.id == id }

    fun getCompletedSamples(): List<Sample> =
        samples.filter { it.status == SampleStatus.COMPLETED }

    fun getPendingSamples(): List<Sample> =
        samples.filter { it.status == SampleStatus.PENDING }

    fun getRunningSamples(): List<Sample> =
        samples.filter { it.status == SampleStatus.RUNNING }

    fun getFailedSamples(): List<Sample> =
        samples.filter { it.status == SampleStatus.FAILED }

    fun getOverallStatus(): String {
        return when {
            samples.isEmpty() -> "Empty"
            getRunningSamples().isNotEmpty() -> "Running"
            getFailedSamples().isNotEmpty() -> "Some Failed"
            getPendingSamples().isNotEmpty() -> "Ready"
            else -> "Complete"
        }
    }
}