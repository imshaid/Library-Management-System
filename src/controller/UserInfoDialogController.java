package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.User;

public class UserInfoDialogController {

    @FXML
    private ImageView profileImage;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label borrowedCountLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Label badgesLabel;
    @FXML
    private ProgressBar ratingBar;
    @FXML
    private Label averageRatingLabel;
    @FXML
    private Button closeButton;

    private User user;

    public void setUser(User user) {
        this.user = user;

        try {
            Image img = new Image(getClass().getResourceAsStream(user.getProfilePicPath()));
            profileImage.setImage(img);
        } catch (Exception e) {
            profileImage.setImage(new Image(getClass().getResourceAsStream("/assets/users/user.png")));
        }

        fullNameLabel.setText(user.getFullName());
        usernameLabel.setText("@" + user.getUsername());
        emailLabel.setText(user.getEmail());
        phoneLabel.setText(user.getPhoneNumber().isBlank() ? "N/A" : user.getPhoneNumber());
        addressLabel.setText(user.getAddress().isBlank() ? "N/A" : user.getAddress());
        borrowedCountLabel.setText(String.valueOf(user.getCurrentBorrowedBooks().size()));
        pointsLabel.setText(user.getPoints().getCurrent() + " / " + user.getPoints().getMax());
        badgesLabel.setText(user.getBadges().length == 0 ? "None" : String.join(", ", user.getBadges()));

        // Set average rating text
        averageRatingLabel.setText(String.format("%.2f / 5.0", user.getAverageRating()));

        // Set progress (normalized between 0 and 1)
        double progress = user.getAverageRating() / 5.0;
        ratingBar.setProgress(progress);

        // Dynamically set color based on rating value
        if (progress < 0.3) {
            ratingBar.setStyle("-fx-accent: red;");
        } else if (progress < 0.7) {
            ratingBar.setStyle("-fx-accent: orange;");
        } else {
            ratingBar.setStyle("-fx-accent: green;");
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}