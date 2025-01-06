package com.example.cube.socket;

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


public class IOService extends Service implements WebSocketClient.Listener{

    private BroadcastReceiver receiver;
    private WebSocketClient webSocketClient;
    private ServerURL serverURL;
    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "com.example.ID_SENDER":
                        String senderId = intent.getStringExtra("senderId");
                        Log.e("IOService", "com.example.ID_SENDER " + senderId);
                        serverURL.setSenderId(senderId);
                        break;
                    case "com.example.ID_RECIVER":
                        String receiverId = intent.getStringExtra("receiverId");
                        Log.e("IOService", "com.example.ID_RECIVER " + receiverId);
                        serverURL.setReciverId(receiverId);
                        break;
                    case "com.example.SEND_TO_SERVER":
                        String message = intent.getStringExtra("message");
                        Log.e("IOService","com.example.SEND_TO_SERVER " + message);
                        webSocketClient.sendMessage(message);
                        break;
                    case "com.example.IP_TO_SERVER":
                        String ip = intent.getStringExtra("ip");
                        Log.e("IOService", "com.example.IP_TO_SERVER " + ip);
                        serverURL.setIp(ip);
                        break;
                    case "com.example.PORT_TO_SERVER":
                        String port = intent.getStringExtra("port");
                        Log.e("IOService", "com.example.PORT_TO_SERVER " + port);
                        serverURL.setPort(port);
                        break;
                }
            }
        };

        // Реєстрація BroadcastReceiver з декількома фільтрами
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.ID_SENDER");
        filter.addAction("com.example.ID_RECIVER");
        filter.addAction("com.example.SEND_TO_SERVER");
        filter.addAction("com.example.IP_TO_SERVER");
        filter.addAction("com.example.PORT_TO_SERVER");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(receiver, filter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String senderId = intent.getStringExtra("com.example.ID_SENDER");
            String Ip_Address = intent.getStringExtra("com.example.IP_TO_SERVER");
            String Port = intent.getStringExtra("com.example.PORT_TO_SERVER");
            Log.e("MainActivity", "Listener ID_SENDER " + senderId+" "+Ip_Address+" "+Port);
            serverURL=new ServerURL();
            serverURL.setSenderId(senderId);
            serverURL.setIp(Ip_Address);
            serverURL.setPort(Port);
            webSocketClient=new WebSocketClient(this);
            webSocketClient.connect(serverURL.getServerAddress(),serverURL.getRegistration());
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void addMessage(String message) {
        Intent intent = new Intent("com.example.RECEIVED_MESSAGE");
        intent.putExtra("message", message);
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
                } else if (serverURL.getReciverId()!= null && serverURL.getReciverId().equals(envelope.getSenderId())) {
                    addMessage(message);
                } else {
                   saveMessage(message);
                }
            }
        } catch (JSONException e) {
            Log.e("MainActivity"," Помилка обробки JSON під час отримання повідомлення - " + e.getMessage());
            Log.e("MainActivity","Не JSON повідомлення"+message);
        }
    }

    private void saveMessage(String message) {
        Intent intent = new Intent("com.example.RECEIVED_MESSAGE");
        intent.putExtra("save_message", message);
        sendBroadcast(intent);  // Надсилання повідомлення Activity 1
    }
}
