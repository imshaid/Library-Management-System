package controller;

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

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UserSignUpController {

    private static final String SELECTED_TAB_CLASS = "selected-tab";

    @FXML
    private Button signInTab, signUpTab;

    @FXML
    private TextField nameField, emailField, usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button signUpButton;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            Scene scene = emailField.getScene();
            if (scene != null) {
                String lightTheme = getClass().getResource("/css/style-light.css").toExternalForm();

                if (!scene.getStylesheets().contains(lightTheme)) {
                    scene.getStylesheets().add(lightTheme);
                }

                if (!scene.getRoot().getStyleClass().contains("root")) {
                    scene.getRoot().getStyleClass().add("root");
                }

                emailField.getParent().requestFocus();
                signInTab.setCursor(Cursor.HAND);
                signUpTab.setCursor(Cursor.HAND);

                selectSignUpTab();
            }
        });
    }

    private void selectSignUpTab() {
        setActiveTab(signUpTab);
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
        MainController.getInstance().resizeWindow(900, 636);
        MainController.getInstance().loadContent("/fxml/userAuth.fxml");
    }

    @FXML
    private void handleSignUpTabClick() {
        // Already on sign-up
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            errorLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        List<User> allUsers = UserUtils.loadUsers();

        boolean emailExists = allUsers.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        boolean usernameExists = allUsers.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));

        if (emailExists || usernameExists) {
            errorLabel.setStyle("-fx-text-fill: red;");
            if (emailExists && usernameExists) {
                errorLabel.setText("Email and username already exist. Please sign in.");
            } else if (emailExists) {
                errorLabel.setText("Email already exists. Please sign in.");
            } else {
                errorLabel.setText("Username already taken. Please sign in.");
            }
            return;
        }

        // Generate new user ID based on last user's number
        int nextId = allUsers.stream()
                .map(User::getUserId)
                .filter(id -> id.startsWith("U"))
                .map(id -> id.substring(1))
                .filter(id -> id.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;

        String userId = String.format("U%03d", nextId);
        Date registeredDate = new Date();

        // Create the new user object
        User newUser = new User(userId, username, email, pass, name, registeredDate);
        newUser.setPoints(new model.Points(0, 0)); // Start with 0 / 0 points
        newUser.setProfilePicPath("/assets/users/user.png");

        // Save the user
        UserUtils.saveUser(newUser);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/userDashboard.fxml"));
            Parent node = loader.load();

            UserDashboardController controller = loader.getController();
            controller.setCurrentUser(newUser);

            MainController.getInstance().resizeWindow(1200, 836);
            MainController.getInstance().loadContent(node);
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setStyle("-fx-text-fill: red;");
            errorLabel.setText("Something went wrong.");
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainController.getInstance().resizeWindow(900, 636);
        MainController.getInstance().loadContent("/fxml/launch.fxml");
    }
}