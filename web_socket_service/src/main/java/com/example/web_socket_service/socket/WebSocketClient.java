package com.example.web_socket_service.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {
    private WebSocket webSocket;
    private String SERVER_URL;
    private String CLIENT_ID;
    private int retryCount = 0;
    private final int MAX_RETRY = 3;
    private boolean isRegistered = false; // Чи отримали відповідь від сервера
    private boolean isCheckingStatus = false; // Запобігає множинним перевіркам

    private final Handler handler = new Handler(Looper.getMainLooper());

    Listener listener;

    public WebSocketClient(Listener listener) {
        this.listener = listener;
    }

    // Метод для підключення до сервера
    public void connect(String SERVER_URL, String CLIENT_ID) {
        this.SERVER_URL = SERVER_URL;
        this.CLIENT_ID = CLIENT_ID;

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // Підтримка постійного з'єднання
                .build();

        Request request = new Request.Builder().url(SERVER_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                listener.onNotification("Connected to server...");
                register();
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (text.equals("REGISTER_OK")) {
                    Log.e("IOService", "REGISTER_OK:" + CLIENT_ID);
                    isRegistered = true;
                    retryCount = 0; // Скидаємо лічильник невдалих спроб
                } else {
                    listener.onListener(text);
                }
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                listener.onNotification("Connection closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                listener.onNotification("Connection closed: " + reason);
                reconnect();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                t.printStackTrace();
                listener.onNotification("Connection failed: " + t.getMessage());
                reconnect();
            }
        });

        client.dispatcher().executorService().shutdown();
    }

    private void register() {
        isRegistered = false;
        retryCount = 0;

        if (isConnected()) {
            startStatusCheck();
        } else {
            Log.e("IOService", "WebSocket is closed");
        }
    }

    // ✅ Запускає перевірку статусу кожні 3 секунди (тільки один раз!)
    private void startStatusCheck() {
        if (isCheckingStatus) return; // Запобігає множинному виклику
        isCheckingStatus = true;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (webSocket != null) {
                    if (isConnected()) {
                        Log.d("WebSocket", "Checking registration status... Attempt: " + retryCount);
                        webSocket.send("CHECK_STATUS:" + CLIENT_ID);
                    }
                    if (!isRegistered) {
                        retryCount++;
                        if (retryCount >= MAX_RETRY) {
                            if (isConnected()) {
                                Log.d("WebSocket", "Re-registering...");
                                webSocket.send("REGISTER:" + CLIENT_ID);
                                retryCount = 0;
                            }
                        }
                    }
                }
                handler.postDelayed(this, 3000); // Повторюємо через 3 секунди
            }
        }, 3000);
    }

    // ✅ Зупиняє таймер перед перепідключенням
    private void stopStatusCheck() {
        isCheckingStatus = false; // Скидаємо флаг
        handler.removeCallbacksAndMessages(null); // Видаляємо всі заплановані завдання
    }

    // Метод для відправки повідомлень
    public void sendMessage(String message) {
        Log.e("IOService", "Web Socket Message To Service: " + message);
        if (webSocket != null) {
            webSocket.send(message);
        } else {
            Log.e("IOService", "WebSocket is not connected.");
        }
    }

    // Закриття з'єднання
    public void closeConnection() {
        stopStatusCheck(); // Зупиняємо перевірку перед закриттям
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
    }

    // ✅ Оновлений метод перепідключення
    private void reconnect() {
        stopStatusCheck(); // Зупиняємо старий таймер перед перепідключенням

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            listener.onNotification("Reconnecting...");
            connect(SERVER_URL, CLIENT_ID); // Спроба перепідключення
        }, 5000); // Затримка перед перепідключенням
    }

    public boolean isConnected() {
        return webSocket != null;
    }

    public boolean isClosed() {
        return webSocket == null;
    }

    /**
     * Інтерфейс для обробки подій та логів.
     */
    public interface Listener {
        void onNotification(String message);

        void onListener(String message);
    }
}
