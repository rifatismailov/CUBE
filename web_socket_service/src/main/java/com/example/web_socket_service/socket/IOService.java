

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private boolean activityLife = false;
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
        // Initialize the BroadcastReceiver to listen for multiple events
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    switch (Objects.requireNonNull(intent.getAction())) {
                        case "CUBE_ID_SENDER":
                            senderId = intent.getStringExtra("senderId");
                            Log.e("IOService", "CUBE_ID_SENDER " + senderId);
                            synchronized (connectionInfo) {
                                connectionInfo.setSenderId(senderId);// Оновлюємо лише значення
                            }
                            break;
                        case "CUBE_ID_RECIVER":
                            receiverId = intent.getStringExtra("receiverId");
                            Log.e("IOService", "CUBE_ID_RECIVER " + receiverId);
                            synchronized (connectionInfo) {
                                connectionInfo.setReciverId(receiverId); // Оновлюємо лише значення
                            }
                            Log.e("IOService", "Updated server URL: " + connectionInfo.getServerAddress());
                            break;
                        case "CUBE_SEND_TO_SERVER":
                            setMessage(intent.getStringExtra("message"));
                            break;
                        case "CUBE_IP_TO_SERVER":
                            ip = intent.getStringExtra("ip");
                            Log.e("IOService", "CUBE_IP_TO_SERVER " + ip);
                            synchronized (connectionInfo) {
                                connectionInfo.setIp(ip);// Оновлюємо лише значення
                            }
                            break;
                        case "CUBE_PORT_TO_SERVER":
                            port = intent.getStringExtra("port");
                            Log.e("IOService", "CUBE_PORT_TO_SERVER " + port);
                            synchronized (connectionInfo) {
                                connectionInfo.setPort(port);// Оновлюємо лише значення
                            }
                            break;
                        case "MAIN_ACTIVITY_LIFE":
                            setActivityLife(intent.getStringExtra("LIFE"));
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
        filter.addAction("MAIN_ACTIVITY_LIFE");
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

    private void setActivityLife(String Life) {
        try {
            switch (Life) {
                case "reborn":
                    activityLife = true;
                    Log.e("IOService", "MAIN_ACTIVITY_LIFE reborn Status Second:" + activityLife);
                    break;
                case "died":
                    activityLife = false;
                    Log.e("IOService", "MAIN_ACTIVITY_LIFE died Status :" + activityLife);
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
            Log.e("IOService", "Message " + object);

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
                    break;
                case "update_to_user":
                    Log.e("IOService", "Update message " + status + " ID message " + envelope.getMessageId());
                    break;
                default:
                    Log.e("IOService", "Default case triggered with status: " + status);
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
            Log.e(TAG, "Sending message: " + message);
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
            senderId = intent.getStringExtra("CUBE_ID_SENDER");
            ip = intent.getStringExtra("CUBE_IP_TO_SERVER");
            port = intent.getStringExtra("CUBE_PORT_TO_SERVER");
            String life = intent.getStringExtra("MAIN_ACTIVITY_LIFE");
            try {
                switch (Objects.requireNonNull(life)) {
                    case "reborn":
                        activityLife = true;
                        Log.e("IOService", "MAIN_ACTIVITY_LIFE reborn Status first:" + activityLife);
                        getOfflineMessage();
                        break;
                    case "died":
                        activityLife = false;
                        Log.e("IOService", "MAIN_ACTIVITY_LIFE died Status :" + activityLife);
                        break;
                }
            } catch (Exception e) {
                Log.e("IOService", "Під час отримання житті-діяльності активності було отримано null:" + e);
            }

            connectionInfo.setSenderId(senderId);
            connectionInfo.setIp(ip);
            connectionInfo.setPort(port);
            webSocketClient = new WebSocketClient(this);
            webSocketClient.connect(connectionInfo.getServerAddress(), connectionInfo.getRegistration());
            updateNotification("CUBE is running", "Server address " + ip);
        }
        return START_STICKY;
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
     *                по состоянію активності ми робимо такі дії:
     *                Перше якшо активність доступна ми передаємо його до активності в залежності який там стан
     *                -> Чи проводитися там чат чи ні.
     *                Друге якщо в нас активність не активна то ми зберігаємо до бази даних тільки ті повідомлення які мають заголовки повідомлення або файл
     */
    @Override
    public void onListener(String message) {

        if (message != null) {
            try {
                JSONObject object = new JSONObject(message);
                Envelope envelope = new Envelope(object);

                if (activityLife) {
                    synchronized (connectionInfo) {
                        if (connectionInfo.getSenderId() != null) {
                            if (connectionInfo.getSenderId().equals(envelope.getSenderId()) &&
                                    connectionInfo.getReceiverId().equals(envelope.getReceiverId())) {
                                addMessage(message);
                            } else if (connectionInfo.getReceiverId() != null && connectionInfo.getReceiverId().equals(envelope.getSenderId())) {
                                Log.e("IOService", "Send Message to chat Activity ID : [" + connectionInfo.getReceiverId() + "] " + (connectionInfo.getReceiverId() != null && connectionInfo.getReceiverId().equals(envelope.getSenderId())) + " [" + envelope.getSenderId() + "]");
                                addMessage(message);
                            } else {
                                Log.e("IOService", "Save Message ID : [" + connectionInfo.getReceiverId() + "] " + (connectionInfo.getReceiverId() != null && connectionInfo.getReceiverId().equals(envelope.getSenderId())) + " [" + envelope.getSenderId() + "]");
                                saveMessage(message);
                            }
                        }else{
                            Log.e("IOService", "Receiver ID :"+connectionInfo.getReceiverId());
                        }
                    }
                    returnMessageDeliver(envelope);
                } else {
                    Log.e("IOService", "Listener message  " + message);

                    /*Імовірність того що данні запишуться у базу даних коли активність відсутня 0 %.  в моєму випадку так як
                     * я не зміг поки подолати цю проблема щоб веб сокет міг отримувати та передавати данні на сервер в мене обривається зв'язок*/
                    if (envelope.getOperation().equals("message") || envelope.getOperation().equals("file")) {
//                        messageQueue.offer(envelope);
//
//                        if (messageQueue.size() > 1) {  // Записуємо після накопичення 2 повідомлень (можна змінити)
//                            executorService.submit(this::saveMessagesToDatabase);
//                        }
                        messageManager.setMessage(envelope);
                        returnMessageDeliver(envelope);

                    }
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

