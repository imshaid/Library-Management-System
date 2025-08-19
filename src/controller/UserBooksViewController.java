package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.Book;
import model.User;
import utils.BookUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserBooksViewController {

    @FXML
    private GridPane bookGrid;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortComboBox;

    private User currentUser;
    private List<Book> allBooks = new ArrayList<>();
    private String lastSearchQuery = "";

    @FXML
    public void initialize() {
        sortComboBox.getItems().addAll(
                "Default",
                "Title A → Z",
                "Title Z → A");
        sortComboBox.setValue("Default");

        searchField.setOnAction(event -> handleSearch());

        allBooks = BookUtils.getAllBooks(); // Initial full list
        applyFilterAndSort(); // Display
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        allBooks = BookUtils.getAllBooks(); // Reload fresh data
        applyFilterAndSort();
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    // 🔁 Unified Logic
    private void applyFilterAndSort() {
        String query = lastSearchQuery.toLowerCase().trim();
        List<Book> filtered = new ArrayList<>(allBooks); // Copy original list

        if (!query.isEmpty()) {
            filtered = filtered.stream()
                    .filter(book -> book.getBookId().toLowerCase().contains(query) ||
                            book.getTitle().toLowerCase().contains(query) ||
                            book.getAuthor().toLowerCase().contains(query) ||
                            book.getPublisher().toLowerCase().contains(query) ||
                            book.getGenre().stream().anyMatch(g -> g.toLowerCase().contains(query)))
                    .toList();
        }

        String selectedSort = sortComboBox.getValue();
        if (selectedSort != null) {
            switch (selectedSort) {
                case "Title A → Z" -> filtered = filtered.stream()
                        .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                        .toList();
                case "Title Z → A" -> filtered = filtered.stream()
                        .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER).reversed())
                        .toList();
                default -> {
                } // Do nothing on "Default"
            }
        }

        displayBooks(filtered);
    }

    // 🔍 Called when clicking Search
    @FXML
    private void handleSearch() {
        lastSearchQuery = searchField.getText(); // Store last search
        applyFilterAndSort();
    }

    // 🔃 Called when sort selection changes
    @FXML
    private void handleSortSelection() {
        applyFilterAndSort();
    }

    // 🧽 Refresh UI when needed
    public void reloadBooks() {
        allBooks = BookUtils.getAllBooks();
        applyFilterAndSort();
    }

    public void refreshUserData() {
        // Placeholder
    }

    // 📦 Show books
    private void displayBooks(List<Book> books) {
        bookGrid.getChildren().clear();

        int col = 0, row = 0;
        for (Book book : books) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/bookCard.fxml"));
                HBox card = loader.load();

                UserBookCardController controller = loader.getController();
                controller.setBook(book);
                controller.setUserBooksViewController(this);

                bookGrid.add(card, col, row);
                col++;
                if (col == 2) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Books displayed: " + books.size());
    }
}