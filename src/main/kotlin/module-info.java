module io.github.antoniodimeglio.biodock.biodock {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;


    opens io.github.antoniodimeglio.biodock.biodock to javafx.fxml;
    exports io.github.antoniodimeglio.biodock.biodock;
}