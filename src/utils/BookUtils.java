package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Book;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BookUtils {
    private static final String BOOKS_FILE = "src/data/book.json";

    public static List<Book> getAllBooks() {
        try (FileReader reader = new FileReader(BOOKS_FILE)) {
            Type listType = new TypeToken<ArrayList<Book>>() {
            }.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveAllBooks(List<Book> books) {
        try (FileWriter writer = new FileWriter(BOOKS_FILE)) {
            new Gson().toJson(books, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteBookById(String bookId) {
        List<Book> books = getAllBooks();
        books.removeIf(book -> book.getBookId().equals(bookId));
        saveAllBooks(books);
    }

    public static Book findBookById(String bookId) {
        List<Book> books = getAllBooks();
        for (Book book : books) {
            if (book.getBookId().equals(bookId)) {
                return book;
            }
        }
        return null;
    }

    // NEW method to update a book in the list and save
    public static void updateBook(Book updatedBook) {
        List<Book> books = getAllBooks();
        boolean found = false;

        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getBookId().equals(updatedBook.getBookId())) {
                books.set(i, updatedBook);
                found = true;
                break;
            }
        }

        if (found) {
            saveAllBooks(books);
        } else {
            System.err.println("Book with ID " + updatedBook.getBookId() + " not found for update.");
        }
    }
}