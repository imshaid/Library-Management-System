package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Book;

import java.io.InputStream;

public class BookDetailsDialogController {

    @FXML
    private ImageView coverImage;
    @FXML
    private Label titleLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label publisherLabel;
    @FXML
    private Label publicationYear;
    @FXML
    private Label pages;
    @FXML
    private Label languageLabel;
    @FXML
    private Label editionLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label quantityLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label descriptionLabel;

    private Book book;

    public void setBook(Book book) {
        this.book = book;

        // Load cover image with fallback
        try {
            InputStream imgStream = getClass().getResourceAsStream(book.getCoverImage());
            if (imgStream != null) {
                coverImage.setImage(new Image(imgStream));
            } else {
                System.err.println("Image not found: " + book.getCoverImage());
                InputStream fallbackStream = getClass().getResourceAsStream("/assets/covers/default.jpg");
                if (fallbackStream != null) {
                    coverImage.setImage(new Image(fallbackStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load image: " + book.getCoverImage());
            e.printStackTrace();
        }

        // Populate labels
        titleLabel.setText(book.getTitle());
        authorLabel.setText(book.getAuthor() != null ? book.getAuthor() : "Unknown Author");
        publisherLabel.setText(book.getPublisher() != null ? book.getPublisher() : "Unknown Publisher");
        publicationYear.setText(String.valueOf(book.getPublicationYear()));
        pages.setText(String.valueOf(book.getPages()));
        languageLabel.setText(book.getLanguage() != null ? book.getLanguage() : "N/A");
        editionLabel.setText(book.getEdition() != null ? book.getEdition() : "N/A");
        genreLabel.setText(String.join(", ", book.getGenre()));
        quantityLabel.setText(String.valueOf(book.getQuantity()));
        ratingLabel.setText(String.format("★ %.1f (%d ratings)", book.getAverageRating(), book.getRating().size()));
        descriptionLabel.setText(book.getDescription());
    }
}