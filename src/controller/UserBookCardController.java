package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.Book;
import model.BorrowedBook;
import model.User;
import utils.BookUtils;
import utils.UserUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UserBookCardController {

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
    private ImageView borrowIcon;
    @FXML
    private ImageView infoIcon;

    private String fullDescription;
    private UserBooksViewController userBooksViewController; // Link to parent
    private Book currentBook;

    public void setBook(Book book) {
        this.currentBook = book;

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
            borrowIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/borrow.png")));
            infoIcon.setImage(new Image(getClass().getResourceAsStream("/assets/icons/info.png")));
        } catch (Exception e) {
            System.err.println("Failed to load icons");
        }

        Tooltip.install(borrowIcon, new Tooltip("Borrow this book"));
        Tooltip.install(infoIcon, new Tooltip("View full description"));

        borrowIcon.setOnMouseClicked(e -> borrowBook());

        infoIcon.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/bookDetailsDialog.fxml"));
                VBox dialogContent = loader.load();

                BookDetailsDialogController controller = loader.getController();
                controller.setBook(book); // Encapsulation

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

    public void setUserBooksViewController(UserBooksViewController controller) {
        this.userBooksViewController = controller;
    }

    private void borrowBook() {
        try {
            if (userBooksViewController == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Internal error: Controller not set.");
                return;
            }

            User currentUser = userBooksViewController.getCurrentUser();
            if (currentUser == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "No logged-in user found.");
                return;
            }

            if (currentBook.getQuantity() <= 0) {
                showAlert(Alert.AlertType.WARNING, "Unavailable", "Sorry, this book is currently unavailable.");
                return;
            }

            boolean alreadyBorrowed = currentUser.getCurrentBorrowedBooks().stream()
                    .anyMatch(b -> b.getBookId().equals(currentBook.getBookId()));

            if (alreadyBorrowed) {
                showAlert(Alert.AlertType.WARNING, "Already Borrowed", "You have already borrowed this book.");
                return;
            }

            if (currentUser.getCurrentBorrowedBooks().size() >= 4) {
                showAlert(Alert.AlertType.WARNING, "Limit Reached", "You cannot borrow more than 4 books at a time.");
                return;
            }

            // Show return date picker dialog with max 15 days limit
            LocalDate today = LocalDate.now();
            LocalDate maxReturnDate = today.plusDays(15);

            DatePicker datePicker = new DatePicker(today.plusDays(5)); // default 5 days later
            datePicker.setDayCellFactory(dp -> new javafx.scene.control.DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item.isBefore(today) || item.isAfter(maxReturnDate)) {
                        setDisable(true);
                        setStyle("-fx-background-color: #808080; -fx-text-fill: #fcfcfc"); // red for disabled
                    }
                }
            });

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Select Return Date");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            VBox vbox = new VBox(10);
            vbox.getChildren().addAll(new javafx.scene.control.Label("Please select a return date (max 10 days):"),
                    datePicker);
            dialog.getDialogPane().setContent(vbox);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isEmpty() || result.get() != ButtonType.OK) {
                // User cancelled dialog
                return;
            }

            LocalDate selectedReturnDate = datePicker.getValue();
            if (selectedReturnDate == null || selectedReturnDate.isBefore(today)
                    || selectedReturnDate.isAfter(maxReturnDate)) {
                showAlert(Alert.AlertType.ERROR, "Invalid Date",
                        "Please select a valid return date within 10 days from today.");
                return;
            }

            BorrowedBook borrowedBook = new BorrowedBook(
                    currentBook.getBookId(),
                    today,
                    selectedReturnDate);

            currentUser.getCurrentBorrowedBooks().add(borrowedBook);
            currentUser.setBorrowedCount(currentUser.getBorrowedCount() + 1);

            currentBook.setQuantity(currentBook.getQuantity() - 1);

            // Save updated users
            List<User> allUsers = UserUtils.loadUsers();
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getUserId().equals(currentUser.getUserId())) {
                    allUsers.set(i, currentUser);
                    break;
                }
            }
            UserUtils.saveUsers(allUsers);

            // Save updated books
            List<Book> allBooks = BookUtils.getAllBooks();
            for (int i = 0; i < allBooks.size(); i++) {
                if (allBooks.get(i).getBookId().equals(currentBook.getBookId())) {
                    allBooks.set(i, currentBook);
                    break;
                }
            }
            BookUtils.saveAllBooks(allBooks);

            // Update UI availability label
            availabilityLabel.setText(currentBook.getQuantity() > 0 ? "Available" : "Unavailable");
            availabilityLabel.setStyle("-fx-text-fill: " + (currentBook.getQuantity() > 0 ? "green" : "red") +
                    "; -fx-background-color: " + (currentBook.getQuantity() > 0 ? "#e6fde5" : "#ffe6e6") +
                    "; -fx-background-radius: 16; -fx-padding: 3 12 3 12;");

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "You have successfully borrowed the book! Return by: " + selectedReturnDate);

            if (userBooksViewController != null) {
                userBooksViewController.refreshUserData();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to borrow the book. Please try again.");
        }
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