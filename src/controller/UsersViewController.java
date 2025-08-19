package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import model.User;
import utils.UserUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UsersViewController {

    @FXML
    private GridPane userGrid;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    private List<User> allUsers = new ArrayList<>();
    private String lastSearchQuery = "";

    private static final int USERS_PER_ROW = 4;

    @FXML
    public void initialize() {
        // Sort options
        sortComboBox.getItems().addAll(
                "Default",
                "Name A → Z",
                "Name Z → A",
                "Username A → Z",
                "Username Z → A");
        sortComboBox.setValue("Default");

        // Event handlers
        searchField.setOnAction(e -> handleSearch());
        sortComboBox.setOnAction(e -> handleSortSelection());

        // Load initial data and display
        allUsers = UserUtils.loadUsers();
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        String query = lastSearchQuery.toLowerCase().trim();
        List<User> filtered = new ArrayList<>(allUsers);

        if (!query.isEmpty()) {
            filtered = filtered.stream()
                    .filter(u -> u.getUserId().toLowerCase().contains(query) ||
                            u.getUsername().toLowerCase().contains(query) ||
                            u.getFullName().toLowerCase().contains(query) ||
                            u.getEmail().toLowerCase().contains(query) ||
                            u.getPhoneNumber().toLowerCase().contains(query))
                    .toList();
        }

        String selectedSort = sortComboBox.getValue();
        if (selectedSort != null) {
            switch (selectedSort) {
                case "Name A → Z" -> filtered = filtered.stream()
                        .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER))
                        .toList();
                case "Name Z → A" -> filtered = filtered.stream()
                        .sorted(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER).reversed())
                        .toList();
                case "Username A → Z" -> filtered = filtered.stream()
                        .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                        .toList();
                case "Username Z → A" -> filtered = filtered.stream()
                        .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER).reversed())
                        .toList();
                default -> {
                    // "Default" - no sorting
                }
            }
        }

        displayUsers(filtered);
    }

    @FXML
    private void handleSearch() {
        lastSearchQuery = searchField.getText();
        applyFilterAndSort();
    }

    @FXML
    private void handleSortSelection() {
        applyFilterAndSort();
    }

    public void reloadUsers() {
        allUsers = UserUtils.loadUsers();
        applyFilterAndSort();
    }

    private void displayUsers(List<User> users) {
        userGrid.getChildren().clear();

        int col = 0, row = 0;
        for (User user : users) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserCard.fxml"));
                Node card = loader.load();

                UserCardController controller = loader.getController();
                controller.setUser(user, this::reloadUsers);

                userGrid.add(card, col, row);
                col++;
                if (col == USERS_PER_ROW) {
                    col = 0;
                    row++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}