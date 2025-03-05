

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
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.widget.RemoteViews;

import com.example.database_cube.DatabaseHelper;
import com.example.web_socket_service.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IOService — Android service that manages communication with a WebSocket server.
 * It handles sending and receiving messages, maintaining notification updates, and managing service lifecycle.
 */
public class IOService extends Service implements WebSocketClient.Listener {

    private NotificationManager notificationManager;
    private final String channelId = "cube_web_socket_channel";
    private final int notificationId = 1;
    private WebSocketClient webSocketClient;
    private final ConnectionInfo connectionInfo = new ConnectionInfo();
    private BroadcastReceiver receiver;
    private String senderId;
    private String receiverId;
    private String ip;
    private String port;
    private MessageServiceManager messageManager;
    private ExecutorService executorService;
    private final Queue<Envelope> messageQueue = new ConcurrentLinkedQueue<>();

    /**
     * Called when the service is first created. Initializes BroadcastReceiver and notification manager.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        messageManager = new MessageServiceManager(db);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    switch (Objects.requireNonNull(intent.getAction())) {
                        case "CUBE_ID_SENDER":
                            senderId = intent.getStringExtra("senderId");
                            synchronized (connectionInfo) {
                                connectionInfo.setSenderId(senderId);// Оновлюємо лише значення
                            }
                            break;
                        case "CUBE_SEND_TO_SERVER":
                            setMessage(intent.getStringExtra("message"));
                            break;
                        case "CUBE_SEND_TO_SETTING":
                            String setting = intent.getStringExtra("setting");
                            Log.e("IOService", "CUBE_SEND_TO_SETTING " + setting);
                            // Створення JSONObject з рядка
                            JSONObject jsonObject = new JSONObject(setting);

                            // Отримання значень
                            String userId = jsonObject.getString("userId");
                            ip = jsonObject.getString("serverIp");
                            port = jsonObject.getString("serverPort");
                            synchronized (connectionInfo) {
                                connectionInfo.setSenderId(userId);
                                connectionInfo.setIp(ip);
                                connectionInfo.setPort(port);
                                startWebSocket();
                            }
                            break;

                        case "MAIN_ACTIVITY_REGISTRATION":
                            String request = intent.getStringExtra("request");
                            Log.e("IOService", "request " + request);
                            synchronized (connectionInfo) {
                                connectionInfo.setRegistration(request);
                            }
                            startWebSocket();
                            break;
                        case "MAIN_ACTIVITY_COMMAND":
                            handleActivityCommand(intent.getStringExtra("COMMAND"));
                            break;
                    }
                } catch (Exception e) {
                    Log.e("IOService", "Під час отримання даних з активності було отримано null:" + e);
                }
            }
        };

        // Registering receiver for multiple intent filters
        IntentFilter filter = new IntentFilter();
        filter.addAction("CUBE_ID_SENDER");
        filter.addAction("CUBE_ID_RECIVER");
        filter.addAction("CUBE_SEND_TO_SERVER");
        filter.addAction("CUBE_IP_TO_SERVER");
        filter.addAction("CUBE_PORT_TO_SERVER");
        filter.addAction("MAIN_ACTIVITY_COMMAND");
        filter.addAction("MAIN_ACTIVITY_REGISTRATION");
        filter.addAction("CUBE_SEND_TO_SETTING");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }

        // Start foreground service for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService();
        }
    }

    /**
     * Метод який обробляє отримані команди з основної активності
     */
    private void handleActivityCommand(String command) {
        try {
            JSONObject jsonObject = new JSONObject(connectionInfo.getRegistration());

            switch (command) {
                case "reborn":
                    Log.e("IOService", "Main Activity reborn");
                    getOfflineMessage();
                    connectionInfo.setLife(command);
                    break;
                case "died":
                    connectionInfo.setLife(command);
                    break;
                case "reconnect":

                    break;
            }
        } catch (Exception e) {
            Log.e("IOService", "Під час отримання житті-діяльності активності було отримано null:" + e);
        }
    }

    private void setMessage(String message) {
        try {
            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            // Отримуємо значення messageStatus, перевіряємо на null і обрізаємо пробіли
            String status = envelope.getMessageStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "unknown";  // Встановлюємо значення за замовчуванням
            } else {
                status = status.trim();
            }

            switch (status) {
                case "delivered_to_user":
                    Log.e("IOService", "Delete message " + status + " ID message " + envelope.getMessageId());
                    messageManager.deleteMessageById(envelope.getMessageId());
                    break;
                case "update_to_user":
                    Log.e("IOService", "Update message " + status + " ID message " + envelope.getMessageId());
                    break;
                default:
                    Log.e("WebSocket", "Default case triggered with status: " + status+" "+envelope.toJson());
                    messageManager.setMessage(envelope, "send");
                   // messageManager.deleteAllMessages();
                    sendMessage(message);
                    break;
            }
        } catch (Exception e) {
            Log.e("IOService", "Error parsing message", e);
        }
    }

    /**
     * Sends a message via WebSocket if the connection is active.
     *
     * @param message Message to send.
     */
    private void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isConnected()) {
            webSocketClient.sendMessage(message);
        } else {
            Log.e(TAG, "WebSocket not connected. Cannot send message.");
        }
    }

    /**
     * Starts the service as a foreground service and displays a notification.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService() {
        // Create notification channel
        String channelName = "Cube_WebSocket Service";
        NotificationChannel channel = new NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
        );
        notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }

        // Create custom notification layout
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.notification_title, "CUBE is running");
        notificationLayout.setTextViewText(R.id.notification_text, "Server address " + ip);

        // Build the notification
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setAutoCancel(true)
                .build();

        // Move the service to foreground
        startForeground(notificationId, notification);
    }

    /**
     * Updates the foreground service notification.
     *
     * @param about      Notification title
     * @param newMessage Notification message
     */
    private void updateNotification(String about, String newMessage) {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.notification_title, about);
        notificationLayout.setTextViewText(R.id.notification_text, newMessage);

        Notification updatedNotification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(notificationId, updatedNotification);
    }

    /**
     * Handles the service's start command and initializes WebSocket connection.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            try {
                String setting = intent.getStringExtra("CUBE_SEND_TO_SETTING");
                String command = intent.getStringExtra("MAIN_ACTIVITY_COMMAND");
                String registration = intent.getStringExtra("MAIN_ACTIVITY_REGISTRATION");
                Log.e("IOService", "CUBE_SEND_TO_SETTING " + setting);
                // Створення JSONObject з рядка
                JSONObject jsonObject = new JSONObject(setting);

                // Отримання значень
                senderId = jsonObject.getString("userId");
                ip = jsonObject.getString("serverIp");
                port = jsonObject.getString("serverPort");

                connectionInfo.setSenderId(senderId);
                connectionInfo.setIp(ip);
                connectionInfo.setPort(port);
                connectionInfo.setRegistration(registration);
                if (command != null) {
                    switch (command) {
                        case "reborn":
                            Log.e("IOService", "Main Activity reborn :" + command);
                            connectionInfo.setLife(command);
                            break;
                        case "died":
                            Log.e("IOService", "Main Activity died");
                            connectionInfo.setLife(command);
                            break;
                    }
                }
                startWebSocket();

            } catch (Exception e) {
                Log.e("IOService", "Error on Start Command: " + e);
            }
            updateNotification("CUBE is running", "Server address " + ip);
            getOfflineMessage();
        }
        return START_STICKY;
    }

    private void startWebSocket() {
        if (webSocketClient != null) {
            webSocketClient.restartConnection(connectionInfo); // Перезапуск без дублювання
        } else {
            webSocketClient = new WebSocketClient(this, connectionInfo, messageManager);
            webSocketClient.connect();
        }
    }

    /**
     * Cleans up resources when the service is destroyed.
     */
    @Override
    public void onDestroy() {
        executorService.shutdown();
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

    /**
     * Sends a broadcast message.
     *
     * @param message Message to broadcast.
     */
    private void addMessage(String message) {
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    /**
     * Saves a message by broadcasting it.
     *
     * @param message Message to save.
     */
    private void saveMessage(String message) {
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("save_message", message);
        sendBroadcast(intent);
    }

    @Override
    public void sendStatus(String status) {
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("contact_status", status);
        sendBroadcast(intent);
    }

    /**
     * Handles WebSocket notifications.
     */
    @Override
    public void onNotification(String message) {
        String[] info = message.split(":");
        updateNotification("CUBE is running", info[0]);
        Log.e("IOService", message);
        Intent intent = new Intent("CUBE_RECEIVED_MESSAGE");
        intent.putExtra("notification", message);
        sendBroadcast(intent);
    }

    /**
     * Listens for incoming WebSocket messages and processes them.
     *
     * @param message Incoming message from WebSocket.
     */
    @Override
    public void onListener(String message) {
        if (message != null) {
            try {
                JSONObject object = new JSONObject(message);
                Envelope envelope = new Envelope(object);
                addMessage(message);
                returnMessageDeliver(envelope);
                if (envelope.getMessageStatus().equals("server")) {
                    Log.e("WebSocket", "messages delete "+envelope.getMessageId());
                    messageManager.deleteMessageById(envelope.getMessageId());
                } else {
                    messageManager.setMessage(envelope);
                }
            } catch (JSONException e) {
                Log.e("IOService", " JSON processing error while receiving message - " + e.getMessage());
                Log.e("IOService", "Not a JSON message: " + message);
            }
        }
    }

    private void saveMessagesToDatabase() {
        Envelope message;
        while ((message = messageQueue.poll()) != null) { // Витягуємо всі повідомлення з черги
            messageManager.setMessage(message);
        }
    }

    /**
     * Метод виводи оффлайн повідомлень якщо в они є
     * Витягуємо повідомлення які не відноситься до відправляємих якщо такі є
     * потім отримуємо повідомлення які відносяться до повідомлення або файлу
     * якщо є щось інше то ми їх видаляємо щоб не було накопичення.
     */
    private void getOfflineMessage() {
        HashMap<String, Envelope> messages = messageManager.getMessagesExceptOperation("send");

        for (Map.Entry<String, Envelope> entry : messages.entrySet()) {
            String messageId = entry.getKey();
            Envelope envelope = entry.getValue();
            if (envelope.getOperation().equals("message") || envelope.getOperation().equals("file")) {
                saveMessage(envelope.toJson().toString());
            } else {
                messageManager.deleteMessageById(messageId);
            }
        }

    }

    /**
     * Метод сповіщення сервер о отриманні повідомлення
     *
     * @param envelope повідомлення яке прийшло
     *                 отримуємо такі данні для відправки сповіщення:
     *                 >    @envelope.getSenderId() Id відправника
     *                 >    @envelope.getReceiverId() Id отримувача тоб то нащ
     *                 >    @envelope.getMessageId() Id повідомлення з яким воно прийшло
     */
    private void returnMessageDeliver(Envelope envelope) {
        Log.e("IOService", "Message : " + envelope.toJson());
        if (!envelope.getOperation().equals("messageStatus")) {

            String message = new Envelope.Builder().
                    setSenderId(envelope.getReceiverId()).
                    setReceiverId(envelope.getSenderId()).
                    setOperation("messageStatus").
                    setMessageStatus("delivered").
                    setMessageId(envelope.getMessageId()).
                    build().
                    toJson("senderId", "receiverId", "operation", "messageStatus", "messageId").
                    toString();
            sendMessage(message);
        }
    }
}

