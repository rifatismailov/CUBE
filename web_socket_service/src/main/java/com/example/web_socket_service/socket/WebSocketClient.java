package com.example.web_socket_service.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
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
    private ExecutorService executorService;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Listener listener;
    private ConnectionInfo connectionInfo;
    private MessageServiceManager messageManager;

    public WebSocketClient(Listener listener, ConnectionInfo connectionInfo, MessageServiceManager messageManager) {
        this.listener = listener;
        this.connectionInfo = connectionInfo;
        this.messageManager = messageManager;
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
//        OkHttpClient client = new OkHttpClient.Builder()
//                .retryOnConnectionFailure(true)
//                .connectTimeout(10, TimeUnit.SECONDS) // Без таймауту для WebSocket
//                .build();
        Request request = new Request.Builder().url(SERVER_URL).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                listener.onNotification("connected");
                register();
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                executorService.submit(() -> { // Виконуємо в окремому потоці
                    if (text.startsWith("REGISTER_OK")) {
                        String[] textArray = text.split(":");
                        if ("REGISTER_OK".equals(textArray[0])) {
                            isRegistered = true;
                            retryCount = 0; // Скидаємо лічильник невдалих спроб
                            listener.onNotification("connected");
                            listener.sendStatus(textArray[1]);
                            Log.e("WebSocket", "WebSocket Status "+textArray[1]);

                        }
                        /*перевіряємо кількість повідомлень для відправки*/
                        HashMap<String, Envelope> messages = messageManager.getMessagesByOperation("send");

                        for (Map.Entry<String, Envelope> entry : messages.entrySet()) {
                            String messageId = entry.getKey();
                            Envelope envelope = entry.getValue();
                            sendMessage(envelope.toJson().toString());
                        }
                        Log.e("WebSocket", "WebSocket Count "+messageManager.getMessageCountByOperation("send"));

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
                listener.onNotification("closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                listener.onNotification("closed: " + reason);
                reconnect();
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                listener.onNotification("failed: " + t.getMessage());
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
                try {
                    JSONObject jsonObject = new JSONObject(connectionInfo.getRegistration());

                    if (webSocket != null) {
                        if (isConnected()) {
                            Log.d("WebSocket", "Checking registration status... Attempt: " + retryCount);
                            jsonObject.put("life", connectionInfo.getLife());
                            Log.e("WebSocket", "connectionInfo..." + jsonObject);
                            webSocket.send("CHECK_STATUS:" + jsonObject);
                        }
                        if (!isRegistered) {
                            retryCount++;
                            if (retryCount >= MAX_RETRY) {
                                if (isConnected()) {
                                    Log.d("WebSocket", "Re-registering...");
                                    jsonObject.put("life", connectionInfo.getLife());
                                    Log.e("WebSocket", "connectionInfo..." + jsonObject);

                                    webSocket.send("REGISTER:" + jsonObject);
                                    retryCount = 0;
                                }
                            }
                        }
                    }
                } catch (Exception e) {

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
            listener.onNotification("reconnecting");
            connect(); // Спроба перепідключення
        }, 5000); // Затримка перед перепідключенням
    }

    /**
     * Метод для перезапуску WebSocket-з'єднання
     */
    public void restartConnection(ConnectionInfo newConnectionInfo) {
        closeConnection(); // Закриваємо поточне з'єднання

        // Оновлюємо connectionInfo
        this.connectionInfo = newConnectionInfo;
        this.SERVER_URL = newConnectionInfo.getServerAddress();
        this.CLIENT_ID = newConnectionInfo.getRegistration();

        // Створюємо новий ExecutorService після закриття старого
        executorService = Executors.newFixedThreadPool(2);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            listener.onNotification("restarting");
            connect(); // Перезапускаємо підключення з новими параметрами
        }, 2000);
    }


    public boolean isConnected() {
        return webSocket != null;
    }

    public boolean isClosed() {
        return webSocket == null;
    }

    public boolean getStatusRegistration() {
        return isRegistered;
    }

    /**
     * Інтерфейс для обробки подій та логів.
     */
    public interface Listener {
        void sendStatus(String status);

        void onNotification(String message);

        void onListener(String message);
    }
}
