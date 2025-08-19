package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Book;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

public class BookLoader {

    public static List<Book> loadBooks() {
        try (InputStreamReader reader = new InputStreamReader(
                BookLoader.class.getResourceAsStream("../data/book.json"))) {

            Type bookListType = new TypeToken<List<Book>>() {
            }.getType();
            return new Gson().fromJson(reader, bookListType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}