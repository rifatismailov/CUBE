package http;

import org.example.Envelope;
import org.example.server.HandshakeMap;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer implements Handshake.HandshakeListener {
    final String MESSAGE = "message";
    final String HANDSHAKE = "handshake";
    final String PUBLICKEY = "publicKey";
    final String KEY_EXCHANGE = "keyExchange";
    final String PING = "ping";
    final String IMAGE = "image";
    final String FILE = "file";
    private WebSocket recipient;
    String jsonPattern = "\\{\\s*(\"[^\"]+\"\\s*:\\s*(\"[^\"]*\"|\\{.*?\\}|\\[.*?\\]))(\\s*,\\s*\"[^\"]+\"\\s*:\\s*(\"[^\"]*\"|\\{.*?\\}|\\[.*?\\]))*\\s*\\}";

    private final ConcurrentHashMap<String, WebSocket> clients;
    private final ConcurrentHashMap<String, List<String>> saveMessages;
    private final ConcurrentHashMap<String, String> clientsKey;

    public WebSocketServer(ConcurrentHashMap<String, WebSocket> clients, ConcurrentHashMap<String, List<String>> saveMessages, ConcurrentHashMap<String, String> clientsKey, int port) {
        super(new InetSocketAddress(port));
        this.clients = clients;
        this.saveMessages = saveMessages;
        this.clientsKey = clientsKey;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome! Please register by sending: REGISTER:<your_client_id>");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message.startsWith("REGISTER:")) {
            String userId = new JsonGetter().getUserID(message);
            if (userId == null || clients.containsKey(userId)) {
                conn.send("Registration failed: Invalid or duplicate client ID.");
                return;
            }
            clients.put(userId, conn);
            conn.send("Registration successful! Your ID: " + userId);
            System.out.println("Registration successful! Your ID: " + userId);

            sendSaveMessages(conn,userId);
            broadcast("User " + userId + " joined the chat.");
        }
        else {
            System.out.println(message);
            processMessage(conn, message);
        }
//        else if(Pattern.matches(jsonPattern, message)) {
//            processMessage(conn, message);
//        }else  {
//            System.out.println("Not Json "+message);
//        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        String clientId = getClientId(conn);
        if (clientId != null) {
            clients.remove(clientId);
            broadcast("User " + clientId + " left the chat.");
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {

    }

    private String getClientId(WebSocket conn) {
        return clients.entrySet().stream().filter(entry -> entry.getValue().equals(conn)).map(entry -> entry.getKey()).findFirst().orElse(null);
    }

    /**
     * Метод для обробки вхідних повідомлень.
     *
     * @param conn
     * @param jsonMessage Повідомлення у форматі JSON.
     */
    private void processMessage(WebSocket conn, String jsonMessage) {
        try {
            // Парсимо JSON-повідомлення
            Map<String, String> messageData = new JsonGetter().parseMessage(jsonMessage);
            String senderId = messageData.get("senderId");
            String receiverId = messageData.get("receiverId");
            String operation = messageData.get("operation");
            String messageId = messageData.get("messageId");

            if (operation.equals(MESSAGE)) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
                //conn.send("Message delivered to " + receiverId + ".");
                conn.send(messageStatus(senderId, receiverId, messageId, "server"));
            } else if (operation.equals(IMAGE)) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
                conn.send(messageStatus(senderId, receiverId, messageId, "server"));
            } else if (operation.equals(FILE)) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
                conn.send(messageStatus(senderId, receiverId, messageId, "server"));
            } else if (operation.equals(HANDSHAKE)) {
                // Обробка обміну ключами

                String message = messageData.get(MESSAGE);

                JSONObject jsonObject = new JSONObject(message);
                String publicKey = jsonObject.getString(PUBLICKEY);
                 new Handshake(this).handleHandshake(senderId, receiverId, publicKey);
            } else if (operation.equals(KEY_EXCHANGE)) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
            } else if (operation.equals(PING)) {
                // Обробка текстового повідомлення
                // sendMessage(conn, userId, "CONNECT_SUCCESSFUL");
            }
            else if (operation.equals("messageStatus")) {
                sendMessage(conn, receiverId, jsonMessage);
            }else  if (operation.equals("GET_AVATAR")) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
            }else  if (operation.equals("AVATAR")) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
            }else  if (operation.equals("AVATAR_ORG")) {
                // Обробка текстового повідомлення
                sendMessage(conn, receiverId, jsonMessage);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * Надсилає повідомлення користувачу.
     *
     * @param conn
     * @param receiverId  Ідентифікатор отримувача.
     * @param jsonMessage Повідомлення у форматі JSON.
     */
    public void sendMessage(WebSocket conn, String receiverId, String jsonMessage) throws IOException {
        if (!clients.containsKey(receiverId)) {
            conn.send("Error: User " + receiverId + " not found.");
            saveMessage(receiverId, jsonMessage);

            return;
        }
        recipient = clients.get(receiverId);
        recipient.send(jsonMessage);
    }

    /**
     * Надсилає повідомлення користувачу.
     *
     * @param receiverId  Ідентифікатор отримувача.
     * @param jsonMessage Повідомлення у форматі JSON.
     */

    @Override
    public void sendMessage(String receiverId, String jsonMessage) throws IOException {
        if (!clients.containsKey(receiverId)) {
            // conn.send("Error: User " + receiverId + " not found.");
            saveMessage(receiverId, jsonMessage);
            return;
        }
        recipient = clients.get(receiverId);
        recipient.send(jsonMessage);
    }

    @Override
    public ConcurrentHashMap<String, WebSocket> getOnlineUsers() {
        return clients;
    }

    @Override
    public ConcurrentHashMap<String, String> getClientsKey() {
        return clientsKey;
    }

    /**
     * Метод для збереження офлайн-повідомлень.
     *
     * @param receiverId Ідентифікатор отримувача.
     * @param message    Повідомлення для збереження.
     */
    @Override
    public void saveMessage(String receiverId, String message) {
        saveMessages.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(message);
    }

    private String messageStatus(String senderId, String receiverId, String messageId, String status) {
        Envelope envelope = new Envelope(senderId, receiverId, "messageStatus", "", messageId);
        envelope.setMessageStatus(status);
        return envelope.toJson().toString();
    }
    /**
     * Надсилає збережені офлайн-повідомлення, коли користувач підключається.
     */
    private void sendSaveMessages(WebSocket conn,String userId) {
        if (saveMessages.containsKey(userId)) {
            List<String> messages = saveMessages.get(userId);
            for (String message : messages) {
                conn.send(message);
            }
            saveMessages.remove(userId); // Видалення збережених повідомлень після надсилання
        }
    }

}
