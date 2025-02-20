package com.example.web_socket_service.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final ExecutorService executorService;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Listener listener;
    private ConnectionInfo connectionInfo;

    public WebSocketClient(Listener listener,ConnectionInfo connectionInfo) {
        this.listener = listener;
        this.connectionInfo=connectionInfo;
        this.SERVER_URL = connectionInfo.getServerAddress();
        this.CLIENT_ID = connectionInfo.getRegistration();
        executorService = Executors.newFixedThreadPool(2); // Створюємо пул для асинхронних задач
    }

    // Метод для підключення до сервера
    public void connect() {


        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // Підтримка постійного з'єднання
                .build();

        Request request = new Request.Builder().url(SERVER_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                listener.onNotification("Connected to server...");
                register();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                executorService.submit(() -> { // Виконуємо в окремому потоці
                    if ("REGISTER_OK".equals(text)) {
                        Log.e("WebSocket", "REGISTER_OK:" + CLIENT_ID + " " + text);
                        isRegistered = true;
                        retryCount = 0; // Скидаємо лічильник невдалих спроб
                        listener.onNotification("Connected to server...");

                    } else {
                        if (listener != null) {
                            listener.onListener(text); // Передаємо в сервіс у фоновому потоці
                        } else {
                            Log.e("WebSocket", "Listener is null");
                        }
                    }
                });
            }


            @Override
            public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                listener.onNotification("Connection closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                listener.onNotification("Connection closed : " + reason);
                reconnect();
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                listener.onNotification("Connection failed : " + t.getMessage());
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

    /**
     * Запускає перевірку статусу кожні 3 секунди (тільки один раз!)
     */
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

    /**
     * Зупиняє таймер перед перепідключенням
     */
    private void stopStatusCheck() {
        isCheckingStatus = false; // Скидаємо флаг
        handler.removeCallbacksAndMessages(null); // Видаляємо всі заплановані завдання
    }

    /**
     * Метод для відправки повідомлень
     */
    public void sendMessage(String message) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (webSocket != null) {
                    webSocket.send(message);
                } else {
                    Log.e("WebSocket", "WebSocket is not connected.");
                }
            }
        });
    }

    /**
     * Закриття з'єднання
     */
    public void closeConnection() {
        stopStatusCheck(); // Зупиняємо перевірку перед закриттям
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
        executorService.shutdown(); // Закриваємо пул потоків

    }

    /**
     * Оновлений метод перепідключення
     */
    private void reconnect() {
        stopStatusCheck(); // Зупиняємо старий таймер перед перепідключенням

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            listener.onNotification("Reconnecting...");
            connect(); // Спроба перепідключення
        }, 5000); // Затримка перед перепідключенням
    }

    public boolean isConnected() {
        return webSocket != null;
    }

    public boolean isClosed() {
        return webSocket == null;
    }

    public boolean getStatusRegistration(){
        return  isRegistered;
    }
    /**
     * Інтерфейс для обробки подій та логів.
     */
    public interface Listener {
        void onNotification(String message);

        void onListener(String message);
    }
}
