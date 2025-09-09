package io.github.antoniodimeglio.biodock.biodock

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class BioDockApplication : Application() {

    override fun start(primaryStage: Stage) {
        logger.info { "Starting BioDock application..."}

        try {
            val fxmlLoader = FXMLLoader(
                BioDockApplication::class.java.getResource("/fxml/main-view.fxml")
            )
            val scene = Scene(fxmlLoader.load(), 1200.0, 800.0)

            scene.stylesheets.add(
                BioDockApplication::class.java.getResource("/css/styles.css")?.toExternalForm()
            )

            primaryStage.title = "BioDock - Bioinformatics Pipeline Runner"
            primaryStage.scene = scene
            primaryStage.minWidth = 800.0
            primaryStage.minHeight = 600.0

            primaryStage.show()
            logger.info { "BioDock application started successfully."}

        } catch (e: Exception) {
            logger.error(e) { "Failed to start BioDock application." }
            throw e
        }
    }
}
  
