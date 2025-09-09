package io.github.antoniodimeglio.biodock.biodock.model

import javafx.beans.property.*
import javafx.collections.FXCollections

class AppState {
    val currentProject = SimpleObjectProperty<Project>(Project(name = "New Project"))
    val selectedSamples = SimpleListProperty<Sample>(FXCollections.observableArrayList())
    val selectedPipeline = SimpleObjectProperty<Pipeline>()
    val isAnalysisRunning = SimpleBooleanProperty(false)
    val analysisProgress = SimpleDoubleProperty(0.0)
    val statusMessage = SimpleStringProperty("Ready")
    val dockerStatus = SimpleStringProperty("Checking...")

    val availablePipelines = SimpleListProperty<Pipeline>(
        javafx.collections.FXCollections.observableList(Pipeline.getDefaultPipelines())
    )

    init {
        selectedPipeline.set(availablePipelines.first())
    }

    fun reset() {
        currentProject.set(Project(name = "New Project"))
        selectedSamples.clear()
        selectedPipeline.set(availablePipelines.first())
        isAnalysisRunning.set(false)
        analysisProgress.set(0.0)
        statusMessage.set("Ready")
    }
}