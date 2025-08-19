package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private HBox titleBar;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button closeBtn;
    @FXML
    private Button minimizeBtn;

    private double xOffset = 0;
    private double yOffset = 0;

    private static MainController instance;

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        titleBar.setOnMousePressed(this::handleMousePressed);
        titleBar.setOnMouseDragged(this::handleMouseDragged);

        // ✅ Load Launch Screen initially
        loadContent("/fxml/launch.fxml");

        contentArea.getProperties().put("controller", this);
    }

    public void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Overloaded method if you already loaded the node externally:
    public void loadContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    public void resizeWindow(double width, double height) {
        Platform.runLater(() -> {
            try {
                // Get stage from titleBar OR contentArea
                Stage stage = (Stage) contentArea.getScene().getWindow();

                // Update dimensions
                stage.setWidth(width);
                stage.setHeight(height);
                stage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    private void handleMouseDragged(MouseEvent event) {
        Stage stage = (Stage) titleBar.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) minimizeBtn.getScene().getWindow();
        stage.setIconified(true);
    }
}