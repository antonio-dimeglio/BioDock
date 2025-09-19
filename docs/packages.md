# Module BioDock

BioDock is a JavaFX desktop application built with Kotlin that provides a standardized platform for running containerized bioinformatics pipelines.

## Overview

The application enables researchers to package their workflows as Docker containers with JSON configuration metadata, and provides wet lab biologists with a simple drag-and-drop interface to execute these pipelines without dealing with dependencies, environment setup, or command-line complexity.

## Core Architecture

### Package Structure

* **`controller`** - JavaFX controllers implementing the MVC pattern for UI interactions
* **`model`** - Data classes representing core entities (Project, Pipeline, Sample, etc.)
* **`service`** - Business logic layer for file handling, Docker operations, and pipeline management
* **`util`** - Utility classes for command execution, serialization, and string generation
* **`config`** - Configuration classes for application and pipeline settings

### Key Design Patterns

* **MVC Pattern** - Controllers handle UI, Services contain business logic, Models represent data
* **Result Pattern** - Consistent error handling across all service operations
* **Repository Pattern** - FileService manages data persistence and validation
* **Command Pattern** - CommandExecutor abstracts system command execution

### Technology Stack

* **UI Framework**: JavaFX 21.0.5 with FXML layouts
* **Language**: Kotlin 2.1.20 with coroutines for async operations
* **Serialization**: kotlinx-serialization for JSON handling
* **Logging**: kotlin-logging with KotlinLogging
* **Containerization**: Docker integration for pipeline execution
* **Build Tool**: Maven with custom plugins for JavaFX and documentation

## Quick Start

1. **Load Input Files** - Drag and drop FASTQ files or select input folder
2. **Select Pipeline** - Choose from available containerized workflows
3. **Run Analysis** - Execute pipeline in isolated Docker container
4. **View Results** - Access outputs and generated reports

## Developer Guide

See individual package documentation for detailed API information and implementation examples.