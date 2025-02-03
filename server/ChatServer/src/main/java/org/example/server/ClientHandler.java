package org.example.server;

import org.example.Envelope;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клас для обробки клієнтів, який реалізує багатопоточну логіку роботи з клієнтами
 * через сокети та обмін повідомленнями, включаючи обмін ключами (handshake).
 */
public class ClientHandler implements Runnable, HandshakeMap.HandshakeListener {
    // Зберігає онлайн-користувачів, пов'язаних із їхніми сокетами
    private Map<String, Socket> onlineUsers;

    // Зберігає непрочитані повідомлення для офлайн-користувачів
    private Map<String, List<String>> offlineMessages = new ConcurrentHashMap<>();

    // Список пар користувачів та їхніх ключів
    public static Map<String, String> clientsKey;


    // Сокет для поточного клієнта
    private final Socket clientSocket;

    // Потоки вводу/виводу для роботи із клієнтом
    private BufferedReader in;
    private PrintWriter out;

    // Ідентифікатор користувача
    private String userId;

    // Об'єкт для обробки handshake
    private HandshakeMap handshake;

    final String MESSAGE = "message";
    final String HANDSHAKE = "handshake";
    final String PUBLICKEY = "publicKey";
    final String PRIVATEKEY = "privateKey";
    final String KEY_EXCHANGE = "keyExchange";
    final String PING = "ping";
    final String IMAGE = "image";
    final String FILE = "file";


    /**
     * Конструктор для ініціалізації обробника клієнта.
     *
     * @param socket          Сокет клієнта.
     * @param onlineUsers     Мапа онлайн-користувачів.
     * @param offlineMessages Мапа збережених повідомлень для офлайн-користувачів.
     * @param clientsKey      Список пар користувачів із ключами.
     */
    public ClientHandler(Socket socket, Map<String, Socket> onlineUsers, Map<String, List<String>> offlineMessages, Map<String, String> clientsKey) {
        this.clientSocket = socket;
        this.onlineUsers = onlineUsers;
        this.offlineMessages = offlineMessages;
        this.clientsKey = clientsKey;
        handshake = new HandshakeMap(this);
    }

    /**
     * Головний метод для роботи клієнта, який виконується в окремому потоці.
     * Включає реєстрацію користувача, отримання повідомлень і їх обробку.
     */

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Реєстрація клієнта через JSON-повідомлення
            String line = in.readLine();

            JSONObject jsonObject = new JSONObject(line);
            userId = jsonObject.optString("userId", null);
            // тут буде первірка userId якщо він буде у базі то нічого якщо його нема ми його відключаємо
            // ще треба буде реалізувати підтвердження легетивності користувача кожен абонет буде мати спеціальний код який буде шифруватися його
            // особистим паролем
            if (userId != null && !userId.equals("null")) {
                sendMessage(userId, "REGISTRATION_SUCCESSFUL");
                // Додавання клієнта до списку онлайн-користувачів
                onlineUsers.put(userId, clientSocket);
                System.out.println("Користувач підключився: " + userId);

                // Надсилання офлайн-повідомлень, якщо такі є
                sendOfflineMessages();

                // Основний цикл обробки повідомлень
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    processMessage(inputLine);
                }
            } else {

                //Тут повинна відпрпацювати код умова якого буде якщо ми не отримали під час пікдлюченя userId
                //тоді відправляємо посилання що userId не був отримане та будемо чекати поикне буде отрмане userId
                //поик не буде ідентифіковано userId обміну повідомлен не буде
                System.err.println("Не отриманно userId: " + line+" що є нправильним");
                sendMessage(userId, "REGISTRATION_FAILED");
            }
        } catch (IOException e) {
            System.err.println("Помилка під час отрмиання повідомлення : " + e);

        } catch (JSONException e) {
            System.err.println("Невірний формат повідомлення JSON: " + e);
            try {
                sendMessage(userId, "REGISTRATION_FAILED");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        } finally {
            disconnect();
        }
    }

    /**
     * Метод для обробки вхідних повідомлень.
     *
     * @param jsonMessage Повідомлення у форматі JSON.
     */
    private void processMessage(String jsonMessage) {
        try {
            // Парсимо JSON-повідомлення
            Map<String, String> messageData = parseMessage(jsonMessage);
            String senderId = messageData.get("senderId");
            String receiverId = messageData.get("receiverId");
            String operation = messageData.get("operation");
            String messageId = messageData.get("messageId");

            if (operation.equals(MESSAGE)) {
                // Обробка текстового повідомлення
                sendMessage(receiverId, jsonMessage);
                // відправляємо статус повідомлення в нашому випадку receiverId є відправником
                sendStatus(receiverId,userId, messageId);
                System.out.println("Отримано повідомлення від " + userId + ": " + jsonMessage);

            } else if (operation.equals(IMAGE)) {
                // Обробка текстового повідомлення
                sendMessage(receiverId, jsonMessage);
                sendStatus(receiverId,userId, messageId);
                System.out.println("Отримано повідомлення від " + userId + ": " + jsonMessage);

            } else if (operation.equals(FILE)) {
                // Обробка текстового повідомлення
                sendMessage(receiverId, jsonMessage);
                sendStatus(receiverId,userId, messageId);
                System.out.println("Отримано повідомлення від " + userId + ": " + jsonMessage);

            } else if (operation.equals(HANDSHAKE)) {
                // Обробка обміну ключами
                String message = messageData.get(MESSAGE);
                JSONObject jsonObject = new JSONObject(message);
                String publicKey = jsonObject.getString(PUBLICKEY);
                handshake.handleHandshake(senderId, receiverId, publicKey);
            } else if (operation.equals(KEY_EXCHANGE)) {
                // Обробка текстового повідомлення
                sendMessage(receiverId, jsonMessage);
            } else if (operation.equals(PING)) {
                // Обробка текстового повідомлення
                sendMessage(userId, "CONNECT_SUCCESSFUL");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Парсер JSON-повідомлень.
     *
     * @param jsonMessage Повідомлення у форматі JSON.
     * @return Мапа з полями повідомлення.
     */
    private Map<String, String> parseMessage(String jsonMessage) {
        Map<String, String> data = new HashMap<>();
        JSONObject jsonObject = new JSONObject(jsonMessage);
        try {
            data.put("senderId", jsonObject.getString("senderId"));
            data.put("receiverId", jsonObject.getString("receiverId"));
            data.put("operation", jsonObject.getString("operation"));
            data.put("message", jsonObject.getString("message"));
            data.put("messageId", jsonObject.getString("messageId"));
            if (jsonMessage.contains("fileUrl")) {

                data.put("fileUrl", jsonObject.getString("fileUrl"));
                data.put("fileHash", jsonObject.getString("fileHash"));
            }
        } catch (JSONException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    /**
     * Надсилає повідомлення користувачу.
     *
     * @param receiverId  Ідентифікатор отримувача.
     * @param jsonMessage Повідомлення у форматі JSON.
     */
    @Override
    public void sendMessage(String receiverId, String jsonMessage) throws IOException {
        if (onlineUsers.containsKey(receiverId)) {
            // Якщо отримувач онлайн
            Socket receiverSocket = onlineUsers.get(receiverId);
            PrintWriter receiverOut = new PrintWriter(receiverSocket.getOutputStream(), true);
            receiverOut.println(jsonMessage);
        } else {
            // Якщо отримувач офлайн
            saveOfflineMessage(receiverId, jsonMessage);
        }
    }
    /**
     * Надсилає статус повідомлення відправнику.
     *
     * @param receiverId Ідентифікатор отримувача.
     */
    private void sendStatus(String sender, String receiverId, String messageId) throws IOException {
        if (onlineUsers.containsKey(receiverId)) {
            // Якщо отримувач онлайн
            Socket receiverSocket = onlineUsers.get(receiverId);
            PrintWriter receiverOut = new PrintWriter(receiverSocket.getOutputStream(), true);
            // міняємо sender та receiverId тим вказуємо що повідомлення відправнику надійшло серверу
            Envelope envelope = new Envelope(sender, receiverId, "messageStatus", "",messageId);
            envelope.setMessageStatus("server");
            receiverOut.println(envelope.toJson().toString());
        } else {
            // Якщо отримувач офлайн
            //saveOfflineMessage(receiverId, jsonMessage);
        }
    }

    @Override
    public synchronized Map<String, Socket> getOnlineUsers() {
        return onlineUsers;
    }

    @Override
    public synchronized Map<String, String> getClientsKey() {
        return clientsKey;
    }

    /**
     * Метод для збереження офлайн-повідомлень.
     *
     * @param receiverId Ідентифікатор отримувача.
     * @param message    Повідомлення для збереження.
     */
    @Override
    public void saveOfflineMessage(String receiverId, String message) {
        offlineMessages.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(message);
        System.out.println("Повідомлення збережено для " + receiverId);
    }

    /**
     * Надсилає збережені офлайн-повідомлення, коли користувач підключається.
     */
    private void sendOfflineMessages() {
        if (offlineMessages.containsKey(userId)) {
            List<String> messages = offlineMessages.get(userId);
            for (String message : messages) {
                out.println(message);
            }
            offlineMessages.remove(userId); // Видалення збережених повідомлень після надсилання
        }
    }

    /**
     * Метод для відключення клієнта.
     */
    private void disconnect() {
        try {
            if (userId != null) {
                onlineUsers.remove(userId);
                System.out.println("Користувач " + userId + " відключився.");
            }
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
