package com.example.web_socket_service.socket;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.widget.RemoteViews;
import com.example.web_socket_service.R;
import org.json.JSONException;
import org.json.JSONObject;

public class IOService extends Service implements WebSocketClient.Listener {
    private NotificationManager notificationManager;
    private String channelId = "web_socket_channel";
    private String channelName = "WebSocket Service";
    private final int notificationId = 1;
    private WebSocketClient webSocketClient;
    private ServerURL serverURL;
    private BroadcastReceiver receiver;
    private String senderId;
    private String receiverId;
    private String message;
    private String ip;
    private String port;

    @Override
    public void onCreate() {
        super.onCreate();

        // Реєстрація BroadcastReceiver з декількома фільтрами
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "CUBE_ID_SENDER":
                        senderId = intent.getStringExtra("senderId");
                        Log.e("IOService", "CUBE_ID_SENDER " + senderId);
                        serverURL.setSenderId(senderId);
                        break;
                    case "CUBE_ID_RECIVER":
                        receiverId = intent.getStringExtra("receiverId");
                        Log.e("IOService", "CUBE_ID_RECIVER " + receiverId);
                        serverURL.setReciverId(receiverId);
                        break;
                    case "CUBE_SEND_TO_SERVER":
                        message = intent.getStringExtra("message");
                        Log.e("IOService", "CUBE_SEND_TO_SERVER " + message);
                        sendMessage(message);
                        break;
                    case "CUBE_IP_TO_SERVER":
                        ip = intent.getStringExtra("ip");
                        Log.e("IOService", "CUBE_IP_TO_SERVER " + ip);
                        serverURL.setIp(ip);
                        break;
                    case "CUBE_PORT_TO_SERVER":
                        port = intent.getStringExtra("port");
                        Log.e("IOService", "CUBE_PORT_TO_SERVER " + port);
                        serverURL.setPort(port);
                        break;
                }
            }
        };

        // Реєстрація receiver для різних фільтрів
        IntentFilter filter = new IntentFilter();
        filter.addAction("CUBE_ID_SENDER");
        filter.addAction("CUBE_ID_RECIVER");
        filter.addAction("CUBE_SEND_TO_SERVER");
        filter.addAction("CUBE_IP_TO_SERVER");
        filter.addAction("CUBE_PORT_TO_SERVER");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }

        // Запуск сервісу як foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
        }
    }

    private void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isConnected()) {
            Log.e(TAG, "Sending message: " + message);
            webSocketClient.sendMessage(message);
        } else {
            Log.e(TAG, "WebSocket not connected. Cannot send message.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService() {
        // Створення каналу для нотифікацій для Android O та вище

        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
        );
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        // Створюємо кастомний макет для повідомлення
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.notification_title, "CUBE is running");
        notificationLayout.setTextViewText(R.id.notification_text, "Server address " + ip);

        // Побудова самої нотифікації
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setAutoCancel(true)
                .build();

        // Переведення сервісу в foreground
        startForeground(notificationId, notification);
    }

    private void updateNotification(String about, String newMessage) {
        // Створюємо кастомний макет для оновленого повідомлення
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.notification_title, about);
        notificationLayout.setTextViewText(R.id.notification_text, newMessage);

        Notification updatedNotification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setAutoCancel(true)
                .build();

        // Оновлення нотифікації
        notificationManager.notify(notificationId, updatedNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            senderId = intent.getStringExtra("CUBE_ID_SENDER");
            ip = intent.getStringExtra("CUBE_IP_TO_SERVER");
            port = intent.getStringExtra("CUBE_PORT_TO_SERVER");
            serverURL = new ServerURL();
            serverURL.setSenderId(senderId);
            serverURL.setIp(ip);
            serverURL.setPort(port);
            webSocketClient = new WebSocketClient(this);
            webSocketClient.connect(serverURL.getServerAddress(), serverURL.getRegistration());
            updateNotification("CUBE is running","Server address " + ip);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            Log.e(TAG, "BroadcastReceiver unregistered");
        }
        Log.e(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addMessage(String message) {
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("message", message);
        sendBroadcast(intent);  // Надсилання повідомлення Activity 1
    }

    private void saveMessage(String message) {
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("save_message", message);
        sendBroadcast(intent);  // Надсилання повідомлення Activity 1
    }

    @Override
    public void onNotification(String message) {
        Log.e("IOService", message);
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("notification", message);
        sendBroadcast(intent);  // Надсилання повідомлення Activity 1
    }

    @Override
    public void onListener(String message) {
        try {
            if (message != null) {
                JSONObject object = new JSONObject(message);
                Envelope envelope = new Envelope(object);
                if (serverURL.getSenderId().equals(envelope.getSenderId()) && serverURL.getReciverId().equals(envelope.getReceiverId())) {
                    addMessage(message);
                } else if (serverURL.getReciverId() != null && serverURL.getReciverId().equals(envelope.getSenderId())) {
                    addMessage(message);
                } else {
                    saveMessage(message);
                }
            }
        } catch (JSONException e) {
            Log.e("IOService", " Помилка обробки JSON під час отримання повідомлення - " + e.getMessage());
            Log.e("IOService", "Не JSON повідомлення" + message);
        }
    }
}
