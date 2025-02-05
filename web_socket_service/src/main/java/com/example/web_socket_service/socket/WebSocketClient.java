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
    Listener listener;
    public WebSocketClient(Listener listener){
        this.listener=listener;
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
                listener.onNotification("Connected to server...:");
                webSocket.send("REGISTER:" + CLIENT_ID);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {

                listener.onListener(text);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                listener.onNotification("Connection closing: " + reason);
                webSocket.close(1000, null);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                listener.onNotification( "Connection closed: " + reason);
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

    // Метод для відправки повідомлень
    public void sendMessage(String message) {
        /**Проблема тут я ту не отримую данні через деякий час хоча з сервера я отримую повідомлення */
            Log.e("IOService", "Web Socket Message To Service"+message);
            if (webSocket != null) {
                webSocket.send(message);
            } else {
                Log.e("IOService", "WebSocket is not connected.");
            }
    }

    // Закриття з'єднання
    public void closeConnection() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
    }


    // Метод для перепідключення
    private void reconnect() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            listener.onNotification("Reconnecting...");
            connect(SERVER_URL,CLIENT_ID); // Спроба перепідключення
        }, 5000); // Затримка перед перепідключенням
    }

    public boolean isConnected() {
        return webSocket != null;
    }

    /**
     * Інтерфейс для обробки подій та логів.
     */
    public interface Listener {
        void onNotification(String message);
        void onListener(String message);
    }
}
