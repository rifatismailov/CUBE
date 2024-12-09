package com.example.cube;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.example.cube.control.FIELD;
import com.example.cube.encryption.Encryption;
import com.example.cube.encryption.KeyGenerator;
import com.example.cube.message.MessagesAdapter;
import com.example.cube.control.Side;
import com.example.cube.message.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cube.databinding.ActivityChatBinding;
import com.example.cube.socket.Envelope;
import com.example.folder.Folder;
import com.example.folder.dialogwindows.Open;
import com.example.qrcode.QR;
import com.example.qrcode.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatActivity extends AppCompatActivity implements Folder {
    private ActivityChatBinding binding;

    MessagesAdapter adapter;
    ArrayList<Message> messages;
    private String senderId;       // ІД відправника
    private String receiverName;
    private String receiverId;
    private String receiverStatus;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private PublicKey receiverPublicKey;
    private KeyGenerator.RSA keyGenerator;
    private String senderKey;
    private String receiverKey;
    String test = "This is a photo of just one young family killed by a Russian missile attack " +
            "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
            "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
            "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA";
    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Отримання даних від Activity1
            String dataFromActivity1 = intent.getStringExtra("data_from_MainActivity");
            if (dataFromActivity1 != null) {
                // Обробка отриманих даних
                handleReceivedData(dataFromActivity1);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Реєструємо ресівер для отримання даних від Activity1
        IntentFilter filter = new IntentFilter("com.example.cube.DATA_TO_CHAT");
        registerReceiver(dataReceiver, filter);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        Bundle bundle = getIntent().getExtras();
        senderId = bundle.getString("senderId");
        receiverName = bundle.getString("name");
        receiverId = bundle.getString("receiverId");
        receiverStatus = bundle.getString("status");
        keyGenerator = new KeyGenerator.RSA();
        try {
            String sPublicKey = bundle.getString("publicKey");
            if (sPublicKey != null && !sPublicKey.isEmpty()) {
                publicKey = keyGenerator.decodePublicKey(sPublicKey);
            }
            String sPrivateKey = bundle.getString("privateKey");
            if (sPrivateKey != null && !sPrivateKey.isEmpty()) {
                privateKey = keyGenerator.decodePrivateKey(sPrivateKey);
            }
            String rPublicKey = bundle.getString("receiverPublicKey");
            if (rPublicKey != null && !rPublicKey.isEmpty()) {
                receiverPublicKey = keyGenerator.decodePublicKey(rPublicKey);
                Toast.makeText(this, "" + rPublicKey, Toast.LENGTH_SHORT).show();
            }
            senderKey = bundle.getString(FIELD.SENDER_KEY.getFIELD());
            receiverKey = bundle.getString(FIELD.RECEIVER_KEY.getFIELD());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        messages = new ArrayList<>();
        binding.status.setText(receiverStatus);
        binding.name.setText(receiverName);
        binding.profile.setImageBitmap(QRCode.getQRCode(receiverId));

        adapter = new MessagesAdapter(this, messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);


        receive(new Message("Hello my friend American English varieties", Side.Receiver));
        receive(new Message(test, Side.Receiver));

        binding.back.setOnClickListener(v -> {
            sendDataBackToActivity("endUser");
            finish();
        });
        binding.sendBtn.setOnClickListener(v -> {
            String messageTxt = binding.messageBox.getText().toString();
            if (!binding.messageBox.getText().toString().isEmpty()) {

                send(new Message(messageTxt, Side.Sender));
                //receive(new Message(messageTxt, Side.Receiver));
                binding.messageBox.setText("");
            }
        });
        binding.attachmentBtn.setOnClickListener(v -> {
            new Open(ChatActivity.this);
        });
        binding.profile.setOnClickListener(view -> {
            new QR(this, receiverId);
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void handleReceivedData(String data) {
        try {
            JSONObject object = new JSONObject(data);
            Envelope envelope = new Envelope(object);
            String operation = envelope.toJson().getString("operation");

            // Обробляємо дані від Activity1, наприклад, оновлюємо UI
            if ("message".equals(operation)) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                if (envelope.getFileUrl() == null) {
                    receive(new Message(rMessage, Side.Receiver));
                } else {
                    Message message = new Message(rMessage, Uri.parse(envelope.getFileUrl()), Side.Receiver);
                    message.setHas(envelope.getFileHash());
                    receiveFile(message);
                }
            } else if ("handshake".equals(operation)) {
                JSONObject jsonObject = new JSONObject(envelope.getMessage());
                // чому getString("publicKey"); тому що відправник вказує в повідомлення метрику як publicKey
                // тим вказує що це його публічний кул так ми його забираємо
                String rPublicKey = jsonObject.getString("publicKey");
                receiverPublicKey = keyGenerator.decodePublicKey(rPublicKey);
                Toast.makeText(this, "[" + receiverPublicKey + "]", Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e("ChatActivity", "Помилка під час отримання JSON: " +e);


        } catch (Exception e) {
            Log.e("ChatActivity", "Помилка під час отримання даних з MainActivity: " +e);
        }

        // Відправляємо дані назад у Activity1
        //sendDataBackToActivity(data);
    }

    // Метод для відправки даних назад у Activity
    private void sendDataBackToActivity(String response) {
        Intent intent = new Intent("com.example.cube.REPLY_FROM_CHAT");
        intent.putExtra("data_from_chat", response);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Від'єднання ресівера, коли активність знищується
        unregisterReceiver(dataReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void send(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            try {
                String rMessage = Encryption.AES.encrypt(message.getMessage(), receiverKey);
                Envelope envelope = new Envelope(senderId, receiverId, "message", rMessage);
                //реалізація шифрування повідомлення
                sendDataBackToActivity(envelope.toJson().toString());

                adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
                binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
            } catch (Exception e) {

            }

        });
    }


    private void sendFile(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }

    private void receive(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }

    private void receiveFile(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void openFile(String url, String has) {
        try {


            if (url.endsWith(".jpg") || url.endsWith(".png")) {
                {
                    Message message = new Message("", Uri.parse(url), Side.Sender);
                    message.setHas(has);
                    sendFile(convertImage("", url, has, Side.Sender));
                    String urls = "http://192.168.1.237:8020/api/files/download/" + new File(url).getName(); // Змініть IP на ваш
                    String rMessage = Encryption.AES.encrypt(message.getMessage(), receiverKey);
                    String rURL = Encryption.AES.encrypt(urls, receiverKey);
                    String rHAS = Encryption.AES.encrypt(has, receiverKey);
                    Envelope envelope = new Envelope(senderId, receiverId, "image", rMessage, rURL, rHAS);
                    Log.e("SendFile", urls);

                    sendDataBackToActivity(envelope.toJson().toString());
                }
           /* {
                String urls = "http://192.168.193.183:8080/api/files/download/" + new File(url).getName(); // Змініть IP на ваш
                Message message = new Message("There will be information about your message :\n", Uri.parse(urls), Side.Receiver);
                message.setHas(has);
                receiveFile(message);
                Envelope envelope=new Envelope("H652882301",receiverID,message.getMessage(),urls,has);
                sendDataBackToActivity(envelope.toJson().toString());
            }
            */
            } else {
                {
                    Message message = new Message("There will be information about your message :\n", Uri.parse(url), Side.Sender);
                    message.setHas(has);
                    sendFile(message);
                    String urls = "http://192.168.1.237/api/files/download/" + new File(url).getName(); // Змініть IP на ваш
                    String rMessage = Encryption.AES.encrypt(message.getMessage(), receiverKey);
                    String rURL = Encryption.AES.encrypt(urls, receiverKey);
                    String rHAS = Encryption.AES.encrypt(has, receiverKey);
                    Envelope envelope = new Envelope(senderId, receiverId, "file", rMessage, rURL, rHAS);
                    sendDataBackToActivity(envelope.toJson().toString());
                }

            /*{
                String urls = "http://192.168.193.183:8080/api/files/download/" + new File(url).getName(); // Змініть IP на ваш
                Message message = new Message("There will be information about your message :\n", Uri.parse(urls), Side.Receiver);
                message.setHas(has);
                receiveFile(message);
                Envelope envelope=new Envelope("H652882301",receiverID,message.getMessage(),urls,has);
                sendDataBackToActivity(envelope.toJson().toString());
            }
             */

            }
        }catch (Exception e){

        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItem(int position, @NonNull String url, String has) {
        updateItemAsync(position, url, has);
    }

    // Використовуємо ExecutorService для асинхронної обробки зображення
    private final ExecutorService update_execService = Executors.newSingleThreadExecutor();

    public void updateItemAsync(int position, @NonNull String url, String has) {
        update_execService.execute(() -> {
            try {
                Message message;
                if (url.endsWith(".jpg") || url.endsWith(".png")) {
                    message = convertImage("", url, has, Side.Receiver);
                } else {
                    message = new Message("There will be information about your message :\n", Uri.parse(url), Side.Receiver);
                    message.setHas(has);
                }

                // Оновлюємо адаптер у головному потоці після обробки
                runOnUiThread(() -> {
                    adapter.updateItem(position, message);
                    adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
                    binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
                });

            } catch (Exception e) {
                e.printStackTrace(); // Обробка винятків
            }
        });
    }


    private Message convertImage(String sendMessage, String url, String has, Side side) {
        // Налаштування для зменшення розміру зображення
        BitmapFactory.Options options = new BitmapFactory.Options();
        File file = new File(url);
        if (file.length() > 1050000) { // якщо файл більше 1 мб то зменшує розмір зображення в 2 рази
            options.inSampleSize = 2;  // Це зменшує розмір зображення в 2 рази
        }
        // Декодуємо зображення з налаштуванням inSampleSize
        Bitmap bitmap = BitmapFactory.decodeFile(url, options);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Компресія в JPEG з якістю 80%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

        Message message = new Message(sendMessage, Uri.parse(url), stream.toByteArray(), width, height, side);
        message.setHas(has);
        return message;
    }
}