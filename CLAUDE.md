# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BioDock is a JavaFX desktop application built with Kotlin that provides a standardized platform for running containerized bioinformatics pipelines. The application enables researchers to package their workflows as Docker containers with JSON configuration metadata, and provides wet lab biologists with a simple drag-and-drop interface to execute these pipelines without dealing with dependencies, environment setup, or command-line complexity.

### Core Concept
The application follows a "pipeline-as-a-package" approach where:
- **Researchers/Pipeline Authors** provide a Dockerfile and JSON configuration defining their workflow
- **End Users** simply upload input files, select a pipeline, and receive processed results
- **BioDock** handles all containerization, execution, and result management transparently

## Build and Development Commands

### Building the Application
```bash
# Clean and compile
./mvnw clean compile

# Run the application
./mvnw javafx:run

# Package the application
./mvnw clean package
```

### Testing
```bash
# Run tests
./mvnw test

# Run tests with verbose output
./mvnw test -Dtest.verbose=true
```

### Development
```bash
# Clean build artifacts
./mvnw clean

# Validate project structure
./mvnw validate
```

## Architecture Overview

### Core Structure
- **Main Application**: `BioDockApplication.kt` - JavaFX application entry point
- **Controllers**: MVC pattern with FXML controllers handling UI interactions
- **Services**: Business logic layer for file handling, Docker operations, and pipeline management
- **Models**: Data classes representing core entities (Project, Pipeline, Sample, etc.)
- **Resources**: FXML layouts, CSS styles, and pipeline configurations

### Key Components

#### Controllers (`src/main/kotlin/.../controller/`)
- `MainController.kt` - Primary UI controller managing the main application window
- `ProjectController.kt` - Handles project creation and management
- `PipelineCreatorController.kt` - Custom pipeline definition interface

#### Services (`src/main/kotlin/.../service/`)
- `PipelineService.kt` - Pipeline lifecycle management including loading, validation, and execution orchestration
- `DockerService.kt` - Docker container operations for building images and running containerized pipelines
- `FileService.kt` - File validation, project persistence, and input/output management
- `ReportService.kt` - Analysis result processing and report generation

#### Models (`src/main/kotlin/.../model/`)
- `Pipeline.kt` - Standardized pipeline configuration including Docker commands, file type specifications, and metadata
- `Project.kt` - Analysis project state containing input samples, selected pipelines, and execution settings
- `Sample.kt` - Individual input file metadata and processing status
- `AppState.kt` - Application-wide state management for pipeline library and user preferences

### Pipeline System
BioDock supports standardized pipeline packages consisting of two components:

#### 1. Pipeline JSON Configuration
Pipelines are defined as JSON configurations that specify:
- **Metadata**: ID, name, description, version
- **Input/Output Specifications**: Accepted file types and directory mappings
- **Execution Details**: Docker commands and container configuration
- **UI Display Information**: User-friendly names and descriptions

Example pipeline structure:
```json
{
  "id": "fastqc-v0.12.1",
  "name": "FastQC Quality Control",
  "description": "Quality control checks on raw sequence data",
  "version": "0.12.1",
  "command": ["fastqc", "--outdir", "/output", "/input/*"],
  "inputFileTypes": ["fastq", "fq", "fastq.gz"],
  "outputFileTypes": ["html", "zip"],
  "inputDirectory": "/input",
  "outputDirectory": "/output"
}
```

#### 2. Docker Container
Each pipeline includes a Dockerfile or references a Docker image that:
- Contains all necessary bioinformatics tools and dependencies
- Provides a standardized execution environment
- Ensures reproducible results across different systems
- Isolates pipeline execution from the host system

#### Pipeline Integration Workflow
1. **Pipeline Creation**: Researchers use the Pipeline Creator interface to define Dockerfile and JSON configuration
2. **Pipeline Registration**: Completed pipelines are added to the application's pipeline library
3. **User Execution**: End users select pipelines, provide input files, and execute workflows
4. **Result Management**: BioDock manages container execution and presents results through the UI

### UI Architecture
JavaFX application using FXML layouts with multiple user workflows:

#### Primary User Interface
- `main-view.fxml` - Main application window with drag-and-drop file loading, pipeline selection, and execution management
- `project-view.fxml` - Project creation and management dialog for organizing analysis sessions

#### Pipeline Author Interface  
- `pipelinecreator-view.fxml` - Comprehensive pipeline definition interface allowing researchers to:
  - Load and edit Dockerfile content
  - Configure pipeline metadata and parameters
  - Define input/output file type specifications
  - Preview and validate pipeline configurations
  - Export complete pipeline packages for distribution

## Development Guidelines

### Package Structure
All code uses the base package: `io.github.antoniodimeglio.biodock.biodock`

### Logging
Uses `kotlin-logging` with KotlinLogging. Initialize loggers as:
```kotlin
private val logger = KotlinLogging.logger {}
```

### Serialization
Uses `kotlinx-serialization` for JSON handling. Models requiring serialization are marked with `@Serializable`.

### Pipeline Management
- Pipeline package validation (JSON + Dockerfile integrity)
- Dynamic pipeline loading and registration
- Pipeline execution through Docker container orchestration
- Input/output file type validation and mapping

### File Handling  
- Support for multiple bioinformatics file formats (FASTQ, FASTA, BAM, etc.)
- Project persistence and session management
- Drag-and-drop functionality for intuitive file loading
- Automated result organization and retrieval

### Error Handling
UI errors displayed via JavaFX Alert dialogs through `MainController.showErrorDialog()`.

### Coroutines
Async operations use `kotlinx-coroutines-core` for non-blocking execution.

## Technology Stack
- **Language**: Kotlin 2.1.20
- **UI Framework**: JavaFX 21.0.5
- **Build Tool**: Maven
- **Testing**: JUnit 5.12.1
- **Logging**: kotlin-logging + slf4j-simple
- **Serialization**: kotlinx-serialization-json
- **Containerization**: Docker (for pipeline execution)

## Current Implementation Status
Based on recent commits and code structure:

### Completed Components
- Core JavaFX application architecture and UI framework
- Project management system with drag-and-drop file loading
- Pipeline data model with JSON serialization support
- Basic pipeline configuration structure (FastQC example)
- Pipeline Creator interface scaffolding for Dockerfile integration

### In Progress
- Docker service implementation for container management
- Pipeline validation and execution engine
- Result processing and report generation system
- Complete Pipeline Creator workflow (Dockerfile loading, validation, export)

### Planned Features
- Pipeline library management and sharing
- Advanced file format support beyond FASTQ
- Pipeline versioning and dependency management
- Batch processing capabilities for multiple samples
- Integration with external pipeline repositories

## Future Vision (Post v1.0.0)

### Centralized Pipeline Repository
Long-term vision includes developing a centralized pipeline registry that would transform BioDock into a complete bioinformatics workflow ecosystem:

#### Repository Features
- **Searchable Pipeline Catalog**: Organized by categories (QC, assembly, annotation, variant calling, etc.)
- **Community-Driven**: User ratings, reviews, and usage statistics for pipeline discovery
- **Quality Assurance**: Automated validation and testing of submitted pipeline packages
- **Version Management**: Semantic versioning with dependency tracking and compatibility checking
- **One-Click Integration**: Direct download and installation into BioDock's local pipeline library

#### Benefits for the Community
- **Discoverability**: Eliminate the need to scour literature and GitHub for reliable workflows
- **Reproducibility**: Standardized, tested pipeline packages ensure consistent results
- **Collaboration**: Researchers can easily share and build upon each other's workflows
- **Trust**: Curated repository with validation reduces risk of using unreliable pipelines
- **Documentation**: Centralized location for pipeline documentation and usage examples

This centralized approach would position BioDock as a comprehensive solution for the entire bioinformatics workflow lifecycle, from pipeline discovery to execution and result analysis.
- Remember that I am a junior software developer, and that the current project, namely BioDock, is a portfolio project. Therefore, you should act as if to provide guidance to me, without necessarily giving me out directly the solution to something but to instead give me useful tips and to guide me through things, unless I esplicitally ask for help and even then you should make sure that I really need it.