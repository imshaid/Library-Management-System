package controller;

import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.User;
import utils.UserUtils;

public class UserAuthController {

    private static final String SELECTED_TAB_CLASS = "selected-tab";
    private static final String ADMIN_USERNAME = "";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String ADMIN_PASSWORD = "";

    @FXML
    private Button signInTab, signUpTab;

    @FXML
    private Button actionButton;

    @FXML
    private TextField usernameOrEmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Scene scene = usernameOrEmailField.getScene();
            if (scene == null)
                return;

            Parent root = scene.getRoot();
            if (!root.getStyleClass().contains("root")) {
                root.getStyleClass().add("root");
            }

            String lightTheme = getClass().getResource("/css/style-light.css").toExternalForm();
            if (!scene.getStylesheets().contains(lightTheme)) {
                scene.getStylesheets().add(lightTheme);
            }

            usernameOrEmailField.getParent().requestFocus();

            signInTab.setCursor(Cursor.HAND);
            signUpTab.setCursor(Cursor.HAND);

            setActiveTab(signInTab);
        });
    }

    private void setActiveTab(Button activeTab) {
        signInTab.getStyleClass().remove(SELECTED_TAB_CLASS);
        signUpTab.getStyleClass().remove(SELECTED_TAB_CLASS);

        activeTab.getStyleClass().add(SELECTED_TAB_CLASS);

        signInTab.applyCss();
        signUpTab.applyCss();
    }

    @FXML
    private void handleSignInTabClick() {
        setActiveTab(signInTab);
    }

    @FXML
    private void handleSignUpTabClick() {
        MainController.getInstance().resizeWindow(900, 636);
        MainController.getInstance().loadContent("/fxml/userSignUp.fxml");
    }

    // private void clearFieldsAndErrors() {
    // usernameOrEmailField.clear();
    // passwordField.clear();
    // errorLabel.setText("");
    // }

    @FXML
    private void handleSignIn(ActionEvent event) {
        String input = usernameOrEmailField.getText().trim();
        String pass = passwordField.getText().trim();

        if (input.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter both username/email and password.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        User matchedUser = UserUtils.findUserByIdOrUsernameOrEmail(input);

        if (matchedUser == null) {
            errorLabel.setText("User not found. Please sign up first.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!pass.equals(matchedUser.getPasswordHash())) { // Later: use hashed comparison!
            errorLabel.setText("Incorrect password. Try again.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // ✅ Load dashboard into custom title bar window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/userDashboard.fxml"));
            Parent dashboardContent = loader.load();

            UserDashboardController controller = loader.getController();
            controller.setCurrentUser(matchedUser);

            MainController.getInstance().resizeWindow(1200, 836);
            MainController.getInstance().loadContent(dashboardContent);

            errorLabel.setText(""); // Clear any old error
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load dashboard.");
            errorLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainController.getInstance().resizeWindow(900, 636);
        MainController.getInstance().loadContent("/fxml/launch.fxml");
    }
}