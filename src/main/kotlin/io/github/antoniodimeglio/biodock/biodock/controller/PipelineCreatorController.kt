package io.github.antoniodimeglio.biodock.biodock.controller

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox

class PipelineCreatorController {
    lateinit var dockerfileArea: TextArea
    lateinit var loadDockerfileBtn: Button
    lateinit var outputDirField: TextField
    lateinit var inputDirField: TextField
    lateinit var outputTypesContainer: VBox
    lateinit var addOutputTypeBtn: Button
    lateinit var inputTypesContainer: VBox
    lateinit var addInputTypeBtn: Button
    lateinit var commandsContainer: VBox
    lateinit var addCommandBtn: Button
    lateinit var descriptionArea: TextArea
    lateinit var versionField: TextField
    lateinit var nameField: TextField
    lateinit var idField: TextField


    @FXML private fun onLoadDockerfile() {}

    @FXML private fun onAddCommand() {}

    @FXML private fun onAddInputType() {}

    @FXML private fun onAddOutputType() {}

    @FXML private fun onPreview() {}

    @FXML private fun onValidate() {}

    @FXML private fun onSave() {}

    @FXML private fun onCancel() {}
}