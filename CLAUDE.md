# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BioDock is a JavaFX desktop application built with Kotlin that provides a graphical interface for running bioinformatics pipelines on FASTQ files using Docker containers. The application is designed for wet lab biologists who need to run quality control and analysis workflows without command-line expertise.

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
- `PipelineService.kt` - Manages available pipelines and their execution
- `DockerService.kt` - Docker container operations (currently minimal implementation)
- `FileService.kt` - File validation and project persistence
- `ReportService.kt` - Result report generation and display

#### Models (`src/main/kotlin/.../model/`)
- `Pipeline.kt` - Pipeline configuration with command, input/output specifications
- `Project.kt` - Project state containing samples and settings
- `Sample.kt` - Individual FASTQ file metadata
- `AppState.kt` - Application-wide state management

### Pipeline System
Pipelines are defined as JSON configurations in `src/main/resources/pipelines/` with corresponding Docker configurations. Each pipeline specifies:
- Input/output file types and directories
- Docker commands to execute
- Metadata for UI display

### UI Architecture
JavaFX application using FXML layouts:
- `main-view.fxml` - Primary application interface with drag-and-drop sample loading
- `project-view.fxml` - Project creation dialog
- `pipelinecreator-view.fxml` - Custom pipeline definition interface

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

### File Handling
- FASTQ file validation through `FileService.validateFastqFile()`
- Project persistence managed by `FileService.saveProjectDirectory()`
- Drag-and-drop functionality integrated in main UI

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
- Basic UI and project management implemented
- FastQC pipeline configuration present
- Docker service skeleton exists but needs implementation
- File validation and drag-and-drop functional
- Report viewing and result management in progress