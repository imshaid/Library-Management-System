package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;
import javafx.stage.Stage;
import model.Admin;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private AnchorPane mainContent;
    @FXML
    private Label adminNameLabel;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button usersButton;
    @FXML
    private Button booksButton;

    private Admin currentAdmin;

    @FXML
    public void initialize() {
        System.out.println("AdminDashboardController initialized");
        Platform.runLater(() -> setActiveButton(dashboardButton));
    }

    public void setCurrentAdmin(Admin admin) {
        this.currentAdmin = admin;

        if (adminNameLabel != null) {
            adminNameLabel.setText("Welcome, " + admin.getFullName());
        }

        // Load dashboard view AFTER admin is available
        loadView("/fxml/admin/dashboardHome.fxml");
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active style from all buttons
        dashboardButton.getStyleClass().remove("active-tab");
        usersButton.getStyleClass().remove("active-tab");
        booksButton.getStyleClass().remove("active-tab");

        // Add active style to the selected one
        if (!activeBtn.getStyleClass().contains("active-tab")) {
            activeBtn.getStyleClass().add("active-tab");
        }
    }

    @FXML
    private void handleDashboardClick(ActionEvent event) {
        loadView("/fxml/admin/dashboardHome.fxml");
        setActiveButton(dashboardButton);
    }

    @FXML
    private void handleUsersClick(ActionEvent event) {
        loadView("/fxml/admin/usersView.fxml");
        setActiveButton(usersButton);
    }

    @FXML
    private void handleBooksClick(ActionEvent event) {
        loadView("/fxml/admin/booksView.fxml");
        setActiveButton(booksButton);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Inject Admin into the subview controller if it's the dashboard home
            if (fxmlPath.contains("dashboardHome.fxml")) {
                Object controller = loader.getController();
                if (controller instanceof AdminDashboardHomeController) {
                    ((AdminDashboardHomeController) controller).setCurrentAdmin(currentAdmin);
                }
            }

            mainContent.getChildren().setAll(view);

            if (fxmlPath.contains("dashboardHome.fxml")) {
                Object controller = loader.getController();
                System.out.println("🔧 Loaded controller for dashboardHome: " + controller);
                if (controller instanceof AdminDashboardHomeController) {
                    ((AdminDashboardHomeController) controller).setCurrentAdmin(currentAdmin);
                    System.out.println("✅ Admin injected into dashboardHome");
                } else {
                    System.out.println("❌ Controller is not AdminDashboardHomeController");
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Resize back to 900x600 for launch screen
            MainController.getInstance().resizeWindow(900, 636);

            // Load the launch screen inside the custom window layout
            MainController.getInstance().loadContent("/fxml/launch.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}