package utils;

import com.google.gson.reflect.TypeToken;
import model.Admin;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AdminUtils {
    private static final String ADMIN_FILE = "src/data/admin.json";

    public static List<Admin> loadAdmins() {
        try (FileReader reader = new FileReader(ADMIN_FILE)) {
            Type listType = new TypeToken<List<Admin>>() {
            }.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void saveAdmins(List<Admin> admins) {
        try (FileWriter writer = new FileWriter(ADMIN_FILE)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(admins, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}