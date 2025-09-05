package io.github.antoniodimeglio.biodock.biodock.controller

import io.github.antoniodimeglio.biodock.biodock.service.FastqcRunner
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.TabPane
import javafx.scene.control.TextArea
import javafx.scene.web.WebView
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainController {

    @FXML private lateinit var logArea: TextArea
    @FXML private lateinit var tabPane: TabPane
    @FXML private lateinit var statusLabel: Label
    @FXML private lateinit var runBtn: Button
    @FXML private lateinit var openFileBtn: Button
    @FXML private lateinit var reportView: WebView

    private var selectedFile: String? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    @FXML
    private lateinit var openFile: MenuItem

    @FXML
    private fun handleOpenFile() {
        val fileChooser = FileChooser()
        fileChooser.title = "Select a FASTQ file."
        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter("FASTQ Files", "*.fastq"))
        val file = fileChooser.showOpenDialog(Stage())
        if (file != null){
            selectedFile = file.absolutePath
            statusLabel.text = "Selected ${selectedFile}"
        }
    }

    @FXML
    private fun handleRunFastqc() {
        val file = selectedFile ?: return
        statusLabel.text = "Running FASTQC..."
        logArea.clear()


        scope.launch{
             FastqcRunner.runFastqc(file,
                onLog = { line -> appendLog(line)} ,
                onDone = { reportPath -> showReport(reportPath) })
        }

        Platform.runLater { statusLabel.text = "Done" }
    }

    private fun appendLog(line: String) {
        Platform.runLater { logArea.appendText(line + "\n") }
    }

    private fun showReport(path: String) {
        Platform.runLater {
            val url = "file:///$path".replace("\\", "/")
            reportView.engine.load(url)
        }
    }
}