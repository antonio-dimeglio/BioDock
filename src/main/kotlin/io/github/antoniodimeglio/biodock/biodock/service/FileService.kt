package io.github.antoniodimeglio.biodock.biodock.service

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.GZIPInputStream

class FileService {
    private val logger = KotlinLogging.logger {}

    private fun isFastqFormatValid(file: File): Boolean {
        return try {
            val reader = if (file.name.endsWith(".gz", ignoreCase = true)) {
                GZIPInputStream(file.inputStream()).bufferedReader()
            } else {
                file.bufferedReader()
            }

            val lines = reader.lineSequence().take(4).toList()
            reader.close()

            if (lines.size < 4) return false

            lines[0].startsWith("@") &&
                    lines[2].startsWith("+") &&
                    lines[1].isNotEmpty() &&
                    lines[3].isNotEmpty()

        } catch (e: Exception) {
            false
        }
    }

    fun validateFastqFile(file: File): ValidationResult {
        return when {
            !file.exists() -> ValidationResult.Error("File does not exist")
            !file.canRead() -> ValidationResult.Error("Cannot read file")
            file.length() == 0L -> ValidationResult.Error("File is empty")
            !file.name.matches(Regex(".*\\.(fastq|fq)(\\.gz)?$")) ->
                ValidationResult.Error("Not a FASTQ file")
            !isFastqFormatValid(file) -> ValidationResult.Error("FASTQ file formatting is invalid")
            else -> ValidationResult.Success("Valid FASTQ file")
        }
    }

    fun extractSampleNameFromFilename(file: File): String {
        return file.nameWithoutExtension
    }

    fun createWorkspaceDirectory(projectName: String){}

    fun copyFileToWorkspace(source: File, workspace: File): File {
        if (!source.exists())
            throw FileNotFoundException("Could not find file ${source.name}")

        if (!workspace.exists())
            throw FileNotFoundException("Could not find workspace ${workspace.name}")

        return source.copyTo(workspace)
    }

    fun cleanupWorkspace(workspaceDir: File){}

}

sealed class ValidationResult {
    data class Success(val message: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}