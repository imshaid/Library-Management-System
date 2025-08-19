package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class LaunchController {

    private static final String ADMIN_SECRET_CODE = "1234";

    @FXML
    void goToUserAuth(ActionEvent event) {
        MainController.getInstance().resizeWindow(900, 636);
        MainController.getInstance().loadContent("/fxml/userAuth.fxml");
    }

    @FXML
    void goToAdminLogin(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Admin Access");
        dialog.setHeaderText("Enter Admin Secret Code");
        dialog.setContentText("Secret Code:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String enteredCode = result.get().trim();

            if (enteredCode.equals(ADMIN_SECRET_CODE)) {
                MainController.getInstance().resizeWindow(900, 636);
                MainController.getInstance().loadContent("/fxml/adminLogin.fxml");
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Access Denied");
                alert.setHeaderText("Invalid Secret Code");
                alert.setContentText("You are not authorized to access the admin panel.");
                alert.showAndWait();
            }
        }
    }
}