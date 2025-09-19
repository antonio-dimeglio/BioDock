package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Pipeline
import io.github.antoniodimeglio.biodock.biodock.model.Project
import io.github.antoniodimeglio.biodock.biodock.util.Result
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

    private fun isFastqFormatValid(file: File): Result<String> {
        return try {
            val reader = if (file.name.endsWith(".gz", ignoreCase = true)) {
                GZIPInputStream(file.inputStream()).bufferedReader()
            } else {
                file.bufferedReader()
            }

            reader.use { r ->
                val lines = r.lineSequence().take(4).toList()
                if (lines.size < 4) return Result.Error("Invalid FastQ format, file contains less than 4 lines.")

                // If every other check passes, the final step makes sure that
                // the data in the file actually follows the fastq definition
                // i.e. only nucleotides are present and that only ASCII values
                // in the range [33, 126] are

                val validNucleotides = setOf('A', 'T', 'G', 'C', 'N', 'a', 't', 'g', 'c', 'n')

                return when {
                    !lines[0].startsWith("@") ->
                        Result.Error("Invalid header line: must start with '@'")

                    !lines[2].startsWith("+") ->
                        Result.Error("Invalid separator line: must start with '+'")

                    lines[1].isEmpty() || lines[3].isEmpty() ->
                        Result.Error("Sequence or quality line is empty")

                    lines[1].length != lines[3].length ->
                        Result.Error("Sequence and quality lines have different lengths")

                    !lines[1].all { it in validNucleotides } ->
                        Result.Error("Invalid nucleotide characters in sequence")

                    !lines[3].all { it.code in 33..126 } ->
                        Result.Error("Invalid quality score characters")

                    else -> Result.Success(file.absolutePath, "Valid FASTQ format")
                }
            }
        } catch (e: Exception) {
            Result.Error("Failed to read Fastq file: ${e.message}", e)
        }
    }

    fun validateFileFormat(file: File, pipeline: Pipeline): Result<String> {
        // TODO: Refactor code to map expected format to function that checks if file format is valid.
        return when {
            !file.exists() -> Result.Error("File does not exist")
            !file.canRead() -> Result.Error("Cannot read file")
            file.length() == 0L -> Result.Error("File is empty")
            !file.name.matches(Regex(".*\\.(fastq|fq)(\\.gz)?$")) ->
                Result.Error("File has incorrect extension")

            else -> isFastqFormatValid(file)
        }
    }

    fun validateFastqFile(file: File): Result<String> {
        return when {
            !file.exists() -> Result.Error("File does not exist")
            !file.canRead() -> Result.Error("Cannot read file")
            file.length() == 0L -> Result.Error("File is empty")
            !file.name.matches(Regex(".*\\.(fastq|fq)(\\.gz)?$")) ->
                Result.Error("File has incorrect extension")
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

    fun createLink(from: String, to: String){
        Files.createLink(Paths.get(to), Paths.get(from))
    }
}
