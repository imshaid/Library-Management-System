package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Admin;
import utils.AdminUtils;

import java.time.LocalTime;
import java.util.List;

public class AdminLoginController {

    @FXML
    private TextField usernameOrEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        // Remove auto-focus from username field
        Platform.runLater(() -> {
            if (usernameOrEmailField.getParent() != null) {
                usernameOrEmailField.getParent().requestFocus();
            }
        });
    }

    @FXML
    private void handleAdminSignIn(ActionEvent event) {
        String input = usernameOrEmailField.getText().trim();
        String password = passwordField.getText().trim();

        List<Admin> allAdmins = AdminUtils.loadAdmins();

        Admin matchedAdmin = allAdmins.stream()
                .filter(admin -> admin.getAdminId().equalsIgnoreCase(input)
                        || admin.getUsername().equalsIgnoreCase(input)
                        || admin.getEmail().equalsIgnoreCase(input))
                .findFirst()
                .orElse(null);

        if (matchedAdmin == null) {
            showError("Admin not found. Redirecting to launch...");
            goToLaunchWithDelay();
            return;
        }

        if (!password.equals(matchedAdmin.getPasswordHash())) {
            showError("Invalid password. Redirecting to launch...");
            goToLaunchWithDelay();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/adminDashboard.fxml"));
            Parent node = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.setCurrentAdmin(matchedAdmin);

            MainController.getInstance().resizeWindow(1200, 836);
            MainController.getInstance().loadContent(node);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load dashboard.");
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText(message);
        });
    }

    private void goToLaunchWithDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2-second pause
                Platform.runLater(() -> goBack(null));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainController.getInstance().resizeWindow(900, 636);
        MainController.getInstance().loadContent("/fxml/launch.fxml");
    }
}