package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Project
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.zip.GZIPInputStream

object FileService {
    private val logger = KotlinLogging.logger {}

    private fun isFastqFormatValid(file: File): ValidationResult {
        return try {
            val reader = if (file.name.endsWith(".gz", ignoreCase = true)) {
                GZIPInputStream(file.inputStream()).bufferedReader()
            } else {
                file.bufferedReader()
            }

            reader.use { r ->
                val lines = r.lineSequence().take(4).toList()
                if (lines.size < 4) return ValidationResult.Error("Invalid FastQ format, file contains less than 4 lines.")

                // If every other check passes, the final step makes sure that
                // the data in the file actually follows the fastq definition
                // i.e. only nucleotides are present and that only ASCII values
                // in the range [33, 126] are

                val validNucleotides = setOf('A', 'T', 'G', 'C', 'N', 'a', 't', 'g', 'c', 'n')

                return when {
                    !lines[0].startsWith("@") ->
                        ValidationResult.Error("Invalid header line: must start with '@'")

                    !lines[2].startsWith("+") ->
                        ValidationResult.Error("Invalid separator line: must start with '+'")

                    lines[1].isEmpty() || lines[3].isEmpty() ->
                        ValidationResult.Error("Sequence or quality line is empty")

                    lines[1].length != lines[3].length ->
                        ValidationResult.Error("Sequence and quality lines have different lengths")

                    !lines[1].all { it in validNucleotides } ->
                        ValidationResult.Error("Invalid nucleotide characters in sequence")

                    !lines[3].all { it.code in 33..126 } ->
                        ValidationResult.Error("Invalid quality score characters")

                    else -> ValidationResult.Success("Valid FASTQ format")
                }
            }
        } catch (e: Exception) {
            ValidationResult.Error("Failed to read Fastq file: ${e.message}")
        }
    }

    fun validateFastqFile(file: File): ValidationResult {
        return when {
            !file.exists() -> ValidationResult.Error("File does not exist")
            !file.canRead() -> ValidationResult.Error("Cannot read file")
            file.length() == 0L -> ValidationResult.Error("File is empty")
            !file.name.matches(Regex(".*\\.(fastq|fq)(\\.gz)?$")) ->
                ValidationResult.Error("File has incorrect extension")
            else -> isFastqFormatValid(file)
        }
    }

    fun extractSampleNameFromFilename(file: File): String {
        return file.nameWithoutExtension
    }

    fun saveProjectDirectory(project: Project){
        val path = Paths.get(project.workingDirectory.path)
        Files.createDirectories(path)

        val json = Json.encodeToString(project)
        val filePath = path.resolve("project.json")

        Files.writeString(
            filePath,
            json,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    fun copyFileToProject(source: File, project: Project): File {
        if (!source.exists())
            throw FileNotFoundException("Could not find file ${source.name}")

        if (!project.workingDirectory.exists())
            throw FileNotFoundException("Could not find project ${project.workingDirectory.name}")

        val destFile = File(project.workingDirectory, source.name)
        return source.copyTo(destFile, overwrite = true)
    }

    fun cleanupProject(project: Project){
        val projectFolder = project.workingDirectory
        if (!projectFolder.exists())
            throw FileNotFoundException("Could not find file ${projectFolder.name}")

        projectFolder.deleteRecursively()
    }

     fun listSubdirectories(path: String): List<File> {
        val dir = Paths.get(path)
        if (!Files.isDirectory(dir)) return emptyList()

        return Files.list(dir).use { stream ->
            stream.filter { Files.isDirectory(it) }
                .map { it.toFile() }
                .toList()
        }
    }
}

sealed class ValidationResult {
    data class Success(val message: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}