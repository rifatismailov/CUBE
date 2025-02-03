package http;

import org.java_websocket.WebSocket;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class RunWeb {

    private static ConcurrentHashMap<String, WebSocket> clients = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, List<String>> saveMessages = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, String> clientsKey = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServer(clients, saveMessages, clientsKey, 8080);
        server.start();
        System.out.println("Chat server started on port 8080...");

        // Додай shutdown hook для автоматичного виклику server.stop()
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                System.out.println("Server stopped gracefully.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}
