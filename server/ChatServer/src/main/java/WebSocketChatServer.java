import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketChatServer extends WebSocketServer {

    private static ConcurrentHashMap<String, WebSocket> clients = new ConcurrentHashMap<>();

    public WebSocketChatServer(int port) {
        super(new InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send("Welcome! Please register by sending: REGISTER:<your_client_id>");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
      System.out.println(message);
        if (message.startsWith("REGISTER:")) {
            String clientId = message.substring(9).trim();
            if (clientId.isEmpty() || clients.containsKey(clientId)) {
                conn.send("Registration failed: Invalid or duplicate client ID.");
                return;
            }
            clients.put(clientId, conn);
            conn.send("Registration successful! Your ID: " + clientId);
            broadcast("User " + clientId + " joined the chat.");
        } else if (message.contains(":")) {
            String[] parts = message.split(":", 2);
            String recipientId = parts[0].trim();
            String msgContent = parts[1].trim();

            if (!clients.containsKey(recipientId)) {
                conn.send("Error: User " + recipientId + " not found.");
                return;
            }

            WebSocket recipient = clients.get(recipientId);
            recipient.send("Message from " + getClientId(conn) + ": " + msgContent);
            conn.send("Message delivered to " + recipientId + ".");
        } else {
            conn.send("Invalid message format. Use REGISTER:<id> or <recipient>:<message>");
        }
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
        return clients.entrySet().stream()
                .filter(entry -> entry.getValue().equals(conn))
                .map(entry -> entry.getKey())
                .findFirst().orElse(null);
    }

    public static void main(String[] args) {
        WebSocketChatServer server = new WebSocketChatServer(8080);
        server.start();
        System.out.println("Chat server started on port 8080...");
    }
}
