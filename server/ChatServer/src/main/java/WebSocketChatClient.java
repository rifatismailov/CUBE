import org.example.Envelope;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.UUID;

public class WebSocketChatClient extends WebSocketClient {
    private static final String CLIENT_ID = String.format("REGISTER:{\"userId\":\"%s\"}", "client_android_BBB");

    static WebSocketChatClient client;

    static {
        try {
            client = new WebSocketChatClient(new URI("ws://localhost:8080"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public WebSocketChatClient(URI serverUri) throws URISyntaxException {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to server.");
        client.send("REGISTER:" + CLIENT_ID);
    }

    @Override
    public void onMessage(String message) {
        try {
            System.out.println("Server: " + message);
            JSONObject jsonObject = new JSONObject(message);

            client.send(messageStatus("client_android_BBA", "client_android_AAA", jsonObject.getString("messageId"), "delivery"));

        }catch (Exception e){
            System.out.println("Other Message: " + message);

        }
          }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server. Reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        client.connect();

        while (!client.isOpen()) {
            Thread.sleep(100);
        }

        System.out.println("To register, use: REGISTER:<your_client_id>");
        System.out.println("To send a message: <recipient>:<message>");
        System.out.println("Type 'exit' to close the connection.");

        while (true) {
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                client.close();
                break;
            }
            client.send(new Envelope("client_android_BBB","client_android_AAA","message",input, UUID.randomUUID().toString()).toJson().toString());
        }
    }

    private String messageStatus(String senderId, String receiverId, String messageId, String status) {
        Envelope envelope = new Envelope(senderId, receiverId, "messageStatus", "", messageId);
        envelope.setMessageStatus(status);
        return envelope.toJson().toString();
    }
}

