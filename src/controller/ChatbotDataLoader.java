package controller;

import com.google.gson.Gson;
import model.IntentWrapper;
import model.Intent;

import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class ChatbotDataLoader {

    private List<Intent> intents;

    public ChatbotDataLoader() {
        try (Reader reader = new FileReader("src/data/chatbot-data.json")) {
            Gson gson = new Gson();
            IntentWrapper wrapper = gson.fromJson(reader, IntentWrapper.class);
            this.intents = wrapper.getIntents();
            System.out.println("Loaded intents: " + (intents != null ? intents.size() : "null"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Intent> getIntents() {
        return intents;
    }
}