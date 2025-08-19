package model;

import java.util.*;

public class User extends Person {
    private int borrowedCount;
    private Points points;
    private String[] badges;
    private List<Double> bookRatingsHistory;
    private double averageRating;
    private List<BorrowedBook> currentBorrowedBooks;
    private List<BorrowedBook> overdueBooks;
    private String profilePicPath;

    public User() {
        super(); // Required for Gson
    }

    // Constructor
    public User(String userId, String username, String email, String passwordHash, String fullName,
            Date registeredDate) {
        super(userId, username, email, passwordHash, fullName, registeredDate, "", "");
        this.borrowedCount = 0;
        this.points = new Points(0, 100);
        this.badges = new String[0];
        this.bookRatingsHistory = new ArrayList<>();
        this.averageRating = 0.0;
        this.currentBorrowedBooks = new ArrayList<>();
        this.overdueBooks = new ArrayList<>();
        this.profilePicPath = "src/assets/user/default.png";
    }

    @Override
    public String getRole() {
        return "User";
    }

    public String getUserId() {
        return getId();
    }

    public int getBorrowedCount() {
        return borrowedCount;
    }

    public Points getPoints() {
        return points;
    }

    public String[] getBadges() {
        return badges;
    }

    public List<Double> getBookRatingsHistory() {
        return bookRatingsHistory;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public List<BorrowedBook> getCurrentBorrowedBooks() {
        return currentBorrowedBooks;
    }

    public List<BorrowedBook> getOverdueBooks() {
        return overdueBooks;
    }

    public String getProfilePicPath() {
        return profilePicPath;
    }

    public void setBorrowedCount(int borrowedCount) {
        this.borrowedCount = borrowedCount;
    }

    public void setPoints(Points points) {
        this.points = points;
    }

    public void setBadges(String[] badges) {
        this.badges = badges;
    }

    public void setBookRatingsHistory(List<Double> bookRatingsHistory) {
        this.bookRatingsHistory = bookRatingsHistory;
        updateAverageRating();
    }

    public void setCurrentBorrowedBooks(List<BorrowedBook> currentBorrowedBooks) {
        this.currentBorrowedBooks = currentBorrowedBooks;
    }

    public void setOverdueBooks(List<BorrowedBook> overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public void setProfilePicPath(String profilePicPath) {
        this.profilePicPath = profilePicPath;
    }

    public void incrementBorrowedCount() {
        borrowedCount++;
    }

    public void addRatingToHistory(double rating) {
        this.bookRatingsHistory.add(rating);
        updateAverageRating();
    }

    public void recalcAverageRating() {
        updateAverageRating();
    }

    private void updateAverageRating() {
        if (!bookRatingsHistory.isEmpty()) {
            double sum = bookRatingsHistory.stream().mapToDouble(Double::doubleValue).sum();
            this.averageRating = Math.round((sum / bookRatingsHistory.size()) * 100.0) / 100.0;
            System.out.println("Updated Average Rating: " + this.averageRating);
        } else {
            this.averageRating = 0.0;
        }
    }

    public void addPoints(int value) {
        int newPoints = this.points.getCurrent() + value;
        this.points.setCurrent(newPoints);
        if (newPoints > this.points.getMax()) {
            this.points.setMax(newPoints);
        }
    }

    public void subtractPoints(int value) {
        int newPoints = this.points.getCurrent() - value;
        this.points.setCurrent(Math.max(0, newPoints));
    }

    public void setCurrentPoints(int value) {
        this.points.setCurrent(Math.min(value, this.points.getMax()));
    }

    public void setMaxPoints(int value) {
        this.points.setMax(value);
    }
}