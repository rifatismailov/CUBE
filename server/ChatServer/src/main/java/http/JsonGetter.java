package http;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class JsonGetter {

    /**
     * Парсер JSON-повідомлень.
     *
     * @param jsonMessage Повідомлення у форматі JSON.
     * @return Мапа з полями повідомлення.
     */
    public Map<String, String> parseMessage(String jsonMessage) {
        Map<String, String> data = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonMessage);
            data.put("senderId", jsonObject.optString("senderId", ""));
            data.put("receiverId", jsonObject.optString("receiverId", ""));
            data.put("operation", jsonObject.optString("operation", ""));
            data.put("message", jsonObject.optString("message", ""));
            data.put("messageId", jsonObject.optString("messageId", ""));

            // Перевірка на вкладений JSON у "message"
            String messageContent = jsonObject.optString("message", "");
            if (messageContent.startsWith("{")) {
                JSONObject nestedMessage = new JSONObject(messageContent);
                data.put("publicKey", nestedMessage.optString("publicKey", ""));
            }

            if (jsonMessage.contains("fileUrl")) {
                data.put("fileUrl", jsonObject.optString("fileUrl", ""));
                data.put("fileHash", jsonObject.optString("fileHash", ""));
            }
        } catch (JSONException e) {
            System.out.println("JSON Parsing Error: " + e.getMessage());
        }
        return data;
    }



    public String getUserID(String message) {
        if (message == null || !message.startsWith("REGISTER:")) {
            // Логування помилки
            System.err.println("Invalid message format: " + message); // Вивести помилку
            return null; // Або викинути виняток
        }

        String jsonString = message.replaceAll("REGISTER:", "");

        try {
            // Перевірка, чи є рядок валідним JSON
            if (jsonString.trim().startsWith("{")) {
                JSONObject jsonObject = new JSONObject(jsonString);
                String userId = jsonObject.optString("userId", null);
                if (userId == null || userId.isEmpty()) {
                    System.err.println("userId is missing or empty.");
                    return null;
                }
                return userId;
            } else {
                System.err.println("Invalid JSON format: " + jsonString); // Логування невалідного формату
                return null;
            }
        } catch (JSONException e) {
            // Логування помилки парсингу JSON
            System.err.println("Error parsing JSON: " + jsonString + " - " + e.getMessage()); // Логування помилки парсингу
            return null; // Або викинути виняток
        }
    }
}
