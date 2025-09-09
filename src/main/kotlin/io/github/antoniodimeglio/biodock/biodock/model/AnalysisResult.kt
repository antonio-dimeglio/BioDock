package io.github.antoniodimeglio.biodock.biodock.model

import java.io.File
import java.time.Duration
import java.time.LocalDateTime

data class AnalysisResult(
    val sampleId: String,
    val pipeline: String,
    val status: SampleStatus,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val outputFiles: List<File> = emptyList(),
    val logFile: File? = null,
    val htmlReport: File? = null,
    val summary: Map<String, Any> = emptyMap(),
    val errorMessage: String? = null
) {
    val duration: Long?
        get() = if (endTime != null) {
            Duration.between(startTime, endTime).toMillis()
        } else null

    val isSuccess: Boolean
        get() = status == SampleStatus.COMPLETED && errorMessage  == null

    fun getDurationFormatted(): String {
        return duration?.let { millis ->
            val seconds = millis / 1000
            val minutes = seconds / 60
            val hours = minutes / 60

            when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        } ?: "N/A"
    }
}