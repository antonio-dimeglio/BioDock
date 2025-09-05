package io.github.antoniodimeglio.biodock.biodock

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class BioDockApplication : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(BioDockApplication::class.java.getResource("biodock-view.fxml"))
        val scene = Scene(fxmlLoader.load(), 1024.0, 768.0)
        stage.title = "BioDock"
        stage.scene = scene
        stage.show()
    }
}
  
