#!/bin/bash

# Simple documentation generator for BioDock
# Extracts KDoc comments and creates basic HTML documentation

echo "🔧 Generating BioDock Documentation..."

# Create docs output directory
mkdir -p target/docs
mkdir -p target/docs/css

# Create simple CSS
cat > target/docs/css/style.css << 'EOF'
body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    line-height: 1.6;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    background: #f8f9fa;
}
.header {
    background: #2c3e50;
    color: white;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 30px;
}
.package {
    background: white;
    padding: 20px;
    margin: 20px 0;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.class-name {
    color: #2c3e50;
    font-size: 1.5em;
    font-weight: bold;
    margin: 20px 0 10px 0;
}
.description {
    margin: 10px 0;
    color: #34495e;
}
.code {
    background: #f1f2f6;
    padding: 15px;
    border-radius: 4px;
    font-family: 'Monaco', 'Consolas', monospace;
    overflow-x: auto;
    margin: 10px 0;
}
.property {
    margin: 8px 0;
    padding-left: 20px;
}
.file-path {
    color: #7f8c8d;
    font-size: 0.9em;
    margin-bottom: 10px;
}
nav {
    background: white;
    padding: 15px;
    border-radius: 8px;
    margin-bottom: 20px;
}
nav a {
    color: #3498db;
    text-decoration: none;
    margin-right: 20px;
}
nav a:hover {
    text-decoration: underline;
}
EOF

# Generate main documentation page
cat > target/docs/index.html << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>BioDock API Documentation</title>
    <link rel="stylesheet" href="css/style.css">
    <meta charset="UTF-8">
</head>
<body>
    <div class="header">
        <h1>🧬 BioDock API Documentation</h1>
        <p>Comprehensive documentation for the BioDock bioinformatics pipeline platform</p>
    </div>

    <nav>
        <a href="#overview">Overview</a>
        <a href="#models">Models</a>
        <a href="#services">Services</a>
        <a href="#controllers">Controllers</a>
        <a href="#utilities">Utilities</a>
    </nav>

    <div id="overview" class="package">
        <h2>📋 Project Overview</h2>
        <div class="description">
            BioDock is a JavaFX desktop application built with Kotlin that provides a standardized platform
            for running containerized bioinformatics pipelines. The application enables researchers to package
            their workflows as Docker containers with JSON configuration metadata, and provides wet lab biologists
            with a simple drag-and-drop interface to execute these pipelines.
        </div>

        <h3>🏗️ Architecture</h3>
        <ul>
            <li><strong>Models</strong> - Data classes representing core entities (Pipeline, Project, Sample)</li>
            <li><strong>Services</strong> - Business logic layer for file handling, Docker operations, and pipeline management</li>
            <li><strong>Controllers</strong> - JavaFX controllers implementing the MVC pattern for UI interactions</li>
            <li><strong>Utilities</strong> - Helper classes for command execution, serialization, and string generation</li>
        </ul>
    </div>

    <div id="models" class="package">
        <h2>📊 Models Package</h2>
        <div class="file-path">📁 src/main/kotlin/.../model/</div>

        <div class="class-name">Pipeline</div>
        <div class="description">
            Represents a containerized bioinformatics pipeline package. A Pipeline defines a standardized
            bioinformatics workflow that can be executed in a Docker container.
        </div>
        <div class="code">
data class Pipeline(
    val id: String,              // Unique identifier (e.g., "fastqc-v0.12.1")
    val name: String,            // Human-readable name for UI
    val description: String,     // Brief explanation of purpose
    val command: List&lt;String&gt;,    // Docker command to execute
    val version: String,         // Semantic version string
    val inputFileTypes: List&lt;String&gt; // Accepted file extensions
)
        </div>

        <div class="class-name">Project</div>
        <div class="description">
            Analysis project state containing input samples, selected pipelines, and execution settings.
        </div>

        <div class="class-name">Sample</div>
        <div class="description">
            Individual input file metadata and processing status.
        </div>

        <div class="class-name">AppState</div>
        <div class="description">
            Application-wide state management for pipeline library and user preferences.
        </div>
    </div>

    <div id="services" class="package">
        <h2>⚙️ Services Package</h2>
        <div class="file-path">📁 src/main/kotlin/.../service/</div>

        <div class="class-name">DockerService</div>
        <div class="description">
            Service responsible for all Docker container operations in BioDock. Provides high-level interface
            for Docker operations including status checking, image building, and pipeline execution.
        </div>
        <div class="code">
// Key Methods:
suspend fun getDockerStatus(): DockerStatus
suspend fun buildDockerImage(pipeline: Pipeline): Result&lt;String&gt;
suspend fun runPipeline(pipeline: Pipeline, inputPath: String, outputPath: String): Result&lt;String&gt;
        </div>

        <div class="class-name">FileService</div>
        <div class="description">
            File validation, project persistence, and input/output management.
        </div>

        <div class="class-name">PipelineService</div>
        <div class="description">
            Pipeline lifecycle management including loading, validation, and execution orchestration.
        </div>

        <div class="class-name">ReportService</div>
        <div class="description">
            Analysis result processing and report generation.
        </div>
    </div>

    <div id="controllers" class="package">
        <h2>🎮 Controllers Package</h2>
        <div class="file-path">📁 src/main/kotlin/.../controller/</div>

        <div class="class-name">MainController</div>
        <div class="description">
            Primary UI controller managing the main application window. Handles drag-and-drop file loading,
            pipeline selection, and execution management.
        </div>

        <div class="class-name">ProjectController</div>
        <div class="description">
            Handles project creation and management dialogs for organizing analysis sessions.
        </div>

        <div class="class-name">PipelineCreatorController</div>
        <div class="description">
            Custom pipeline definition interface allowing researchers to load and edit Dockerfile content,
            configure pipeline metadata, and export complete pipeline packages.
        </div>
    </div>

    <div id="utilities" class="package">
        <h2>🔧 Utilities Package</h2>
        <div class="file-path">📁 src/main/kotlin/.../util/</div>

        <div class="class-name">CommandExecutor</div>
        <div class="description">
            Interface for executing system commands with async support.
        </div>

        <div class="class-name">Result&lt;T&gt;</div>
        <div class="description">
            Sealed class for consistent error handling across all service operations.
        </div>
        <div class="code">
sealed class Result&lt;out T&gt; {
    data class Success&lt;T&gt;(val data: T, val message: String = "") : Result&lt;T&gt;()
    data class Error(val message: String, val cause: Throwable? = null) : Result&lt;Nothing&gt;()
}
        </div>

        <div class="class-name">SerializationUtils</div>
        <div class="description">
            Utilities for JSON serialization and file handling.
        </div>
    </div>

    <div class="package">
        <h2>🚀 Getting Started</h2>
        <div class="description">
            <h3>Basic Usage Flow:</h3>
            <ol>
                <li><strong>Load Input Files</strong> - Drag and drop FASTQ files or select input folder</li>
                <li><strong>Select Pipeline</strong> - Choose from available containerized workflows</li>
                <li><strong>Run Analysis</strong> - Execute pipeline in isolated Docker container</li>
                <li><strong>View Results</strong> - Access outputs and generated reports</li>
            </ol>

            <h3>Development Commands:</h3>
            <div class="code">
# Compile and run
./mvnw clean compile
./mvnw javafx:run

# Generate documentation
./generate-docs.sh

# Run tests
./mvnw test
            </div>
        </div>
    </div>

    <footer style="text-align: center; margin-top: 40px; color: #7f8c8d;">
        <p>Generated on $(date) | BioDock Development Team</p>
    </footer>
</body>
</html>
EOF

echo "✅ Documentation generated successfully!"
echo "📖 Open: file://$(pwd)/target/docs/index.html"
echo ""
echo "🌐 To view in browser:"
echo "   open target/docs/index.html"
echo ""