package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import model.Book;
import model.Intent;
import model.User;
import utils.BookUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class UserDashboardController {

    @FXML
    private AnchorPane mainContent;
    @FXML
    private StackPane chatbotContainer;
    @FXML
    private ImageView botIcon;
    @FXML
    private TextArea userMessageField;
    @FXML
    private VBox chatBox;
    @FXML
    private ScrollPane chatScrollPane;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button booksButton;

    private User currentUser;
    private final List<HBox> chatHistory = new ArrayList<>();
    private final List<Book> allBooks = BookUtils.getAllBooks();
    private final ChatbotDataLoader dataLoader = new ChatbotDataLoader();
    private final Random random = new Random();
    private final String[] fallbackResponses = {
            "I’m still learning 🧠. Try asking about books, borrowing, or authors!",
            "Hmm, I didn’t get that quite right. Maybe rephrase?",
            "I'm here for library questions—ask me about genres, books, or borrowing!",
            "Oops! That’s not in my book yet. 😉"
    };

    @FXML
    public void initialize() {
        botIcon.setImage(new Image(getClass().getResource("/assets/chatBot.png").toExternalForm()));
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadDashboardHome();
        setActiveButton(dashboardButton);
    }

    private void loadDashboardHome() {
        loadViewWithUser("/fxml/user/dashboardHome.fxml");
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active style from all buttons
        dashboardButton.getStyleClass().remove("active-tab");
        settingsButton.getStyleClass().remove("active-tab");
        booksButton.getStyleClass().remove("active-tab");

        // Add active style to the selected one
        if (!activeBtn.getStyleClass().contains("active-tab")) {
            activeBtn.getStyleClass().add("active-tab");
        }
    }

    @FXML
    private void handleDashboardClick(ActionEvent event) {
        loadDashboardHome();
        setActiveButton(dashboardButton);
    }

    @FXML
    private void handleBooksClick(ActionEvent event) {
        loadViewWithUser("/fxml/user/booksView.fxml");
        setActiveButton(booksButton);
    }

    @FXML
    private void handleSettingsClick(ActionEvent event) {
        loadViewWithUser("/fxml/user/settingsView.fxml");
        setActiveButton(settingsButton);
    }

    private void loadViewWithUser(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("setCurrentUser", User.class).invoke(controller, currentUser);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
                }
            }

            mainContent.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("❌ Failed to load FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            chatHistory.clear();
            MainController.getInstance().resizeWindow(900, 636);
            MainController.getInstance().loadContent("/fxml/launch.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleChatbot(MouseEvent event) {
        if (chatbotContainer != null) {
            boolean isVisible = chatbotContainer.isVisible();
            chatbotContainer.setVisible(!isVisible);

            if (!isVisible) {
                chatBox.getChildren().clear();
                chatBox.getChildren().addAll(chatHistory);

                if (chatHistory.isEmpty()) {
                    addMessage("Hello there! How can I help you today?", false);
                }
            }
        }
    }

    @FXML
    private void handleSendMessage(ActionEvent event) {
        String userMessage = userMessageField.getText().trim();
        if (!userMessage.isEmpty()) {
            addMessage(userMessage, true);
            userMessageField.clear();

            String botReply = generateBotReply(userMessage);
            addMessage(botReply, false);
        }
    }

    private void addMessage(String text, boolean isUser) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(isUser ? 120 : 160);
        messageLabel.setStyle(
                "-fx-padding: 8 10 8 10; -fx-background-radius: 10; -fx-font-size: 12;" +
                        (isUser
                                ? "-fx-background-color: #457b7ad6; -fx-text-fill: white; -fx-alignment: center-right;"
                                : "-fx-background-color: #e5e9f0; -fx-text-fill: #2e3440;"));

        HBox wrapper = new HBox(messageLabel);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        wrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(wrapper);
        chatHistory.add(wrapper);

        Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }

    private String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }

    private String getBooksByAuthor(String author) {
        StringBuilder result = new StringBuilder("\n\n📚 Books by this author:\n");
        boolean found = false;
        for (Book book : allBooks) {
            if (book.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                result.append("• ").append(book.getTitle()).append("\n");
                found = true;
            }
        }
        return found ? result.toString() : "\n\n🔍 No books found by this author in the library.";
    }

    private String getBooksByGenre(String genre) {
        StringBuilder result = new StringBuilder("\n\n📚 Books in this genre:\n");
        boolean found = false;

        for (Book book : allBooks) {
            for (String g : book.getGenre()) {
                if (g.toLowerCase().contains(genre.toLowerCase())) {
                    result.append("• ").append(book.getTitle())
                            .append(" by ").append(book.getAuthor()).append("\n");
                    found = true;
                    break;
                }
            }
        }

        return found ? result.toString() : "\n\n🔍 No books found in this genre.";
    }

    private String convertTagToGenreName(String tag) {
        String[] words = tag.split("_");
        StringBuilder genre = new StringBuilder();
        for (String word : words) {
            genre.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1)).append(" ");
        }
        return genre.toString().trim();
    }

    private boolean messageMentionsGenre(String message, String genreName) {
        return message.toLowerCase().contains(genreName.toLowerCase());
    }

    private boolean isGenreTag(String tag) {
        List<String> genreTags = Arrays.asList(
                "dystopian_fiction", "thriller", "mystery", "dystopian", "adventure",
                "science_fiction", "classic_fiction", "philosophical_fiction", "fantasy",
                "drama", "historical_fiction", "philosophy", "horror", "classic",
                "gothic_mystery", "fiction", "coming_of_age", "memoir", "non_fiction",
                "romance", "literary", "history", "political_satire", "post_apocalyptic",
                "post_apocalyptic_fiction", "psychological_thriller", "romantic_thriller",
                "suspense", "crime", "supernatural", "heartwarming_fiction",
                "gothic_horror", "coming_of_age_fiction", "techno_thriller",
                "biography", "science_thriller", "dark_romance", "mythology",
                "egyptian_mythology", "greek_mythology", "military", "paranormal_romance",
                "ya", "historical_fantasy", "fae", "dark_fantasy", "dark_romantasy",
                "mythic", "post_apocalyptic_fantasy", "ya_fantasy");
        return genreTags.contains(tag.toLowerCase());
    }

    private String extractAuthorFromTag(String tag) {
        if (tag.startsWith("author_info_")) {
            return tag.replace("author_info_", "").replace("_", " ").trim();
        }
        return "";
    }

    private String generateBotReply(String message) {
        String normalizedMessage = normalize(message);
        double bestScore = 0;
        Intent bestIntent = null;

        for (Intent intent : dataLoader.getIntents()) {
            for (String pattern : intent.getPatterns()) {
                String normalizedPattern = normalize(pattern);
                double score = getSimilarity(normalizedMessage, normalizedPattern);
                if (score > bestScore) {
                    bestScore = score;
                    bestIntent = intent;
                }
            }
        }

        if (bestIntent != null && bestScore >= 0.6) {
            List<String> responses = bestIntent.getResponses();
            String response = responses.get(random.nextInt(responses.size()));

            String tag = bestIntent.getTag();
            String genreName = convertTagToGenreName(tag);

            // Check if the user's message actually includes the genre name
            if (isGenreTag(tag) && messageMentionsGenre(message, genreName)) {
                String books = getBooksByGenre(genreName);
                if (!books.contains("No books found")) {
                    response += books;
                }
                return response;
            }

            // Author-based detection
            if (tag.startsWith("author_info_") || normalizedMessage.contains("author")
                    || normalizedMessage.contains("by")) {
                String author = extractAuthorFromTag(tag);
                response += getBooksByAuthor(author);
                return response;
            }

            return response;
        }

        return fallbackResponses[random.nextInt(fallbackResponses.length)];
    }

    private double getSimilarity(String input, String pattern) {
        String[] inputWords = input.split("\\s+");
        String[] patternWords = pattern.split("\\s+");

        int matches = 0;
        for (String iw : inputWords) {
            for (String pw : patternWords) {
                if (iw.equals(pw)) {
                    matches++;
                    break;
                }
            }
        }

        return (double) matches / patternWords.length;
    }
}