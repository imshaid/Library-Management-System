package model;

import java.util.List;

public class Book {
    private final String bookId; // Immutable
    private final String isbn; // Immutable
    private final String title; // Immutable
    private final String author; // Immutable
    private String publisher;
    private int publicationYear;
    private int pages;
    private List<String> genre;
    private String language;
    private String edition;
    private String description;
    private List<Double> rating;
    private double averageRating;
    private int quantity;
    private String coverImage;

    // Constructor
    public Book(String bookId, String isbn, String title, String author,
            String publisher, int publicationYear, int pages, List<String> genre,
            String language, String edition, String description,
            List<Double> rating, double averageRating, int quantity, String coverImage) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.pages = pages;
        this.genre = genre;
        this.language = language;
        this.edition = edition;
        this.description = description;
        this.rating = rating;
        this.averageRating = averageRating;
        this.quantity = quantity;
        this.coverImage = coverImage;
    }

    // Getters (Encapsulation)
    public String getBookId() {
        return bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getPublisher() {
        return publisher;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public int getPages() {
        return pages;
    }

    public List<String> getGenre() {
        return genre;
    }

    public String getLanguage() {
        return language;
    }

    public String getEdition() {
        return edition;
    }

    public String getDescription() {
        return description;
    }

    public List<Double> getRating() {
        return rating;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCoverImage() {
        return coverImage;
    }

    // Setters (Encapsulation)
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(List<Double> rating) {
        this.rating = rating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    // Utility methods
    public int getRatingCount() {
        if (rating == null)
            return 0;
        return rating.size();
    }

    public void addRating(double newRating) {
        this.rating.add(newRating);
        recalculateAverageRating();
    }

    private void recalculateAverageRating() {
        if (rating.isEmpty()) {
            averageRating = 0;
        } else {
            double sum = 0;
            for (Double r : rating) {
                sum += r;
            }
            averageRating = sum / rating.size();
        }
    }
}