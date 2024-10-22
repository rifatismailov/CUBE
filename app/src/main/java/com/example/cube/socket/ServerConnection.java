package com.example.cube.socket;

import android.util.Log;

import com.example.cube.Envelope;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerConnection {
    private static final String SERVER_IP = "192.168.31.172";  // IP сервера192.168.193.183
    private static final int SERVER_PORT = 8080;  // Порт сервера

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private ScheduledExecutorService scheduler;
    private ConnectionListener listener;
    private String userId;
    private String receiverId;
    private boolean connectUser = false;

    public ServerConnection(ConnectionListener listener) {
        this.listener = listener;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // Метод підключення до сервера
    public void connectToServer() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                Log.d("MainActivity", "Підключення до сервера");
                registerUser();
                startConnectionChecker();
                listener.onConnected();
            } catch (IOException e) {
                Log.e("ServerConnection", "Помилка підключення до сервера", e);
            }
        });

    }

    // Метод для перевірки підключення
    private void startConnectionChecker() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            Log.d("MainActivity", "перевірка");

            if (socket == null || socket.isClosed()) {
                Log.d("MainActivity", "підключення розірвано");
                return;
            }
            if (socket.isConnected()) {
                Log.d("MainActivity", "підключення встановлнео");
            }
            try {

                // Перевіряємо, чи сервер доступний
                boolean reachable = socket.getInetAddress().isReachable(1000); // Таймаут 1 секунда

                if (!reachable) {
                    Log.d("MainActivity", "сервер недосяжний, підключення розірвано");
                    connectUser = false;

                    // reconnectToServer(); // Перепідключення до сервера
                } else {
                    Log.d("MainActivity", "сервер доступний, підключення активне");
                    if (!connectUser) {
                        Log.d("MainActivity", "Регістрація");
                        connectToServer();
                    }
                }
            } catch (IOException e) {
                Log.d("MainActivity", "помилка перевірки доступності сервера");
            }

        }, 0, 5, TimeUnit.SECONDS); // Перевірка кожні 5 секунд
    }

    // Надсилання даних на сервер
    public void sendData(String data) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (output != null) {
                output.println(data);
            }
        });
    }

    // Читання даних з сервера
    public void listenForMessages() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    if (message != null) {
                        JSONObject object = new JSONObject(message);
                        Envelope envelope = new Envelope(object);

                        if (userId.equals(envelope.getSenderId()) && receiverId.equals(envelope.getReceiverId())) {
                            listener.onMessageReceived(message);
                            Thread.sleep(100); // 100 мс
                        } else if (receiverId != null && receiverId.equals(envelope.getSenderId())) {
                            if (isJson(envelope.getMessage()))
                                listener.onMessageReceived(message);
                            else handshake(envelope.getSenderId(), envelope.getMessage());
                            Thread.sleep(100); // 100 мс
                        } else {
                            if (isJson(envelope.getMessage()))
                                listener.saveMessage(envelope);
                            else handshake(envelope.getSenderId(), envelope.getMessage());
                        }
                    } else {
                        Log.e("MainActivity", "Отримано null повідомлення");
                    }
                    //listener.onMessageReceived(message);
                }
            } catch (IOException e) {
                Log.e("ServerConnection", "Помилка читання повідомлень", e);
            } catch (JSONException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private final Pattern JSON_PATTERN = Pattern.compile(
            "\\s*\\{[^{}]*\\}\\s*|\\s*\\[[^\\[\\]]*\\]\\s*"
    );

    public boolean isJson(String jsonString) {
        Matcher matcher = JSON_PATTERN.matcher(jsonString.trim());
        return !matcher.matches();
    }

    public void handshake(String senderId, String jsonString) {

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            // Отримайте значення userId
            String publicKey = jsonObject.getString("publicKey");
            listener.clientHandshake(senderId, publicKey);

        } catch (Exception e) {
        }
    }


    // Зупинка перевірки підключення
    public void stopConnectionChecker() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        try {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e("ServerConnection", "Помилка закриття сокета", e);
        }
    }

    // Реєстрація користувача
    private void registerUser() {
        String registerMessage = "{\"userId\":\"" + this.userId + "\"}";
        sendData(registerMessage);
        connectUser = true;
    }

    // Інтерфейс для сповіщення про стан підключення
    public interface ConnectionListener {
        void onConnected();

        void onMessageReceived(String message);

        void saveMessage(Envelope envelope);

        void clientHandshake(String senderId, String publicKey);
    }
}
