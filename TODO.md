# BioDock Implementation TODO 🔧
*Concrete implementation tasks to complete core functionality*

## 🚨 Critical Implementation Gaps (Phase 1)
*These need to be completed for basic functionality to work*

### 1. Pipeline Model Completion
**File: `src/main/kotlin/.../model/Pipeline.kt`**
- [ ] **Add missing fields to Pipeline data class**:
  ```kotlin
  val outputFileTypes: List<String>,
  val inputDirectory: String = "/data",
  val outputDirectory: String = "/results",
  val dockerImage: String? = null,
  val requirements: Map<String, String> = emptyMap()
  ```
- [ ] **Update existing pipeline JSON files** to include new fields
- [ ] **Test Pipeline serialization/deserialization** with new fields

### 2. MainController - Core Pipeline Execution
**File: `src/main/kotlin/.../controller/MainController.kt`**

#### 2.1 Critical Missing Methods
- [ ] **Implement `runPipeline()` method (lines 379-382)**:
  ```kotlin
  private fun runPipeline() {
      // 1. Validate selected pipeline exists
      // 2. Validate input samples exist and are valid
      // 3. Create output directory
      // 4. Call DockerService.buildDockerImage() if needed
      // 5. Call DockerService.runPipeline() with paths
      // 6. Update UI with progress/results
      // 7. Handle errors with user dialogs
  }
  ```

- [ ] **Replace `handleRunFastqc()` simulation with real execution**:
  ```kotlin
  // Remove simulateProgress() call
  // Add actual pipeline execution logic
  // Use coroutines for async execution
  ```

#### 2.2 Service Integration (Currently Missing)
- [ ] **Add service instances to MainController**:
  ```kotlin
  private val dockerService = DockerService()
  private val fileService = FileService()
  private val pipelineService = PipelineService()
  private val reportService = ReportService()
  ```

- [ ] **Implement async pipeline execution with coroutines**:
  ```kotlin
  private fun executeAsyncPipeline(pipeline: Pipeline, samples: List<Sample>, outputPath: String) {
      scope.launch {
          // Docker build + run logic here
          // Update UI on Platform.runLater
      }
  }
  ```

#### 2.3 Error Handling Integration
- [ ] **Add Result<T> handling for all service calls**:
  ```kotlin
  when (val result = dockerService.runPipeline(...)) {
      is Result.Success -> // Update UI with success
      is Result.Error -> showErrorDialog(result.message)
  }
  ```

### 3. File Validation System
**File: `src/main/kotlin/.../service/FileService.kt`**

- [ ] **Replace hardcoded FASTQ validation (line 66 TODO)**:
  ```kotlin
  fun validateFileFormat(file: File, allowedTypes: List<String>): Boolean {
      return allowedTypes.any { type -> validateSpecificFormat(file, type) }
  }

  private fun validateSpecificFormat(file: File, type: String): Boolean {
      return when (type.lowercase()) {
          "fastq", "fq" -> validateFastq(file)
          "fasta", "fa" -> validateFasta(file)
          "bam" -> validateBam(file)
          else -> false
      }
  }
  ```

- [ ] **Implement missing format validators**:
  ```kotlin
  private fun validateFasta(file: File): Boolean
  private fun validateBam(file: File): Boolean
  ```

- [ ] **Update MainController drag-and-drop to use dynamic validation**:
  ```kotlin
  // Replace hardcoded FASTQ check with:
  val selectedPipeline = pipelineSelector.selectionModel.selectedItem
  if (selectedPipeline != null) {
      fileService.validateFileFormat(file, selectedPipeline.inputFileTypes)
  }
  ```

### 4. Results Management Implementation
**File: `src/main/kotlin/.../service/ReportService.kt`**

- [ ] **Implement core ReportService methods**:
  ```kotlin
  fun parseOutputFiles(outputDirectory: File, pipeline: Pipeline): AnalysisResult {
      // Scan output directory for files matching pipeline.outputFileTypes
      // Create AnalysisResult with file paths and metadata
  }

  fun generateSummaryReport(analysisResult: AnalysisResult): File {
      // Generate HTML summary from analysis outputs
  }
  ```

- [ ] **Connect results to UI in MainController**:
  ```kotlin
  private fun updateResultsTable() {
      // Populate resultsTable with completed analyses
  }

  private fun loadReportInViewer(reportFile: File) {
      // Load HTML report into reportWebView
  }
  ```

## 🔧 Essential Features (Phase 2)
*Complete after Phase 1 for full user workflow*

### 5. PipelineCreatorController Implementation
**File: `src/main/kotlin/.../controller/PipelineCreatorController.kt`**

- [ ] **`onLoadDockerfile()` - File loading**:
  ```kotlin
  private fun onLoadDockerfile() {
      val fileChooser = FileChooser()
      fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Dockerfile", "Dockerfile", "*"))
      val file = fileChooser.showOpenDialog(stage)
      if (file != null) {
          dockerfileContent.text = file.readText()
      }
  }
  ```

- [ ] **`onSave()` - Pipeline creation**:
  ```kotlin
  private fun onSave() {
      val pipeline = Pipeline(
          id = pipelineIdField.text,
          name = pipelineNameField.text,
          // ... other fields from form
      )
      pipelineService.savePipeline(pipeline, dockerfileContent.text)
  }
  ```

- [ ] **Form validation methods**:
  ```kotlin
  private fun validateForm(): Boolean
  private fun showValidationErrors(errors: List<String>)
  ```

### 6. Docker Integration Improvements
**File: `src/main/kotlin/.../service/DockerService.kt`**

- [ ] **Fix volume mounting to use Pipeline fields**:
  ```kotlin
  suspend fun runPipeline(pipeline: Pipeline, hostInputPath: String, hostOutputPath: String): Result<String> {
      val runResult = commandExecutor.execute(
          "docker", "run", "--rm",
          "-v", "$hostInputPath:${pipeline.inputDirectory}",
          "-v", "$hostOutputPath:${pipeline.outputDirectory}",
          // ...
      )
  }
  ```

- [ ] **Add container cleanup**:
  ```kotlin
  suspend fun cleanupContainer(pipeline: Pipeline): Result<String> {
      // Remove stopped containers
  }
  ```

- [ ] **Add real-time log streaming**:
  ```kotlin
  suspend fun runPipelineWithLogs(
      pipeline: Pipeline,
      inputPath: String,
      outputPath: String,
      logCallback: (String) -> Unit
  ): Result<String>
  ```

### 7. Project Management Completion
**File: `src/main/kotlin/.../service/FileService.kt`**

- [ ] **Implement `loadProject()`**:
  ```kotlin
  fun loadProject(projectFile: File): Result<Project> {
      // Deserialize project JSON
      // Validate project structure
      // Return Result with project or error
  }
  ```

- [ ] **Add project validation**:
  ```kotlin
  fun validateProject(project: Project): List<String> {
      // Check if input files still exist
      // Validate pipeline references
      // Return list of validation errors
  }
  ```

## 🎯 Integration Tasks (Phase 3)
*Connect all components for seamless user experience*

### 8. UI State Management
- [ ] **Integrate AppState throughout application**:
  ```kotlin
  // MainController should update AppState on significant events
  // Use AppState for consistent status across UI components
  ```

- [ ] **Add progress tracking for long operations**:
  ```kotlin
  // Show progress during Docker build/run
  // Estimate time remaining
  // Allow cancellation
  ```

### 9. Complete Missing UI Handlers
**File: `src/main/kotlin/.../controller/MainController.kt`**

- [ ] **`importSampleSheet()`** - Parse CSV/TSV into Sample objects
- [ ] **`exportResults()`** - Compress outputs for sharing
- [ ] **`refreshResults()`** - Update status from Docker containers
- [ ] **`openExternalReport()`** - Open HTML reports in browser
- [ ] **`saveLogs()`** - Export execution logs

### 10. Error Handling & User Experience
- [ ] **Comprehensive error dialogs**:
  ```kotlin
  private fun showDetailedError(title: String, message: String, details: String)
  ```

- [ ] **Input validation before execution**:
  ```kotlin
  private fun validateBeforeRun(): List<String> {
      // Check Docker status
      // Validate selected pipeline
      // Check input files exist
      // Verify output directory is writable
  }
  ```

- [ ] **Status indicators throughout UI**:
  ```kotlin
  // Docker status indicator
  // Pipeline execution status
  // File validation status
  ```

## 🧪 Testing Implementation Tasks
- [ ] **Unit tests for DockerService methods**
- [ ] **Integration tests for pipeline execution flow**
- [ ] **UI tests for file drag-and-drop**
- [ ] **Error scenario testing (Docker not running, invalid files)**

## 📋 Implementation Notes

### Quick Start Recommendation:
1. **Start with Pipeline model completion** - Everything else depends on this
2. **Implement `runPipeline()` method** - This is the core functionality
3. **Add service integration to MainController** - Connect UI to backend
4. **Test with a simple pipeline** - Verify end-to-end flow works

### Development Tips:
- **Test each method independently** before integrating
- **Use Result<T> pattern consistently** for error handling
- **Add logging to track execution flow** during development
- **Keep UI responsive** with coroutines for long operations

### Architecture Decisions:
- **Pipeline model is the contract** between UI and Docker execution
- **Result<T> pattern provides consistent error handling**
- **Services are stateless** - MainController manages UI state
- **Coroutines handle async operations** without blocking UI

---
*This TODO focuses on concrete implementation gaps that prevent core functionality from working. Complete Phase 1 for basic pipeline execution capability.*