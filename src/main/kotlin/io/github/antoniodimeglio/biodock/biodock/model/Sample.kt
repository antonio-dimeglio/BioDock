package io.github.antoniodimeglio.biodock.biodock.model

import io.github.antoniodimeglio.biodock.biodock.service.FileService
import io.github.antoniodimeglio.biodock.biodock.service.ValidationResult
import io.github.antoniodimeglio.biodock.biodock.util.FileSerializer
import io.github.antoniodimeglio.biodock.biodock.util.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDateTime
import java.util.UUID.randomUUID

@Serializable
enum class SampleStatus(val displayName: String, val cssClass: String) {
    PENDING("Pending", "status-pending"),
    RUNNING("Running", "status-running"),
    COMPLETED("Completed", "status-success"),
    FAILED("Failed", "status-error"),
    CANCELLED("Cancelled", "status-cancelled")
}

@Serializable
data class Sample(
    val id: String = randomUUID().toString(),
    val name: String,
    @Serializable(with = FileSerializer::class)
    val file: File,
    val fileSize: Long = file.length(),
    @Serializable(with = LocalDateSerializer::class)
    val addedAt: LocalDateTime = LocalDateTime.now(),
    var status: SampleStatus = SampleStatus.PENDING,
    var analysisResults: AnalysisResult? = null,
    var isValid: Boolean = false,
    var validationMessage: String? = null
) {
    fun isValidFastq(): Boolean {
        return when (val result = FileService.validateFastqFile(file)){
            is ValidationResult.Success -> {
                isValid = true
                validationMessage = result.message
                true
            }
            is ValidationResult.Error -> {
                isValid = false
                validationMessage = result.message
                false
            }
        }
    }

    fun getDisplayName(): String {
        return if (name != file.name) "$name (${file.name})" else name
    }

    fun getSizeFormatted(): String {
        return when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            fileSize < 1024 * 1024 * 1024  -> "${fileSize / (1024 * 1024)} GB"
            else -> "${fileSize / (1024 * 1024 * 1024)} GB"
        }
    }
}