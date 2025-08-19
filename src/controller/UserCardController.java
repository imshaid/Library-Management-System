package controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import utils.UserUtils;

public class UserCardController {

    @FXML
    private ImageView profileImage;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView deleteIcon;

    @FXML
    private ImageView infoIcon;

    private User user;

    // Add callback to refresh parent UI
    private Runnable onUserDeleted;

    public void setUser(User user, Runnable onUserDeleted) {
        this.user = user;
        this.onUserDeleted = onUserDeleted;

        // Set icons images so they appear on UI
        deleteIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/delete.jpg")));
        infoIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/info.png")));

        // Set user info
        try {
            profileImage.setImage(new Image(getClass().getResourceAsStream(user.getProfilePicPath())));
        } catch (Exception e) {
            profileImage.setImage(new Image(getClass().getResourceAsStream("/assets/users/user.png")));
        }

        fullNameLabel.setText(user.getFullName());
        usernameLabel.setText("@" + user.getUsername());

        // Set delete icon click
        deleteIcon.setOnMouseClicked(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Delete user: " + user.getFullName() + "?");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (UserUtils.deleteUserById(user.getUserId())) {
                        onUserDeleted.run(); // Trigger UI refresh or other actions
                    }
                }
            });
        });

        // Info icon click
        infoIcon.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserInfoDialog.fxml"));
                Parent root = loader.load();

                UserInfoDialogController controller = loader.getController();
                controller.setUser(user);

                Stage stage = new Stage();
                stage.setTitle("User Details - " + user.getFullName());
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene(root));
                stage.showAndWait();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}