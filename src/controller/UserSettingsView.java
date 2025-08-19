package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.User;
import utils.UserUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class UserSettingsView {

    @FXML
    private TextField fullNameField, emailField, phoneField, addressField;

    @FXML
    private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;

    @FXML
    private Button browseImageButton, saveChangesButton, changePasswordButton, deleteAccountButton, cancelButton;

    @FXML
    private ImageView profileImageView; // <-- Add ImageView to show profile picture

    private User currentUser;

    public void initialize() {
        // Nothing here; user will be set via setCurrentUser(User)
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            populateFieldsFromUser();
        }
    }

    private void populateFieldsFromUser() {
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhoneNumber());
        addressField.setText(currentUser.getAddress());

        // Load profile image if exists
        String imgPath = currentUser.getProfilePicPath();
        if (imgPath != null && !imgPath.isEmpty()) {
            File imgFile = new File("src" + imgPath); // Assuming path starts with "/assets/..."
            if (imgFile.exists()) {
                Image img = new Image(imgFile.toURI().toString());
                profileImageView.setImage(img);
            } else {
                profileImageView.setImage(null); // Clear if not found
            }
        } else {
            profileImageView.setImage(null); // Clear if no path set
        }

        // Clear password fields for security
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Window window = browseImageButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            try {
                // Copy file to user assets folder
                File destDir = new File("src/assets/users/");
                if (!destDir.exists())
                    destDir.mkdirs();

                // Create a new file name: username + extension
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                String newFileName = currentUser.getUsername() + extension;
                File destFile = new File(destDir, newFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update the TextField and User profilePicPath (relative path for resource
                // loading)
                String relativePath = "/assets/users/" + newFileName;
                currentUser.setProfilePicPath(relativePath);

                // Update ImageView instantly
                Image img = new Image(destFile.toURI().toString());
                profileImageView.setImage(img);

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Failed to save profile image.");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveChanges(ActionEvent event) {
        if (currentUser == null)
            return;

        currentUser.setFullName(fullNameField.getText());
        currentUser.setEmail(emailField.getText());
        currentUser.setPhoneNumber(phoneField.getText());
        currentUser.setAddress(addressField.getText());

        List<User> allUsers = UserUtils.loadUsers();
        allUsers.removeIf(u -> u.getUserId().equals(currentUser.getUserId()));
        allUsers.add(currentUser);
        UserUtils.saveUsers(allUsers);

        showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Your information has been updated.");
    }

    @FXML
    private void handleCancelChanges(ActionEvent event) {
        if (currentUser != null) {
            populateFieldsFromUser();
        }
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        if (currentUser == null)
            return;

        String oldPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!currentUser.getPasswordHash().equals(oldPass)) {
            showAlert(Alert.AlertType.ERROR, "Incorrect Password", "Old password is incorrect.");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showAlert(Alert.AlertType.ERROR, "Mismatch", "New passwords do not match.");
            return;
        }

        currentUser.setPasswordHash(newPass);

        List<User> allUsers = UserUtils.loadUsers();
        allUsers.removeIf(u -> u.getUserId().equals(currentUser.getUserId()));
        allUsers.add(currentUser);
        UserUtils.saveUsers(allUsers);

        showAlert(Alert.AlertType.INFORMATION, "Password Changed", "Your password has been updated.");

        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleDeleteAccount(ActionEvent event) {
        if (currentUser == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = UserUtils.deleteUserById(currentUser.getUserId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Account Deleted", "Your account has been deleted.");

                MainController.getInstance().resizeWindow(900, 636);
                MainController.getInstance().loadContent("/fxml/launch.fxml");
            } else {
                showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete your account.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}