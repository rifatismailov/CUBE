

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
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.widget.RemoteViews;

import com.example.database_cube.DatabaseHelper;
import com.example.web_socket_service.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * IOService — сервіс Android, який керує зв'язком із сервером WebSocket.
 * Він обробляє надсилання й отримання повідомлень, підтримку оновлень сповіщень і керування життєвим циклом служби.
 */
public class IOService extends Service implements WebSocketClient.Listener {

    private NotificationManager notificationManager;
    private final String channelId = "cube_web_socket_channel";
    private final int notificationId = 1;
    private WebSocketClient webSocketClient;
    private final ConnectionInfo connectionInfo = new ConnectionInfo();
    private BroadcastReceiver receiver;
    private String senderId;
    private String ip;
    private String port;
    private MessageServiceManager messageManager;
    private ExecutorService executorService;
    private final Queue<Envelope> messageQueue = new ConcurrentLinkedQueue<>();
    private SoundPlayer soundPlayer;

    /**
     * Called when the service is first created. Initializes BroadcastReceiver and notification manager.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
        soundPlayer = new SoundPlayer();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        messageManager = new MessageServiceManager(db);
        //messageManager.deleteAllMessages();
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
                            JSONObject jsonObject = new JSONObject(setting);
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
                            synchronized (connectionInfo) {
                                connectionInfo.setRegistration(request);
                            }
                            startWebSocket();
                            break;
                        case "MAIN_ACTIVITY_COMMAND":
                            handleActivityCommand(intent.getStringExtra("command"));
                            break;
                    }
                } catch (Exception e) {
                    Log.e("IOService", "When retrieving data from activity, null was received:" + e);
                }
            }
        };

        // Registering receiver for multiple intent filters
        IntentFilter filter = new IntentFilter();
        filter.addAction("CUBE_ID_SENDER");
        filter.addAction("CUBE_SEND_TO_SERVER");
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
     * Handles commands received from the main activity.
     *
     * @param command The command string received.
     */
    private void handleActivityCommand(String command) {
        try {

            switch (command) {
                case "reborn":
                    getOfflineMessage();
                    connectionInfo.setLife(command);
                    break;
                case "died":
                    connectionInfo.setLife(command);
                    break;
            }
        } catch (Exception e) {
            Log.e("IOService", "Під час отримання житті-діяльності активності було отримано null:" + e);
        }
    }

    /**
     * Processes and handles the incoming message.
     *
     * @param message The JSON message received.
     */
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
                case "update_to_message":
                    messageManager.deleteMessageById(envelope.getMessageId());//видаляємо збережене повідомлення за ID яке прийшло
                    Log.e("IOService", "delete message id: " + envelope.getMessageId());

                case "delivered_to_user":
                    sendMessage(envelope.toJson().toString());//не зберігаємо у базу даних
                    messageManager.deleteMessageById(envelope.getMessageId());//видаляємо збережене повідомлення за ID яке прийшло
                    Log.e("IOService", "delete message id: " + envelope.getMessageId());

                    break;
                default:
                    processEnvelope(envelope);
                    Log.e("IOService", "send message id: " + envelope.getMessageId());


                    break;
            }
        } catch (Exception e) {
            Log.e("IOService", "Error parsing message", e);
        }
    }


    /**
     * Processes and stores messages unless they are avatars.
     * Some messages are not saved because they can be requested again
     * for example, avatar sharing and key sharing, which can be requested again
     *
     * @param envelope The message envelope to process.
     */

    private void processEnvelope(Envelope envelope) {
        if (IGNORED_OPERATIONS.contains(envelope.getOperation())) {
            sendMessage(envelope.toJson().toString());
        } else {
            messageManager.setMessage(envelope, "send");
            sendMessage(envelope.toJson().toString());
        }
    }

    private static final Set<String> IGNORED_OPERATIONS = new HashSet<>(Arrays.asList(
            "AVATAR_ORG", "AVATAR", "GET_AVATAR", "keyExchange", "handshake"
    ));

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

    @NonNull
    private Notification getNotification(String about, String newMessage) {
        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notificationLayout.setTextViewText(R.id.notification_title, about);
        notificationLayout.setTextViewText(R.id.notification_text, newMessage);
        // Build the notification
        return new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.chat_message_icon)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setColor(Color.BLACK) // Задає чорний колір фону іконки
                .setAutoCancel(true)
                .build();
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

        Notification notification = getNotification("CUBE is running", "Server address " + ip);
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
        Notification notification = getNotification(about, newMessage);
        notificationManager.notify(notificationId, notification);
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
                if (setting != null) {
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
                                getOfflineMessage();
                                connectionInfo.setLife(command);
                                break;
                            case "died":
                                connectionInfo.setLife(command);
                                break;
                        }
                    }
                    startWebSocket();
                }

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
        }
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
    public void onNotification(@NonNull String message) {
        String[] info = message.split(":");
        updateNotification("Web Notification", info[0]);
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
                Log.e("IOService", "Message " + message);

                JSONObject object = new JSONObject(message);
                Envelope envelope = new Envelope(object);

                returnMessageStatus(envelope);
                if (connectionInfo.getLife().equals("reborn")) {
                    addMessage(message);
                    messageManager.setMessage(envelope);

                } else {
                    messageManager.setMessage(envelope);
                    // Створюємо множину дозволених статусів
                    Set<String> allowedStatuses = new HashSet<>(Arrays.asList("message", "file"));

                    if (allowedStatuses.contains(envelope.getOperation())) {
                        soundPlayer.playNotificationSound(this);
                        updateNotification(envelope.getSenderId(),envelope.getMessageId());
                    }
//                    if ("ready".equals(envelope.getMessageStatus())) {
//                        Envelope saveEnvelope = messageManager.getMessageById(envelope.getMessageId());
//                        saveEnvelope.setMessageStatus(envelope.getMessageStatus());
//                        messageManager.setMessage(saveEnvelope, saveEnvelope.getTime());
//                        Log.e("IOService", connectionInfo.getLife() + " ready JSON message: " + envelope.toJson());
//
//                    } else {
//                        messageManager.setMessage(envelope);
//                        Log.e("IOService", connectionInfo.getLife() + " JSON message: " + envelope.toJson());
//
//                    }
                }

//                if (envelope.getMessageStatus().equals("server")) {
//                    messageManager.deleteMessageById(envelope.getMessageId());
//                    messageManager.setMessage(envelope);
//                } else if (envelope.getMessageStatus().equals("received")) {
//                    messageManager.deleteMessageById(envelope.getMessageId());
//                    messageManager.setMessage(envelope);
//                } else if (envelope.getMessageStatus().equals("delivered")) {
//                    messageManager.deleteMessageById(envelope.getMessageId());
//                    messageManager.setMessage(envelope);
//                }

            } catch (JSONException e) {
                Log.e("IOService", " JSON processing error while receiving message - " + e.getMessage());
                Log.e("IOService", "Not a JSON message: " + message);
            }
        }
    }


    /**
     * Метод обробки оффлайн-повідомлень, якщо вони є.
     * Отримуємо всі повідомлення, окрім тих, що стосуються операції "send".
     * Якщо повідомлення є типу "message" або "file", зберігаємо їх.
     * Всі інші повідомлення видаляємо, щоб уникнути накопичення нерелевантних даних.
     */
    private void getOfflineMessage() {
        HashMap<String, Envelope> messages = messageManager.getMessagesExceptOperation("send");

        for (Map.Entry<String, Envelope> entry : messages.entrySet()) {
            String messageId = entry.getKey();
            Envelope envelope = entry.getValue();
            saveMessage(envelope.toJson().toString());
            Log.e("IOService", "get  Offline Message ID" + messageId);
        }
        //Log.e("IOService", " Count " + messageManager.getMessageCountExceptByOperation("send"));
    }

    /**
     * Надсилає сповіщення серверу про отримання повідомлення.
     *
     * @param envelope отримане повідомлення, для якого потрібно надіслати підтвердження.
     *                 Використовуються наступні дані:
     *                 - @envelope.getSenderId() — ID відправника.
     *                 - @envelope.getReceiverId() — ID отримувача (тобто наш ID).
     *                 - @envelope.getMessageId() — ID отриманого повідомлення.
     */
    private void returnMessageStatus(@NonNull Envelope envelope) {
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

    public class SoundPlayer {
        private MediaPlayer mediaPlayer;

        public void playNotificationSound(Context context) {
            if (mediaPlayer != null) {
                mediaPlayer.release(); // Звільняємо попередній плеєр
            }
            mediaPlayer = MediaPlayer.create(context, R.raw.notification_2_310755); // Файл в res/raw/
            mediaPlayer.setOnCompletionListener(mp -> mp.release()); // Автоматичне закриття після завершення
            mediaPlayer.start(); // Відтворення
        }
    }
}

