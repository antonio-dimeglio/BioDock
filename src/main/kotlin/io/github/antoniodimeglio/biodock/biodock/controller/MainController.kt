package io.github.antoniodimeglio.biodock.biodock.controller


import io.github.antoniodimeglio.biodock.biodock.model.Project
import io.github.antoniodimeglio.biodock.biodock.service.DockerService
import io.github.antoniodimeglio.biodock.biodock.service.FileService
import io.github.antoniodimeglio.biodock.biodock.service.PipelineService
import io.github.antoniodimeglio.biodock.biodock.service.ValidationResult
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import javafx.stage.Stage
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.input.KeyCombination
import javafx.scene.input.TransferMode
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger {}

class MainController : Initializable {

    lateinit var borderPane: BorderPane
    lateinit var newProjectButton: Button

    // Top toolbar
    @FXML private lateinit var pipelineSelector: ComboBox<String>
    @FXML private lateinit var openFileBtn: Button
    @FXML private lateinit var runBtn: Button
    @FXML private lateinit var statusLabel: Label

    // Left sidebar
    @FXML private lateinit var projectNameLabel: Label
    @FXML private lateinit var sampleListView: ListView<String>

    // Center tabs
    @FXML private lateinit var tabPane: TabPane
    @FXML private lateinit var progressBar: ProgressBar
    @FXML private lateinit var progressLabel: Label
    @FXML private lateinit var sampleCountLabel: Label
    @FXML private lateinit var overallStatusLabel: Label
    @FXML private lateinit var selectedPipelineLabel: Label
    @FXML private lateinit var resultsTable: TableView<*>
    @FXML private lateinit var reportView: WebView
    @FXML private lateinit var logArea: TextArea

    // Bottom status bar
    @FXML private lateinit var dockerStatusLabel: Label
    @FXML private lateinit var versionLabel: Label
    @FXML private lateinit var timestampLabel: Label

    private val selectedFiles = mutableListOf<File>()
    private var currentProject = Project(
        name = "NewProject",
    )
    private val uiScope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
    private val dockerService = DockerService()

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        setupUI()
        setupEventHandlers()
        updateUI()
    }

    private fun setupUI() {
        val availablePipelines = PipelineService.getAvailablePipelines()

        pipelineSelector.items.addAll(
            availablePipelines.map { it.name }
        )
        pipelineSelector.selectionModel.selectFirst()

        projectNameLabel.text = "New Project"
        statusLabel.text = "Ready"
        overallStatusLabel.text = "Ready"
        selectedPipelineLabel.text = pipelineSelector.value
        versionLabel.text = "BioDock v1.0.0"

        progressBar.progress = 0.0
        progressLabel.text = ""

        logArea.appendText("BioDock started successfully\n")
        logArea.appendText("Ready to process samples\n")

        sampleListView.selectionModel.selectionMode = SelectionMode.MULTIPLE
    }

    private fun updateDockerStatus() {
        uiScope.launch {
            val status = withContext(Dispatchers.IO) { dockerService.getDockerStatus() }
            Platform.runLater { dockerStatusLabel.text = "Docker: $status" }

        }
    }

    private fun setupEventHandlers() {
        pipelineSelector.setOnAction {
            selectedPipelineLabel.text = pipelineSelector.value
            logger.info { "Pipeline selected: ${pipelineSelector.value}" }
        }

        runBtn.isDisable = true

        sampleListView.setOnDragOver { event ->
            if (event.dragboard.hasFiles()) {
                event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
            }
            event.consume()
        }

        sampleListView.setOnDragDropped { event ->
            val db = event.dragboard
            var success = false

            if (db.hasFiles()) {
                db.files.forEach { file ->
                    if (!selectedFiles.contains(file) &&
                        FileService.validateFastqFile(file) is ValidationResult.Success){
                        selectedFiles.add(file)
                    }
                }

                success = true
            }
            updateUI()
            event.isDropCompleted = success
            event.consume()
        }

        Platform.runLater {
            val scene = borderPane.scene ?: return@runLater

            val osName = System.getProperty("os.name").lowercase()
            val saveKey = if (osName.contains("mac")) {
                KeyCombination.keyCombination("Shortcut+S")
            } else {
                KeyCombination.keyCombination("Ctrl+S")
            }

            scene.accelerators[saveKey] = Runnable { saveProject() }
        }
    }

    private fun updateUI() {
        sampleCountLabel.text = selectedFiles.size.toString()
        runBtn.isDisable = selectedFiles.isEmpty()


        sampleListView.items.clear()
        sampleListView.items.addAll(selectedFiles.map { it.name })
        updateDockerStatus()
    }

    private fun getFilesFromDialog(): List<File>? {
        val fileChooser = FileChooser().apply {
            title = "Select FASTQ Files"
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("FASTQ Files", "*.fastq", "*.fq", "*.fastq.gz", "*.fq.gz"),
                FileChooser.ExtensionFilter("All Files", "*.*")
            )
        }

        val stage = openFileBtn.scene.window as Stage
        val files = fileChooser.showOpenMultipleDialog(stage)

        return files
    }

    private fun showErrorDialog(description: String) {
        val alert = Alert(Alert.AlertType.ERROR).apply {
            this.title = "Error"
            headerText = title
            contentText = description
        }
        alert.showAndWait()
    }

    private fun simulateProgress() {
        val timeline = Timeline(
            KeyFrame(Duration.seconds(1.0), EventHandler<ActionEvent> {
                progressBar.progress += 0.2
                progressLabel.text = "Processing sample ${(progressBar.progress * selectedFiles.size).toInt() + 1}..."

                if (progressBar.progress >= 1.0) {
                    statusLabel.text = "Complete"
                    overallStatusLabel.text = "Analysis Complete"
                    progressLabel.text = "Done"
                    logArea.appendText("Analysis completed successfully!\n")
                }
            })).apply {
            cycleCount = 5
        }
        timeline.play()
    }

    private fun loadProject(project: Project) {
        currentProject = project
        projectNameLabel.text = currentProject.name

        if (currentProject.selectedPipeline?.isNotEmpty() == true){
            pipelineSelector.value = currentProject.selectedPipeline
        }
    }

    @FXML private fun saveProject(){
        try {
            val wd = currentProject.workingDirectory.path

            if (Files.exists(Paths.get(wd))) {
                val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
                    title = "Overwriting Project"
                    headerText = "Overwrite Project Directory?"
                    contentText = "Are you sure you want to overwrite the working directory for the project?"
                }

                val result = alert.showAndWait()
                if (result.orElse(ButtonType.CANCEL) != ButtonType.OK) {
                    return
                }
            }

            FileService.saveProjectDirectory(currentProject)

        } catch (e: Exception) {
            showErrorDialog("Error when trying to save the project: ${e.message}")
        }
    }
    @FXML private fun newProject() {
        try {
            val loader = FXMLLoader(javaClass.getResource("/fxml/project-view.fxml"))
            val dialogPane = loader.load<DialogPane>()

            val controller = loader.getController<ProjectController>()
            controller.setDialogPane(dialogPane)

            val dialog = Dialog<ButtonType>().apply {
                title = "Create New Project"
                initModality(Modality.WINDOW_MODAL)
                initOwner(projectNameLabel.scene.window)
            }
            dialog.dialogPane = dialogPane

            val result = dialog.showAndWait()
                if (result.isPresent && result.get() == ButtonType.OK) {
                    val project = controller.getProject()
                    project?.let { project ->
                        loadProject(project)
                    }
                }
        } catch (e: Exception) {
            logger.error { e }
            showErrorDialog("Failed to load new project dialog: ${e.message}")
        }
    }

    @FXML private fun addSample(){
        val files = getFilesFromDialog()

        if (files != null) {
            selectedFiles.addAll(
                files.filter { FileService.validateFastqFile(it) is ValidationResult.Success  &&
                    !selectedFiles.contains(it)}
            )

            updateUI()
            logArea.appendText("Added ${files.size} files\n")
        }
    }
    @FXML private fun removeSample() {
        val selected = sampleListView.selectionModel.selectedItems.toList()

        if (selected.isEmpty()) return

        val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Confirm Deletion"
            headerText = "Remove selected samples?"
            contentText = "Are you sure you want to remove ${selected.size} sample(s)?"
        }

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            sampleListView.items.removeAll(selected)
            sampleListView.selectionModel.clearSelection()

            selected.forEach { item ->
                val fileToRemove = selectedFiles.find { it.name == item }
                if (fileToRemove != null) {
                    selectedFiles.remove(fileToRemove)
                }
            }
        }
    }
    @FXML private fun clearSamples() {
        val alert = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Confirm Deletion"
            headerText = "Remove all samples?"
            contentText = "Are you sure you want to remove all samples?"
        }

        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            sampleListView.items.clear()
            sampleListView.selectionModel.clearSelection()

            selectedFiles.clear()
        }
    }

    @FXML private fun importSampleSheet() { logger.info { "Import sample sheet - TODO" } }
    @FXML private fun exportResults() { logger.info { "Export results - TODO" } }
    @FXML private fun refreshResults() { logger.info { "Refresh results - TODO" } }
    @FXML private fun exportAllResults() { logger.info { "Export all results - TODO" } }
    @FXML private fun openExternalReport() { logger.info { "Open external report - TODO" } }
    @FXML private fun clearLogs() { logArea.clear() }
    @FXML private fun saveLogs() { logger.info { "Save logs - TODO" } }

    @FXML
    private fun handleRunFastqc() {
        logger.info { "Starting FastQC analysis..." }

        statusLabel.text = "Running..."
        overallStatusLabel.text = "Running Analysis"
        progressBar.progress = 0.2
        progressLabel.text = "Initializing..."

        logArea.appendText("Starting ${pipelineSelector.value} analysis...\n")
        logArea.appendText("Processing ${selectedFiles.size} samples\n")

        // TODO: Implement actual FastQC execution in Phase 3
        // For now, just simulate progress
        simulateProgress()
    }
}