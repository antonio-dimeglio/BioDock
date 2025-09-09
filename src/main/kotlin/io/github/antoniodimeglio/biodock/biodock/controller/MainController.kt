package io.github.antoniodimeglio.biodock.biodock.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import javafx.stage.Stage
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.util.Duration
import java.io.File
import java.net.URL
import java.util.*

private val logger = KotlinLogging.logger {}

class MainController : Initializable {

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

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        logger.info { "Initializing MainController..." }

        setupUI()
        setupEventHandlers()
        updateUI()
    }

    private fun setupUI() {
        // Setup pipeline selector
        pipelineSelector.items.addAll(
            "FastQC Only",
            "RNA-seq Quick QC",
            "Variant Calling QC"
        )
        pipelineSelector.selectionModel.selectFirst()

        projectNameLabel.text = "New Project"
        statusLabel.text = "Ready"
        overallStatusLabel.text = "Ready"
        selectedPipelineLabel.text = pipelineSelector.value
        dockerStatusLabel.text = "Docker: Checking..."
        versionLabel.text = "BioDock v1.0.0"

        progressBar.progress = 0.0
        progressLabel.text = ""

        logArea.appendText("BioDock started successfully\n")
        logArea.appendText("Ready to process samples\n")
    }

    private fun setupEventHandlers() {
        pipelineSelector.setOnAction {
            selectedPipelineLabel.text = pipelineSelector.value
            logger.info { "Pipeline selected: ${pipelineSelector.value}" }
        }

        runBtn.isDisable = true
    }

    private fun updateUI() {
        sampleCountLabel.text = selectedFiles.size.toString()
        runBtn.isDisable = selectedFiles.isEmpty()

        // Update sample list
        sampleListView.items.clear()
        sampleListView.items.addAll(selectedFiles.map { it.name })
    }

    @FXML
    private fun handleOpenFile() {
        logger.info { "Opening file chooser..." }

        val fileChooser = FileChooser().apply {
            title = "Select FASTQ Files"
            extensionFilters.addAll(
                FileChooser.ExtensionFilter("FASTQ Files", "*.fastq", "*.fq", "*.fastq.gz", "*.fq.gz"),
                FileChooser.ExtensionFilter("All Files", "*.*")
            )
        }

        val stage = openFileBtn.scene.window as Stage
        val files = fileChooser.showOpenMultipleDialog(stage)

        if (files != null) {
            selectedFiles.addAll(files)
            updateUI()
            logArea.appendText("Added ${files.size} files\n")
            logger.info { "Selected ${files.size} files" }
        }
    }

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

    // Placeholder methods for future implementation
    @FXML private fun newProject() { logger.info { "New project - TODO" } }
    @FXML private fun removeSample() { logger.info { "Remove sample - TODO" } }
    @FXML private fun importSampleSheet() { logger.info { "Import sample sheet - TODO" } }
    @FXML private fun exportResults() { logger.info { "Export results - TODO" } }
    @FXML private fun refreshResults() { logger.info { "Refresh results - TODO" } }
    @FXML private fun exportAllResults() { logger.info { "Export all results - TODO" } }
    @FXML private fun openExternalReport() { logger.info { "Open external report - TODO" } }
    @FXML private fun clearLogs() { logArea.clear() }
    @FXML private fun saveLogs() { logger.info { "Save logs - TODO" } }
}