package utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UserUtils {
    private static final String FILE_PATH = "src/data/user.json";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .setPrettyPrinting()
            .create();

    public static List<User> loadUsers() {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type listType = new TypeToken<List<User>>() {
            }.getType();
            List<User> users = gson.fromJson(reader, listType);
            System.out.println("✅ Loaded " + users.size() + " users.");
            return users;
        } catch (Exception e) {
            System.err.println("❌ Failed to load users:");
            e.printStackTrace();
            return List.of();
        }
    }

    public static User findUserByIdOrUsernameOrEmail(String input) {
        List<User> users = loadUsers();
        for (User user : users) {
            if (input.equalsIgnoreCase(user.getUserId()) ||
                    input.equalsIgnoreCase(user.getUsername()) ||
                    input.equalsIgnoreCase(user.getEmail())) {
                return user;
            }
        }
        return null;
    }

    // Save a single user (append and overwrite whole file)
    public static void saveUser(User user) {
        List<User> users = new ArrayList<>(loadUsers());
        users.add(user);

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
            System.out.println("✅ User saved: " + user.getUsername());
        } catch (IOException e) {
            System.err.println("❌ Failed to save user:");
            e.printStackTrace();
        }
    }

    // Save a list of users (overwrite entire file)
    public static void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(users, writer);
            System.out.println("✅ Saved " + users.size() + " users.");
        } catch (IOException e) {
            System.err.println("❌ Failed to save users:");
            e.printStackTrace();
        }
    }

    public static boolean deleteUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            System.err.println("⚠️ Invalid userId for deletion.");
            return false;
        }

        List<User> users = loadUsers();
        int beforeSize = users.size();
        boolean removed = users.removeIf(u -> userId.equals(u.getUserId()));

        if (removed && users.size() < beforeSize) {
            try (FileWriter writer = new FileWriter(FILE_PATH)) {
                gson.toJson(users, writer);
                System.out.println("🗑️ User " + userId + " deleted successfully.");
                return true;
            } catch (IOException e) {
                System.err.println("❌ Failed to write updated user list:");
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ No user found with ID: " + userId);
        }
        return false;
    }
}