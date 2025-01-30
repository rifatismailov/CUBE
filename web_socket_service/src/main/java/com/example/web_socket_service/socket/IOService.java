package com.example.web_socket_service.socket;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;


public class IOService extends Service implements WebSocketClient.Listener {

    private WebSocketClient webSocketClient;
    private ServerURL serverURL;

    @Override
    public void onCreate() {
        super.onCreate();
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "CUBE_ID_SENDER":
                        String senderId = intent.getStringExtra("senderId");
                        Log.e("IOService", "CUBE_ID_SENDER " + senderId);
                        serverURL.setSenderId(senderId);
                        break;
                    case "CUBE_ID_RECIVER":
                        String receiverId = intent.getStringExtra("receiverId");
                        Log.e("IOService", "CUBE_ID_RECIVER " + receiverId);
                        serverURL.setReciverId(receiverId);
                        break;
                    case "CUBE_SEND_TO_SERVER":
                        String message = intent.getStringExtra("message");
                        Log.e("IOService", "CUBE_SEND_TO_SERVER " + message);
                        webSocketClient.sendMessage(message);
                        break;
                    case "CUBE_IP_TO_SERVER":
                        String ip = intent.getStringExtra("ip");
                        Log.e("IOService", "CUBE_IP_TO_SERVER " + ip);
                        serverURL.setIp(ip);
                        break;
                    case "CUBE_PORT_TO_SERVER":
                        String port = intent.getStringExtra("port");
                        Log.e("IOService", "CUBE_PORT_TO_SERVER " + port);
                        serverURL.setPort(port);
                        break;
                }
            }
        };

        // Реєстрація BroadcastReceiver з декількома фільтрами
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String senderId = intent.getStringExtra("CUBE_ID_SENDER");
            String Ip_Address = intent.getStringExtra("CUBE_IP_TO_SERVER");
            String Port = intent.getStringExtra("CUBE_PORT_TO_SERVER");
            serverURL = new ServerURL();
            serverURL.setSenderId(senderId);
            serverURL.setIp(Ip_Address);
            serverURL.setPort(Port);
            webSocketClient = new WebSocketClient(this);
            webSocketClient.connect(serverURL.getServerAddress(), serverURL.getRegistration());
        }
        return START_STICKY;
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

    /**
     * Обробляє отримані повідомлення від сервера.
     * Якщо повідомлення отримано від цього ж користувача або для нього, воно передається в listener.
     * Якщо повідомлення не для цього користувача, воно зберігається.
     *
     * @param message Повідомлення, яке отримано від сервера.
     */

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
