package io.github.antoniodimeglio.biodock.biodock.service

import io.github.antoniodimeglio.biodock.biodock.model.Project
import io.github.antoniodimeglio.biodock.biodock.util.Result
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.util.zip.GZIPOutputStream

class FileServiceTest {
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Success)
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Success)
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

        val result = FileService.validateFastqFile(tempFile)
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
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

        val result = FileService.validateFastqFile(tempFile)

        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("Invalid quality score characters", errorResult.message)
    }

    @Test
    fun `Fastq file should exist`() {
        val file = File("")

        val result = FileService.validateFastqFile(file)
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("File does not exist", errorResult.message)
    }

    @Test
    fun `Fastq file should not be empty`() {
        val tempFile = Files.createTempFile("test", ".fastq").toFile()
        tempFile.deleteOnExit()

        val result = FileService.validateFastqFile(tempFile)
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("File is empty", errorResult.message)
    }

    @Test
    fun `Fastq file should have valid extension`() {
        val tempFile = Files.createTempFile("test", ".txt").toFile()
        tempFile.deleteOnExit()
        tempFile.writeText("FooBar")

        val result = FileService.validateFastqFile(tempFile)
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("File has incorrect extension", errorResult.message)
    }

    @Test
    fun `Fastq file sample name should not contain extension`() {
        val tempFile = File("test.fastq")
        tempFile.deleteOnExit()

        val result = FileService.extractSampleNameFromFilename(tempFile)
        val expected = "test"

        assertEquals(expected, result)
    }

    @Test
    fun `saveProjectDirectory should create directory and save project json`() {
        val tempDir = Files.createTempDirectory("test-project").toFile()
        val project = Project(name = "TestProject", workingDirectory = tempDir)

        FileService.saveProjectDirectory(project)

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

        FileService.saveProjectDirectory(project)

        project.name = "Modified"
        FileService.saveProjectDirectory(project)

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

        FileService.saveProjectDirectory(project)

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

        val copiedFile = FileService.copyFileToProject(sourceFile, project)

        assertTrue(copiedFile.exists())
        assertEquals("test content", copiedFile.readText())
        assertEquals(projectDir, copiedFile.parentFile)

        sourceFile.delete()
        projectDir.deleteRecursively()
    }

    @Test
    fun `copyFileToProject should throw FileNotFoundException for missing source`() {
        val nonExistentFile = File("does-not-exist.fastq")
        val project = Project(name = "Test", workingDirectory = Files.createTempDirectory("project").toFile())

        assertThrows<FileNotFoundException> {
            FileService.copyFileToProject(nonExistentFile, project)
        }
    }

    @Test
    fun `copyFileToProject should throw FileNotFoundException for missing project directory`() {
        val sourceFile = Files.createTempFile("source", ".fastq").toFile()
        val project = Project(name = "Test", workingDirectory = File("non-existent-dir"))

        assertThrows<FileNotFoundException> {
            FileService.copyFileToProject(sourceFile, project)
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

        FileService.cleanupProject(project)

        assertFalse(tempDir.exists())
        assertFalse(File(tempDir, "project.json").exists())
        assertFalse(subDir.exists())
    }

    @Test
    fun `cleanupProject should delete empty project directory`() {
        val tempDir = Files.createTempDirectory("empty-project").toFile()
        val project = Project(name = "EmptyProject", workingDirectory = tempDir)

        assertTrue(tempDir.exists())

        FileService.cleanupProject(project)

        assertFalse(tempDir.exists())
    }

    @Test
    fun `cleanupProject should throw FileNotFoundException for non-existent directory`() {
        val nonExistentDir = File("does-not-exist-${System.currentTimeMillis()}")
        val project = Project(name = "NonExistent", workingDirectory = nonExistentDir)

        val exception = assertThrows<FileNotFoundException> {
            FileService.cleanupProject(project)
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

        FileService.cleanupProject(project)

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


        FileService.cleanupProject(project)
        assertFalse(tempDir.exists())
    }

    @Test
    fun `cleanupProject should work after saveProjectDirectory`() {
        val tempDir = Files.createTempDirectory("integration-test").toFile()
        val project = Project(name = "IntegrationTest", workingDirectory = tempDir)

        FileService.saveProjectDirectory(project)
        assertTrue(File(tempDir, "project.json").exists())

        FileService.cleanupProject(project)

        assertFalse(tempDir.exists())
    }


    @Test
    fun `listSubdirectories should return empty list for non-existent directory`() {
        val nonExistentPath = "non-existent-directory-${System.currentTimeMillis()}"

        val result = FileService.listSubdirectories(nonExistentPath)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `listSubdirectories should return empty list for file path`() {
        val tempFile = Files.createTempFile("test", ".txt").toFile()
        tempFile.deleteOnExit()

        val result = FileService.listSubdirectories(tempFile.absolutePath)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `listSubdirectories should return empty list for empty directory`() {
        val tempDir = Files.createTempDirectory("empty-dir").toFile()
        tempDir.deleteOnExit()

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `listSubdirectories should return only directories, not files`() {
        val tempDir = Files.createTempDirectory("test-dir").toFile()
        tempDir.deleteOnExit()

        // Create files and directories
        File(tempDir, "file1.txt").writeText("content")
        File(tempDir, "file2.log").writeText("content")
        val subDir1 = File(tempDir, "subdir1")
        val subDir2 = File(tempDir, "subdir2")
        subDir1.mkdir()
        subDir2.mkdir()

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "subdir1" })
        assertTrue(result.any { it.name == "subdir2" })
        assertFalse(result.any { it.name == "file1.txt" })
        assertFalse(result.any { it.name == "file2.log" })
    }

    @Test
    fun `listSubdirectories should return single subdirectory`() {
        val tempDir = Files.createTempDirectory("single-sub").toFile()
        tempDir.deleteOnExit()

        val subDir = File(tempDir, "onlySubdir")
        subDir.mkdir()

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(1, result.size)
        assertEquals("onlySubdir", result[0].name)
        assertTrue(result[0].isDirectory)
    }

    @Test
    fun `listSubdirectories should return multiple subdirectories`() {
        val tempDir = Files.createTempDirectory("multi-sub").toFile()
        tempDir.deleteOnExit()

        val expectedDirs = listOf("alpha", "beta", "gamma", "delta")
        expectedDirs.forEach { dirName ->
            File(tempDir, dirName).mkdir()
        }

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(4, result.size)
        val resultNames = result.map { it.name }.sorted()
        assertEquals(expectedDirs.sorted(), resultNames)
        assertTrue(result.all { it.isDirectory })
    }

    @Test
    fun `listSubdirectories should not return nested subdirectories`() {
        val tempDir = Files.createTempDirectory("nested-test").toFile()
        tempDir.deleteOnExit()

        val level1 = File(tempDir, "level1")
        level1.mkdir()
        val level2 = File(level1, "level2")
        level2.mkdir()
        val level3 = File(level2, "level3")
        level3.mkdir()

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(1, result.size)
        assertEquals("level1", result[0].name)
        assertFalse(result.any { it.name == "level2" })
        assertFalse(result.any { it.name == "level3" })
    }

    @Test
    fun `listSubdirectories should handle directories with special characters`() {
        val tempDir = Files.createTempDirectory("special-chars").toFile()
        tempDir.deleteOnExit()

        val specialDirs = listOf("dir with spaces", "dir-with-dashes", "dir_with_underscores")
        specialDirs.forEach { dirName ->
            File(tempDir, dirName).mkdir()
        }

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(3, result.size)
        val resultNames = result.map { it.name }.sorted()
        assertEquals(specialDirs.sorted(), resultNames)
    }

    @Test
    fun `listSubdirectories should handle mixed content directory`() {
        val tempDir = Files.createTempDirectory("mixed-content").toFile()
        tempDir.deleteOnExit()

        File(tempDir, "readme.txt").writeText("readme")
        File(tempDir, "config.json").writeText("{}")
        val docsDir = File(tempDir, "docs")
        docsDir.mkdir()
        val srcDir = File(tempDir, "src")
        srcDir.mkdir()
        val testDir = File(tempDir, "test")
        testDir.mkdir()

        File(docsDir, "manual.pdf").writeText("manual")
        File(srcDir, "main.kt").writeText("code")

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(3, result.size)
        val dirNames = result.map { it.name }.sorted()
        assertEquals(listOf("docs", "src", "test"), dirNames)
        assertTrue(result.all { it.isDirectory })
    }

    @Test
    fun `listSubdirectories should return File objects with correct properties`() {
        val tempDir = Files.createTempDirectory("properties-test").toFile()
        tempDir.deleteOnExit()

        val subDir = File(tempDir, "testSubdir")
        subDir.mkdir()

        val result = FileService.listSubdirectories(tempDir.absolutePath)

        assertEquals(1, result.size)
        val returnedFile = result[0]
        assertTrue(returnedFile.exists())
        assertTrue(returnedFile.isDirectory())
        assertFalse(returnedFile.isFile())
        assertEquals("testSubdir", returnedFile.name)
        assertEquals(tempDir, returnedFile.parentFile)
    }
}