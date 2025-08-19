package controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Admin;
import model.Book;
import model.User;
import utils.BookUtils;
import utils.UserUtils;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminDashboardHomeController {

        @FXML
        private Label totalBooksLabel;
        @FXML
        private Label totalAuthorsLabel;
        @FXML
        private Label totalGenresLabel;
        @FXML
        private Label totalUsersLabel;

        @FXML
        private Label totalDueOverdueLabel;
        @FXML
        private Label totalNewUserLabel;
        @FXML
        private Label totalUnavailableBooksLabel;

        @FXML
        private LineChart<Number, Number> userLineChart;
        @FXML
        private VBox legendBox;

        @FXML
        private Label dashboardTitleLabel;

        private Admin currentAdmin;

        // Predefined colors for line chart series (matches your FXML styling)
        private final List<Color> predefinedColors = List.of(
                        Color.web("#1f77b4"),
                        Color.web("#ff7f0e"),
                        Color.web("#2ca02c"),
                        Color.web("#d62728"));

        public void setCurrentAdmin(Admin admin) {
                this.currentAdmin = admin;

                if (dashboardTitleLabel != null) {
                        String name = currentAdmin.getFullName() != null ? currentAdmin.getFullName() : "Admin";
                        dashboardTitleLabel
                                        .setText("Welcome, " + name + " (@" + currentAdmin.getUsername() + ")" + "!");
                }

                loadDashboardStats();
                loadUserStatsCards();
                try {
                        loadUserAnalytics();
                } catch (Exception e) {
                        System.err.println("💥 Error in loadUserAnalytics():");
                        e.printStackTrace();
                }

                userLineChart.setPrefHeight(400);
                userLineChart.setMinHeight(400);
                userLineChart.setMaxHeight(Double.MAX_VALUE);
                VBox.setVgrow(userLineChart, javafx.scene.layout.Priority.ALWAYS);
        }

        private void loadDashboardStats() {
                try {
                        Gson gson = new Gson();
                        Type bookListType = new TypeToken<List<Map<String, Object>>>() {
                        }.getType();
                        List<Map<String, Object>> books = gson.fromJson(new FileReader("src/data/book.json"),
                                        bookListType);

                        int totalBookCopies = books.stream()
                                        .mapToInt(b -> ((Number) b.getOrDefault("quantity", 1)).intValue())
                                        .sum();

                        Set<String> authors = books.stream()
                                        .map(b -> String.valueOf(b.get("author")).trim())
                                        .filter(a -> !a.isEmpty())
                                        .collect(Collectors.toSet());

                        Set<String> genres = books.stream()
                                        .flatMap(b -> {
                                                Object genreObj = b.get("genre");
                                                if (genreObj instanceof List<?>) {
                                                        return ((List<?>) genreObj).stream()
                                                                        .map(Object::toString)
                                                                        .map(String::trim)
                                                                        .filter(g -> !g.isEmpty());
                                                } else if (genreObj instanceof String) {
                                                        return Arrays.stream(((String) genreObj).split(","))
                                                                        .map(String::trim)
                                                                        .filter(g -> !g.isEmpty());
                                                }
                                                return Stream.empty();
                                        })
                                        .map(String::toLowerCase) // Optional: to avoid duplicates like "Fantasy" vs
                                                                  // "fantasy"
                                        .collect(Collectors.toSet());

                        Type userListType = new TypeToken<List<Map<String, Object>>>() {
                        }.getType();
                        List<Map<String, Object>> users = gson.fromJson(new FileReader("src/data/user.json"),
                                        userListType);

                        Set<String> userIds = users.stream()
                                        .map(u -> String.valueOf(u.get("id")).trim())
                                        .filter(id -> id != null && !id.isEmpty())
                                        .collect(Collectors.toSet());

                        totalBooksLabel.setText(String.valueOf(totalBookCopies));
                        totalAuthorsLabel.setText(String.valueOf(authors.size()));
                        totalGenresLabel.setText(String.valueOf(genres.size()));
                        totalUsersLabel.setText(String.valueOf(userIds.size()));

                } catch (Exception e) {
                        e.printStackTrace();
                        totalBooksLabel.setText("0");
                        totalAuthorsLabel.setText("0");
                        totalGenresLabel.setText("0");
                        totalUsersLabel.setText("0");
                }
        }

        private void loadUserStatsCards() {
                if (currentAdmin == null) {
                        totalDueOverdueLabel.setText("0/0");
                        totalNewUserLabel.setText("0");
                        totalUnavailableBooksLabel.setText("0");
                        return;
                }

                List<User> users = UserUtils.loadUsers();
                List<Book> books = BookUtils.getAllBooks();

                long unavailableBooks = books.stream()
                                .filter(book -> book.getQuantity() == 0)
                                .count();

                LocalDate today = LocalDate.now();

                // Count books due today (returnDate == today)
                long dueTodayBooks = users.stream()
                                .flatMap(u -> u.getCurrentBorrowedBooks().stream())
                                .filter(b -> b.getReturnDate().isEqual(today))
                                .count();

                // Count books overdue (returnDate < today)
                long overdueBooks = users.stream()
                                .flatMap(u -> u.getCurrentBorrowedBooks().stream())
                                .filter(b -> b.getReturnDate().isBefore(today))
                                .count();

                // Total new users registered in last 7 days
                long newUsersLast7Days = users.stream()
                                .filter(u -> ChronoUnit.DAYS.between(
                                                u.getRegisteredDate().toInstant()
                                                                .atZone(ZoneId.systemDefault())
                                                                .toLocalDate(),
                                                today) <= 7)
                                .count();

                // Display "due/overdue" as "dueTodayBooks / overdueBooks"
                totalDueOverdueLabel.setText(dueTodayBooks + "/" + overdueBooks);
                totalNewUserLabel.setText(String.valueOf(newUsersLast7Days));
                totalUnavailableBooksLabel.setText(String.valueOf(unavailableBooks));
        }

        private void loadUserAnalytics() {
                userLineChart.getData().clear();

                List<User> users = UserUtils.loadUsers();
                List<Book> books = BookUtils.getAllBooks();

                // Prepare Y-axis
                NumberAxis yAxis = (NumberAxis) userLineChart.getYAxis();
                int tickUnit = 20;
                yAxis.setTickUnit(tickUnit);
                yAxis.setAutoRanging(false);
                yAxis.setLowerBound(0);

                List<XYChart.Series<Number, Number>> allSeries = new ArrayList<>();

                // --- Top Borrowers (Descending) ---
                int maxBorrowed = addUserSeries("Top Borrowers", users.stream()
                                .sorted(Comparator.comparingInt(User::getBorrowedCount).reversed())
                                .limit(10)
                                .toList(),
                                User::getBorrowedCount, allSeries);

                // --- Top Raters (Descending) ---
                List<User> topRaters = users.stream()
                                .sorted(Comparator.comparingInt((User u) -> u.getBookRatingsHistory().size())
                                                .reversed())
                                .limit(10)
                                .toList();

                int maxRatings = addUserSeries("Top Raters", topRaters,
                                u -> u.getBookRatingsHistory().size(), allSeries);

                // --- Top Points (Descending) ---
                List<User> topPointUsers = users.stream()
                                .sorted(Comparator.comparingInt((User u) -> u.getPoints().getCurrent()).reversed())
                                .limit(10)
                                .toList();

                int maxPoints = addUserSeries("Top Points", topPointUsers,
                                u -> u.getPoints().getCurrent(), allSeries);

                // --- Top Rated Books (Descending) ---
                List<Book> topRatedBooks = books.stream()
                                .sorted(Comparator.comparingInt((Book b) -> b.getRating().size()).reversed())
                                .limit(10)
                                .toList();

                int maxBookRatings = addBookSeries("Top Rated Books", topRatedBooks,
                                b -> b.getRating().size(), allSeries);

                // --- Y-Axis Upper Bound Calculation with Extra Space ---
                int globalMax = Collections.max(List.of(maxBorrowed, maxRatings, maxPoints, maxBookRatings));
                int roundedMax = ((globalMax + tickUnit - 1) / tickUnit + 1) * tickUnit;
                yAxis.setUpperBound(roundedMax);

                userLineChart.getData().addAll(allSeries);

                // Assign colors explicitly to each series' line and data points
                for (int i = 0; i < allSeries.size(); i++) {
                        XYChart.Series<Number, Number> series = allSeries.get(i);
                        Color color = predefinedColors.get(i % predefinedColors.size());

                        // Style series line (path)
                        Node seriesLine = series.getNode();
                        if (seriesLine != null) {
                                seriesLine.setStyle("-fx-stroke: " + toHex(color) + ";");
                        }

                        // Style each data point (symbol) to match the series color
                        for (XYChart.Data<Number, Number> data : series.getData()) {
                                Node symbol = data.getNode();
                                if (symbol != null) {
                                        symbol.setStyle(
                                                        "-fx-background-color: " + toHex(color) + ", white;" +
                                                                        "-fx-background-insets: 0, 2;" +
                                                                        "-fx-background-radius: 6px;" +
                                                                        "-fx-padding: 5px;");
                                }
                        }
                }

                buildCustomLegend(allSeries);
        }

        private int addUserSeries(String name, List<User> users, java.util.function.ToIntFunction<User> extractor,
                        List<XYChart.Series<Number, Number>> allSeries) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(name);
                AtomicInteger rank = new AtomicInteger(1);
                users.forEach(user -> {
                        int value = extractor.applyAsInt(user);
                        XYChart.Data<Number, Number> data = new XYChart.Data<>(rank.getAndIncrement(), value);
                        data.setNode(createTooltipNode(user.getFullName() + "\n" + name + ": " + value));
                        series.getData().add(data);
                });
                allSeries.add(series);
                return users.stream().mapToInt(extractor).max().orElse(10);
        }

        private int addBookSeries(String name, List<Book> books, java.util.function.ToIntFunction<Book> extractor,
                        List<XYChart.Series<Number, Number>> allSeries) {
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(name);
                AtomicInteger rank = new AtomicInteger(1);
                books.forEach(book -> {
                        int value = extractor.applyAsInt(book);
                        XYChart.Data<Number, Number> data = new XYChart.Data<>(rank.getAndIncrement(), value);
                        data.setNode(createTooltipNode(book.getTitle() + "\n" + name + ": " + value));
                        series.getData().add(data);
                });
                allSeries.add(series);
                return books.stream().mapToInt(extractor).max().orElse(10);
        }

        private void buildCustomLegend(List<XYChart.Series<Number, Number>> seriesList) {
                legendBox.getChildren().clear();
                legendBox.setAlignment(Pos.CENTER);

                for (int i = 0; i < seriesList.size(); i++) {
                        XYChart.Series<Number, Number> series = seriesList.get(i);
                        Color color = predefinedColors.get(i % predefinedColors.size());

                        Region colorBox = new Region();
                        colorBox.setPrefSize(14, 14);
                        colorBox.setMinSize(14, 14);
                        colorBox.setMaxSize(14, 14);
                        colorBox.setStyle("-fx-background-color: " + toHex(color) +
                                        "; -fx-border-color: transparent; -fx-border-radius: 0; -fx-background-radius: 0;");

                        Label label = new Label(series.getName());
                        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

                        HBox item = new HBox(5, colorBox, label);
                        item.setAlignment(Pos.CENTER_LEFT);

                        legendBox.getChildren().add(item);
                }
                legendBox.setSpacing(20);
        }

        private String toHex(Color color) {
                return String.format("#%02X%02X%02X",
                                (int) (color.getRed() * 255),
                                (int) (color.getGreen() * 255),
                                (int) (color.getBlue() * 255));
        }

        private Node createTooltipNode(String tooltipText) {
                javafx.scene.layout.StackPane dot = new javafx.scene.layout.StackPane();
                dot.setPrefSize(10, 10);
                dot.setStyle("-fx-background-radius: 50%; -fx-background-color: #3a7bd5;"); // example color

                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(tooltipText);

                tooltip.setStyle(
                                "-fx-background-color: rgba(255,255,255,0.75);" +
                                                "-fx-background-radius: 1;" +
                                                "-fx-text-fill: #222;" +
                                                "-fx-font-size: 12px;" +
                                                "-fx-padding: 6;");

                removeTooltipDelay(tooltip);

                javafx.scene.control.Tooltip.install(dot, tooltip);

                return dot;
        }

        private void removeTooltipDelay(javafx.scene.control.Tooltip tooltip) {
                try {
                        Field behaviorField = tooltip.getClass().getDeclaredField("BEHAVIOR");
                        behaviorField.setAccessible(true);
                        Object behavior = behaviorField.get(tooltip);

                        Field activationTimerField = behavior.getClass().getDeclaredField("activationTimer");
                        activationTimerField.setAccessible(true);
                        Timeline activationTimer = (Timeline) activationTimerField.get(behavior);
                        activationTimer.stop();

                        Field hideTimerField = behavior.getClass().getDeclaredField("hideTimer");
                        hideTimerField.setAccessible(true);
                        Timeline hideTimer = (Timeline) hideTimerField.get(behavior);
                        hideTimer.stop();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        private String capitalize(String input) {
                if (input == null || input.isEmpty())
                        return input;
                return input.substring(0, 1).toUpperCase() + input.substring(1);
        }
}