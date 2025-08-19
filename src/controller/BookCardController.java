package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.Book;
import utils.BookUtils;

import java.io.IOException;

import javafx.application.Platform;

public class BookCardController {

    @FXML
    private ImageView coverImage;
    @FXML
    private Label availabilityLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private TextFlow authorFlow;
    @FXML
    private Label genreLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private ImageView updateIcon;
    @FXML
    private ImageView deleteIcon;
    @FXML
    private ImageView infoIcon;

    private String fullDescription;

    private BooksViewController booksViewController;

    public void setBook(Book book) {
        try {
            coverImage.setImage(new Image(getClass().getResourceAsStream(book.getCoverImage())));
        } catch (Exception e) {
            System.err.println("Image not found: " + book.getCoverImage());
        }

        availabilityLabel.setText(book.getQuantity() > 0 ? "Available" : "Unavailable");
        availabilityLabel.setStyle("-fx-text-fill: " + (book.getQuantity() > 0 ? "green" : "red") +
                "; -fx-background-color: " + (book.getQuantity() > 0 ? "#e6fde5" : "#ffe6e6") +
                "; -fx-background-radius: 16; -fx-padding: 3 12 3 12;");

        titleLabel.setText(book.getTitle());

        Text byText = new Text("by ");
        byText.setStyle("-fx-fill: #444; -fx-font-size: 12;");

        Text authorName = new Text(book.getAuthor());
        authorName.setStyle("-fx-fill: rgb(255, 122, 5); -fx-font-weight: bold; -fx-font-size: 12;");
        authorFlow.getChildren().setAll(byText, authorName);

        genreLabel.setText(String.join(", ", book.getGenre()));
        ratingLabel.setText(String.format("★ %.1f", book.getAverageRating()) +
                " (" + book.getRating().size() + " ratings)");

        fullDescription = book.getDescription();
        descriptionLabel.setText(trimDescription(fullDescription));

        try {
            updateIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/update.png")));
            deleteIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/delete.jpg")));
            infoIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/info.png")));
        } catch (Exception e) {
            System.err.println("Failed to load icons");
        }

        Tooltip.install(updateIcon, new Tooltip("Update this book"));
        Tooltip.install(deleteIcon, new Tooltip("Delete this book"));
        Tooltip.install(infoIcon, new Tooltip("View full description"));

        updateIcon.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/bookUpdateDialog.fxml"));
                VBox dialogContent = loader.load();

                BookUpdateDialogController controller = loader.getController();
                controller.setBook(book);

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Update Book");
                dialog.getDialogPane().setContent(dialogContent);

                // ✅ Add your stylesheet here
                dialog.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/style-light.css").toExternalForm());

                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();

                // ✅ Reload books after update
                if (booksViewController != null) {
                    booksViewController.reloadBooks();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        deleteIcon.setOnMouseClicked(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Book");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to delete \"" + book.getTitle() + "\"?");

            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    BookUtils.deleteBookById(book.getBookId());

                    if (booksViewController != null) {
                        Platform.runLater(() -> booksViewController.reloadBooks());
                    }

                    showAlert(Alert.AlertType.INFORMATION, "Deleted",
                            "Book \"" + book.getTitle() + "\" was successfully deleted.");
                }
            });
        });

        infoIcon.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/bookDetailsDialog.fxml"));
                VBox dialogContent = loader.load();

                BookDetailsDialogController controller = loader.getController();
                controller.setBook(book); // 🎯 OOP: Encapsulation used here

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Book Details");
                dialog.getDialogPane().setContent(dialogContent);
                dialog.getDialogPane().getStylesheets().add(
                        getClass().getResource("/css/style-light.css").toExternalForm());
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                dialog.showAndWait();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void setBooksViewController(BooksViewController controller) {
        this.booksViewController = controller;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String trimDescription(String desc) {
        String[] words = desc.split("\\s+");
        if (words.length <= 100)
            return desc;

        StringBuilder shortDesc = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            shortDesc.append(words[i]).append(" ");
        }
        return shortDesc.toString().trim() + "...";
    }
}