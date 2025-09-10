package io.github.antoniodimeglio.biodock.biodock.controller

import io.github.antoniodimeglio.biodock.biodock.model.Project
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.DialogPane
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.net.URL
import java.util.ResourceBundle

class ProjectController : Initializable {
    @FXML private lateinit var projectNameField: TextField
    @FXML private lateinit var descriptionArea: TextArea
    @FXML private lateinit var locationField: TextField
    @FXML private lateinit var browseButton: Button
    @FXML private lateinit var defaultPipelineCombo: ComboBox<String>
    @FXML private lateinit var sampleFormatCombo: ComboBox<String>
    @FXML private lateinit var createSampleSheetCheck: CheckBox
    @FXML private lateinit var enableLoggingCheck: CheckBox
    @FXML private lateinit var autoBackupCheck: CheckBox
    @FXML private lateinit var validationBox: VBox
    @FXML private lateinit var validationLabel: Label

    private var dialogPane: DialogPane? = null

    fun setDialogPane(dialogPane: DialogPane) {
        this.dialogPane = dialogPane
        setupDialogValidation()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        setupPipelineOptions()
        setupSampleFormatOptions()
        setupValidation()
        setDefaultLocation()
    }

    private fun setupPipelineOptions() {
        val pipelines = FXCollections.observableArrayList(
            "FastQC Quality Control",
            "RNA-seq Analysis",
            "DNA-seq Variant Calling",
            "ChIP-seq Analysis",
            "Metagenomics",
            "Custom Pipeline"
        )
        defaultPipelineCombo.items = pipelines
        defaultPipelineCombo.selectionModel.selectFirst()
    }

    private fun setupSampleFormatOptions() {
        val formats = FXCollections.observableArrayList(
            "FASTQ (Single-end)",
            "FASTQ (Paired-end)",
            "FASTA",
            "BAM/SAM",
            "Mixed formats"
        )
        sampleFormatCombo.items = formats
        sampleFormatCombo.selectionModel.selectFirst()
    }

    private fun setupValidation() {
        projectNameField.textProperty().addListener { _, _, _ -> validateForm() }
        locationField.textProperty().addListener { _, _, _ -> validateForm() }
    }

    private fun setupDialogValidation() {
        dialogPane?.let { pane ->
            val okButton = pane.lookupButton(ButtonType.OK) as? Button
            okButton?.disableProperty()?.bind(
                projectNameField.textProperty().isEmpty
                    .or(locationField.textProperty().isEmpty)
            )
        }
    }

    private fun setDefaultLocation() {
        val userHome = System.getProperty("user.home")
        val defaultPath = File(userHome, "BioDockProjects").absolutePath
        locationField.text = defaultPath
    }

    @FXML
    private fun browseLocation() {
        val directoryChooser = DirectoryChooser().apply {
            title = "Select Project Directory"
            initialDirectory = File(locationField.text.ifEmpty { System.getProperty("user.home") })
        }

        val stage = browseButton.scene.window as Stage
        val selectedDirectory = directoryChooser.showDialog(stage)

        selectedDirectory?.let {
            locationField.text = it.absolutePath
        }
    }

    private fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        // Validate project name
        val projectName = projectNameField.text?.trim()
        if (projectName.isNullOrBlank()) {
            errors.add("Project name is required")
        } else if (projectName.length < 3) {
            errors.add("Project name must be at least 3 characters")
        } else if (!projectName.matches(Regex("[a-zA-Z0-9\\s_-]++"))) {
            errors.add("Project name contains invalid characters")
        }

        // Validate location
        val location = locationField.text?.trim()
        if (location.isNullOrBlank()) {
            errors.add("Project location is required")
        } else {
            val locationFile = File(location)
            if (!locationFile.exists() && !locationFile.mkdirs()) {
                errors.add("Cannot create project directory")
            }
        }

        if (errors.isNotEmpty()) {
            validationLabel.text = errors.joinToString("\n• ", "• ")
            validationBox.isVisible = true
            validationBox.isManaged = true
            return false
        } else {
            validationBox.isVisible = false
            validationBox.isManaged = false
            return true
        }
    }

    // Getters for the main controller to access the form data
    fun getProject(): Project? {
        return if (validateForm()) {
            Project(
                name = projectNameField.text.trim(),
                description = descriptionArea.text?.trim() ?: "",
            )
        } else null
    }
}