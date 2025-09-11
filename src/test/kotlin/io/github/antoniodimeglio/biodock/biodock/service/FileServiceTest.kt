package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Project
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.zip.GZIPOutputStream

class FileServiceTest {
    val fileService = FileService()


    @Test
    fun `Valid fastq files should be parsed correctly`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `Valid gzipped fastq should be parsed correctly`() {
        val tempFile = Files.createTempFile("test", ".fastq.gz").toFile()
        GZIPOutputStream(tempFile.outputStream()).use {
            it.write("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent().toByteArray())
        }
        tempFile.deleteOnExit()

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Success)
    }

    @Test
    fun `Valid fastq should have at least 4 lines`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)
        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Invalid FastQ format, file contains less than 4 lines.", errorResult.message)
    }

    @Test
    fun `Valid fastq should have valid header`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Invalid header line: must start with '@'", errorResult.message)
    }

    @Test
    fun `Valid fastq should have valid separator`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          -
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Invalid separator line: must start with '+'", errorResult.message)
    }

    @Test
    fun `Valid fastq should not have empty sequence line`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
 
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Sequence or quality line is empty", errorResult.message)
    }

    @Test
    fun `Valid fastq should not have empty quality line`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          +
          
          -
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Sequence or quality line is empty", errorResult.message)
    }

    @Test
    fun `Valid fastq should have sequence and quality lines of same length`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAAATAGTAAATCCATTTGTTCAACTCACAGTTT
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC6
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Sequence and quality lines have different lengths", errorResult.message)
    }

    @Test
    fun `Valid fastq should have only valid nucleotide characters`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCArATAGTAAATCCATTTGTTCAACTCACAGTTT
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF>>>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Invalid nucleotide characters in sequence", errorResult.message)
    }

    @Test
    fun `Valid fastq should have only valid score characters`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        tempFile.writeText("""
          @SEQ_ID
          GATTTGGGGTTCAAAGCAGTATCGATCAATAGTAAATCCATTTGTTCAACTCACAGTTTT
          +
          !''*((((***+))%%%++)(%%%%).1***-+*''))**55CCF >>>>>CCCCCCC65
      """.trimIndent())

        val result = fileService.validateFastqFile(tempFile)

        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("Invalid quality score characters", errorResult.message)
    }

    @Test
    fun `Fastq file should exist`() {
        val file = File("")

        val result = fileService.validateFastqFile(file)
        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("File does not exist", errorResult.message)
    }

    @Test
    fun `Fastq file should not be empty`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        val result = fileService.validateFastqFile(tempFile)
        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("File is empty", errorResult.message)
    }

    @Test
    fun `Fastq file should have valid extension`() {
        val tempFile = Files.createTempFile("test", ".txt").toFile()
        tempFile.deleteOnExit()
        tempFile.writeText("FooBar")

        val result = fileService.validateFastqFile(tempFile)
        assertTrue(result is ValidationResult.Error)
        val errorResult = result as ValidationResult.Error
        assertEquals("File has incorrect extension", errorResult.message)
    }

    @Test
    fun `Fastq file sample name should not contain extension`() {
        val tempFile = File("test.fastq")
        tempFile.deleteOnExit()

        val result = fileService.extractSampleNameFromFilename(tempFile)
        val expected = "test"

        assertEquals(expected, result)
    }

    @Test
    fun `saveProjectDirectory should create directory and save project json`() {
        val tempDir = Files.createTempDirectory("test-project").toFile()
        val project = Project(name = "TestProject", workingDirectory = tempDir)

        fileService.saveProjectDirectory(project)

        assertTrue(tempDir.exists())
        val jsonFile = File(tempDir, "project.json")
        assertTrue(jsonFile.exists())

        val savedJson = jsonFile.readText()
        assertTrue(savedJson.contains("TestProject"))

        tempDir.deleteRecursively()
    }

    @Test
    fun `saveProjectDirectory should overwrite existing project json`() {
        val tempDir = Files.createTempDirectory("test-project").toFile()
        val project = Project(name = "Original", workingDirectory = tempDir)

        fileService.saveProjectDirectory(project)

        project.name = "Modified"
        fileService.saveProjectDirectory(project)

        val jsonFile = File(tempDir, "project.json")
        val content = jsonFile.readText()
        assertTrue(content.contains("Modified"))
        assertFalse(content.contains("Original"))

        tempDir.deleteRecursively()
    }

    @Test
    fun `saveProjectDirectory should create nested directories`() {
        val deepPath = Files.createTempDirectory("test").resolve("level1/level2/level3").toFile()
        val project = Project(name = "Deep", workingDirectory = deepPath)

        fileService.saveProjectDirectory(project)

        assertTrue(deepPath.exists())
        assertTrue(File(deepPath, "project.json").exists())

        deepPath.deleteRecursively()
    }

    @Test
    fun `copyFileToProject should copy file to project directory`() {
        val sourceFile = Files.createTempFile("source", ".fastq").toFile()
        sourceFile.writeText("test content")

        val projectDir = Files.createTempDirectory("project").toFile()
        val project = Project(name = "Test", workingDirectory = projectDir)

        // Test
        val copiedFile = fileService.copyFileToProject(sourceFile, project)

        // Verify
        assertTrue(copiedFile.exists())
        assertEquals("test content", copiedFile.readText())
        assertEquals(projectDir, copiedFile.parentFile)

        // Cleanup
        sourceFile.delete()
        projectDir.deleteRecursively()
    }

    @Test
    fun `copyFileToProject should throw FileNotFoundException for missing source`() {
        val nonExistentFile = File("does-not-exist.fastq")
        val project = Project(name = "Test", workingDirectory = Files.createTempDirectory("project").toFile())

        assertThrows<FileNotFoundException> {
            fileService.copyFileToProject(nonExistentFile, project)
        }
    }

    @Test
    fun `copyFileToProject should throw FileNotFoundException for missing project directory`() {
        val sourceFile = Files.createTempFile("source", ".fastq").toFile()
        val project = Project(name = "Test", workingDirectory = File("non-existent-dir"))

        assertThrows<FileNotFoundException> {
            fileService.copyFileToProject(sourceFile, project)
        }

        sourceFile.delete()
    }

    @Test
    fun `cleanupProject should delete project directory and all contents`() {
        val tempDir = Files.createTempDirectory("test-project").toFile()
        val project = Project(name = "TestProject", workingDirectory = tempDir)

        File(tempDir, "project.json").writeText("test project data")
        File(tempDir, "sample1.fastq").writeText("sample data")
        val subDir = File(tempDir, "results")
        subDir.mkdir()
        File(subDir, "report.html").writeText("report content")

        assertTrue(tempDir.exists())
        assertTrue(File(tempDir, "project.json").exists())
        assertTrue(subDir.exists())

        fileService.cleanupProject(project)

        assertFalse(tempDir.exists())
        assertFalse(File(tempDir, "project.json").exists())
        assertFalse(subDir.exists())
    }

    @Test
    fun `cleanupProject should delete empty project directory`() {
        val tempDir = Files.createTempDirectory("empty-project").toFile()
        val project = Project(name = "EmptyProject", workingDirectory = tempDir)

        assertTrue(tempDir.exists())

        fileService.cleanupProject(project)

        assertFalse(tempDir.exists())
    }

    @Test
    fun `cleanupProject should throw FileNotFoundException for non-existent directory`() {
        val nonExistentDir = File("does-not-exist-${System.currentTimeMillis()}")
        val project = Project(name = "NonExistent", workingDirectory = nonExistentDir)

        val exception = assertThrows<FileNotFoundException> {
            fileService.cleanupProject(project)
        }

        assertTrue(exception.message!!.contains("Could not find file"))
        assertTrue(exception.message!!.contains(nonExistentDir.name))
    }

    @Test
    fun `cleanupProject should handle nested directory structures`() {
        val tempDir = Files.createTempDirectory("nested-project").toFile()
        val project = Project(name = "NestedProject", workingDirectory = tempDir)

        val deep = File(tempDir, "level1/level2/level3")
        deep.mkdirs()
        File(deep, "deep-file.txt").writeText("deep content")
        File(tempDir, "root-file.txt").writeText("root content")

        assertTrue(tempDir.exists())
        assertTrue(deep.exists())

        fileService.cleanupProject(project)

        assertFalse(tempDir.exists())
        assertFalse(deep.exists())
    }

    @Test
    fun `cleanupProject should handle directory with many files`() {
        val tempDir = Files.createTempDirectory("many-files-project").toFile()
        val project = Project(name = "ManyFiles", workingDirectory = tempDir)

        repeat(50) { i ->
            File(tempDir, "file$i.txt").writeText("content $i")
        }

        assertEquals(50, tempDir.listFiles()?.size)


        fileService.cleanupProject(project)
        assertFalse(tempDir.exists())
    }

    @Test
    fun `cleanupProject should work after saveProjectDirectory`() {
        val tempDir = Files.createTempDirectory("integration-test").toFile()
        val project = Project(name = "IntegrationTest", workingDirectory = tempDir)

        fileService.saveProjectDirectory(project)
        assertTrue(File(tempDir, "project.json").exists())

        fileService.cleanupProject(project)

        assertFalse(tempDir.exists())
    }
}