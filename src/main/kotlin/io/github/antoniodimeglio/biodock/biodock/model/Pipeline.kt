package io.github.antoniodimeglio.biodock.biodock.model
import kotlinx.serialization.Serializable


/**
 * Represents a containerized bioinformatics pipeline package.
 * A Pipeline defines a standardized bioinformatics workflow that can be executed in a Docker container.
 * Each pipeline consists of a JSON configuration (this class) and an associated Dockerfile containing
 * the necessary tools and dependencies.
 *
 * ## Usage Example:
 *  ```kotlin
 *     val fastQCPipeline = Pipeline(
 *         id = "fastqc-v0.12.1",
 *         name = "FastQC Quality Control",
 *         description = "Quality control checks on raw sequence data",
 *         version = "0.12.1",
 *         command = listOf("fastqc", "--outdir", "/results", "/data/`*.fastq"),
 *         inputFileTypes = listOf("fastq", "fq", "fastq.gz")
 *     )
 *     // Execute the pipeline
 *     val result = dockerService.runPipeline(fastqcPipeline, inputPath, outputPath)
 *  ```
 *  @property id Unique identifier for the pipeline, typically includes version (e.g., "fastqc-v0.12.1")
 *  @property name Human-readable name displayed in the UI (e.g., "FastQC Quality Control")
 *  @property description Brief explanation of what the pipeline does and its purpose
 *  @property command Docker command to execute inside the container. Use `/data` for input files and `/results` for outputs
 *  @property version Semantic version string for pipeline versioning and compatibility tracking
 *  @property inputFileTypes List of accepted file extensions (without dots) like ["fastq", "fq", "fastq.gz"]
 *  @see io.github.antoniodimeglio.biodock.biodock.service.DockerService.runPipeline For pipeline execution
 *  @see io.github.antoniodimeglio.biodock.biodock.service.PipelineService.loadPipeline For loading pipeline configurations
 *  @see io.github.antoniodimeglio.biodock.biodock.service.FileService.validateFileFormat For input file validation
 *
 *  @since 1.0.0
 *  @author BioDock Development Team
 */
@Serializable
data class Pipeline(
val id: String,
val name: String,
val description: String,
val command: List<String>,
val version: String,
val inputFileTypes: List<String>)




