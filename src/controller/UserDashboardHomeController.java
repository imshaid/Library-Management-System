package controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Book;
import model.BorrowedBook;
import model.User;
import utils.BookUtils;
import utils.UserUtils;
import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class UserDashboardHomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label borrowedBooksLabel;
    @FXML
    private Label overdueBooksLabel;
    @FXML
    private Label avgRatingLabel;
    @FXML
    private Label pointsLabel;

    @FXML
    private Label totalBooksLabel;
    @FXML
    private Label totalAuthorsLabel;
    @FXML
    private Label totalGenresLabel;

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label profileUsernameLabel;
    @FXML
    private GridPane borrowedBooksGrid;

    private User currentUser;
    private static final int BOOKS_PER_ROW = 4;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateDashboard();
        populateBorrowedBooks();
        updateProfileCard();
    }

    /**
     * Updates dashboard stats dynamically:
     * - User-related stats
     * - Library-wide stats: total unique books / total quantity,
     * total unique authors, total unique genres
     */
    private void updateDashboard() {
        if (currentUser == null)
            return;

        // User-specific info
        welcomeLabel.setText("Hello, " + currentUser.getFullName() + " (@" + currentUser.getUsername() + ")!");
        borrowedBooksLabel.setText(String.valueOf(currentUser.getCurrentBorrowedBooks().size()));
        overdueBooksLabel.setText(String.valueOf(currentUser.getOverdueBooks().size()));
        avgRatingLabel.setText(String.format("%.2f", currentUser.getAverageRating()));
        pointsLabel.setText(currentUser.getPoints().getCurrent() + " / " + currentUser.getPoints().getMax());

        // Load all books from JSON
        List<Book> books = BookUtils.getAllBooks();

        // Total quantity of all books combined (sum of quantity fields)
        int totalQuantity = books.stream()
                .mapToInt(Book::getQuantity)
                .sum();

        // Collect unique authors and genres
        Set<String> uniqueAuthors = new HashSet<>();
        Set<String> uniqueGenres = new HashSet<>();
        for (Book book : books) {
            uniqueAuthors.add(book.getAuthor());
            if (book.getGenre() != null) {
                uniqueGenres.addAll(book.getGenre());
            }
        }

        // Update UI labels accordingly
        totalBooksLabel.setText(String.valueOf(totalQuantity)); // Only total quantity now
        totalAuthorsLabel.setText(String.valueOf(uniqueAuthors.size()));
        totalGenresLabel.setText(String.valueOf(uniqueGenres.size()));
    }

    private void updateProfileCard() {
        if (currentUser == null)
            return;

        if (currentUser.getProfilePicPath() != null &&
                !currentUser.getProfilePicPath().isEmpty()) {
            try {
                profileImageView.setImage(new Image(getClass().getResourceAsStream(currentUser.getProfilePicPath())));
            } catch (Exception e) {
                System.err.println("Failed to load profile image: " + e.getMessage());
            }
        }
    }

    /**
     * Populates the borrowed books grid with book cards showing
     * cover, title and return status button.
     */
    public void populateBorrowedBooks() {
        borrowedBooksGrid.getChildren().clear();
        if (currentUser == null || currentUser.getCurrentBorrowedBooks().isEmpty())
            return;

        int col = 0, row = 0;
        List<BorrowedBook> sortedBooks = new ArrayList<>(currentUser.getCurrentBorrowedBooks());
        sortedBooks.sort(Comparator.comparing(BorrowedBook::getReturnDate));

        for (BorrowedBook b : sortedBooks) {
            Book book = BookUtils.findBookById(b.getBookId());
            if (book == null)
                continue;

            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), b.getReturnDate());

            String btnText;
            String btnColor;
            if (daysLeft > 0) {
                btnText = daysLeft + " day" + (daysLeft > 1 ? "s" : "") + " left";
                btnColor = "#4caf50"; // Green
            } else if (daysLeft == 0) {
                btnText = "Due today";
                btnColor = "#ff9800"; // Orange
            } else {
                btnText = Math.abs(daysLeft) + " day" + (Math.abs(daysLeft) > 1 ? "s" : "") + " over";
                btnColor = "#f44336"; // Red
            }

            Button returnBtn = new Button(btnText);
            returnBtn.setPrefSize(80, 28);
            returnBtn.setStyle("-fx-background-color: " + btnColor + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 11;" +
                    "-fx-background-radius: 1;" +
                    "-fx-cursor: hand;");
            returnBtn.setOnAction(e -> handleReturnBook(b, book));

            ImageView cover = new ImageView(new Image(getClass().getResourceAsStream(book.getCoverImage())));
            cover.setFitWidth(180);
            cover.setFitHeight(270);
            cover.setPreserveRatio(true);
            cover.setStyle("-fx-effect: dropshadow(gaussian, #5e81ac88, 9, 0.15, 0, 0);");

            StackPane imageContainer = new StackPane(cover);
            imageContainer.setPrefSize(180, 270);
            StackPane.setAlignment(returnBtn, Pos.TOP_LEFT);
            imageContainer.getChildren().add(returnBtn);

            Label title = new Label(book.getTitle());
            title.setWrapText(true);
            title.setMaxWidth(180);
            title.setAlignment(Pos.CENTER);
            title.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            VBox card = new VBox(5, imageContainer, title);
            card.setAlignment(Pos.TOP_CENTER);
            card.setPrefWidth(180);
            card.setStyle("-fx-background-color: #EEF1F5; -fx-padding: 10;");

            borrowedBooksGrid.add(card, col, row);
            if (++col == BOOKS_PER_ROW) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Handles book return flow including confirmation,
     * optional rating, points update, and saving changes.
     */
    private void handleReturnBook(BorrowedBook borrowedBook, Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Return");
        confirm.setHeaderText("Return Book");
        confirm.setContentText("Are you sure you want to return '" + book.getTitle() + "'?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK)
            return;

        TextInputDialog ratingDialog = new TextInputDialog();
        ratingDialog.setTitle("Rate Book");
        ratingDialog.setHeaderText("Want to rate this book?");
        ratingDialog.setContentText("Rating (1.0 - 5.0):");

        Optional<String> ratingResult = ratingDialog.showAndWait();
        Double rating = null;
        if (ratingResult.isPresent() && !ratingResult.get().trim().isEmpty()) {
            try {
                rating = Double.parseDouble(ratingResult.get().trim());
                if (rating < 1.0 || rating > 5.0)
                    throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Invalid Rating", "Please enter a number between 1.0 and 5.0.");
                return;
            }
        }

        currentUser.getCurrentBorrowedBooks().removeIf(bb -> bb.getBookId().equals(book.getBookId()));
        currentUser.getOverdueBooks().removeIf(bb -> bb.getBookId().equals(book.getBookId()));
        book.setQuantity(book.getQuantity() + 1);

        if (rating != null) {
            currentUser.addRatingToHistory(rating);
            book.getRating().add(rating);
            recalcBookAverageRating(book);
            recalcUserAverageRating(currentUser); // FIXED HERE
        }

        long overdueDays = ChronoUnit.DAYS.between(borrowedBook.getReturnDate(), LocalDate.now());
        if (overdueDays > 0) {
            currentUser.subtractPoints((int) (overdueDays * 0.5));
        } else {
            currentUser.addPoints(5);
            if (rating != null)
                currentUser.addPoints(3);
        }

        // Save updated user
        List<User> allUsers = UserUtils.loadUsers();
        for (int i = 0; i < allUsers.size(); i++) {
            if (allUsers.get(i).getUserId().equals(currentUser.getUserId())) {
                allUsers.set(i, currentUser);
                break;
            }
        }
        UserUtils.saveUsers(allUsers);

        // Save updated book
        BookUtils.updateBook(book);

        updateDashboard();
        populateBorrowedBooks();

        showAlert(Alert.AlertType.INFORMATION, "Book Returned",
                "Book returned successfully!" + (rating != null ? "\nYou rated it: " + rating : ""));
    }

    private void recalcBookAverageRating(Book book) {
        List<Double> ratings = book.getRating();
        double avg = ratings.isEmpty() ? 0 : ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        book.setAverageRating(avg);
    }

    private void recalcUserAverageRating(User user) {
        List<Double> ratings = user.getBookRatingsHistory();
        if (ratings != null && !ratings.isEmpty()) {
            double avg = ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            avg = Math.round(avg * 100.0) / 100.0;
            user.setBookRatingsHistory(ratings); // This will trigger updateAverageRating()
        } else {
            user.setBookRatingsHistory(new ArrayList<>()); // Ensures average = 0
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleOpenSettings() {
        showAlert(Alert.AlertType.INFORMATION, "Settings", "Settings screen clicked.");
    }
}