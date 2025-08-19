package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import model.Book;
import utils.BookUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BookUpdateDialogController {

    @FXML
    private TextField publisherField;
    @FXML
    private TextField publicationYearField;
    @FXML
    private TextField pagesField;
    @FXML
    private TextField genreField;
    @FXML
    private TextField languageField;
    @FXML
    private TextField editionField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextArea descriptionField;

    @FXML
    private Label wordCountLabel;

    @FXML
    private TextField coverImageNameField;
    @FXML
    private Button selectImageButton;

    private Book book;
    private BooksViewController parentController;

    // Stores the image file selected by Browse button
    private File selectedImageFile = null;

    @FXML
    public void initialize() {
        // Setup the Browse button to open file chooser
        selectImageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cover Image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

            Window window = selectImageButton.getScene().getWindow();
            File file = fileChooser.showOpenDialog(window);
            if (file != null) {
                selectedImageFile = file;
                coverImageNameField.setText(file.getName());
            }
        });

        // Initialize word count label
        updateWordCount();

        // Add listener for live word count update
        descriptionField.textProperty().addListener((obs, oldText, newText) -> updateWordCount());
    }

    private void updateWordCount() {
        String text = descriptionField.getText();
        if (text == null || text.trim().isEmpty()) {
            wordCountLabel.setText("Words: 0");
            return;
        }
        String[] words = text.trim().split("\\s+");
        wordCountLabel.setText("Words: " + words.length);
    }

    public void setBook(Book book) {
        this.book = book;

        // Populate the fields with existing book data
        publisherField.setText(book.getPublisher());
        publicationYearField.setText(String.valueOf(book.getPublicationYear()));
        pagesField.setText(String.valueOf(book.getPages()));
        genreField.setText(String.join(", ", book.getGenre()));
        languageField.setText(book.getLanguage());
        editionField.setText(book.getEdition());
        quantityField.setText(String.valueOf(book.getQuantity()));
        descriptionField.setText(book.getDescription());

        // Show current cover image file name (extract filename from path)
        String coverPath = book.getCoverImage();
        if (coverPath != null && !coverPath.isEmpty()) {
            File coverFile = new File(coverPath);
            coverImageNameField.setText(coverFile.getName());
        }

        // Update word count label initially with loaded description
        updateWordCount();
    }

    public void setParentController(BooksViewController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleSave() {
        try {
            // Update book fields from form
            book.setPublisher(publisherField.getText().trim());
            book.setPublicationYear(Integer.parseInt(publicationYearField.getText().trim()));
            book.setPages(Integer.parseInt(pagesField.getText().trim()));

            // Split genres by comma, trim whitespace
            String[] genres = genreField.getText().split(",");
            for (int i = 0; i < genres.length; i++) {
                genres[i] = genres[i].trim();
            }
            book.setGenre(java.util.Arrays.asList(genres));

            book.setLanguage(languageField.getText().trim());
            book.setEdition(editionField.getText().trim());
            book.setQuantity(Integer.parseInt(quantityField.getText().trim()));
            book.setDescription(descriptionField.getText().trim());

            // Handle cover image update if a new image is selected
            if (selectedImageFile != null) {
                // Destination folder (adjust path as needed)
                File destDir = new File("src/assets/covers/");
                if (!destDir.exists())
                    destDir.mkdirs();

                File destFile = new File(destDir, selectedImageFile.getName());

                // Copy selected image to resources folder
                Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update cover image path in book to relative resource path
                book.setCoverImage("/assets/covers/" + selectedImageFile.getName());
            }
            // Else keep existing cover image path as is

            // Update book in data store
            BookUtils.updateBook(book);

            // Notify parent controller to reload books view
            if (parentController != null) {
                parentController.reloadBooks();
            }

            // Success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Book updated successfully!");
            alert.showAndWait();

            // Close the dialog window
            selectImageButton.getScene().getWindow().hide();

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for year, pages, and quantity.");
        } catch (IOException e) {
            showError("Failed to copy cover image: " + e.getMessage());
        } catch (Exception e) {
            showError("Failed to save book: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        // Close dialog window
        selectImageButton.getScene().getWindow().hide();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}