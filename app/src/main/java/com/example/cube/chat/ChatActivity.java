package com.example.cube.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import com.example.cube.chat.message.BundleProcessor;
import com.example.cube.chat.message.ImageData;
import com.example.cube.R;
import com.example.cube.control.FIELD;
import com.example.cube.encryption.Encryption;
import com.example.cube.encryption.KeyGenerator;
import com.example.cube.chat.message.MessagesAdapter;
import com.example.cube.control.Side;
import com.example.cube.chat.message.Message;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.cube.databinding.ActivityChatBinding;
import com.example.folder.Folder;
import com.example.folder.dialogwindows.Open;
import com.example.qrcode.QR;
import com.example.qrcode.QRCode;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatActivity extends AppCompatActivity implements Folder, OperationMSG.OperableMSG {
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
    String test = "This is a text message at the beginning of your chat. " +
            "You have a simple interface for correspondence and file exchange." +
            " This development was initiated with a special vision to learn programming and improve your programming knowledge. " +
            "The repository of this development is at the link. " +
            "https://github.com/rifatismailov/CUBE " +
            "https://github.com/rifatismailov/";
    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Отримання даних від Activity1
            String dataFromActivity1 = intent.getStringExtra(FIELD.DATE_FROM_USERS_ACTIVITY.getFIELD());
            if (dataFromActivity1 != null) {
                // Обробка отриманих даних
                handleReceivedData(dataFromActivity1);
            }
        }
    };

    private void bundleProcessor() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            BundleProcessor bundleProcessor = new BundleProcessor(bundle);

            senderId = bundleProcessor.getSenderId();
            receiverName = bundleProcessor.getReceiverName();
            receiverId = bundleProcessor.getReceiverId();
            receiverStatus = bundleProcessor.getReceiverStatus();
            publicKey = bundleProcessor.getPublicKey();
            privateKey = bundleProcessor.getPrivateKey();
            receiverPublicKey = bundleProcessor.getReceiverPublicKey();
            senderKey = bundleProcessor.getSenderKey();
            receiverKey = bundleProcessor.getReceiverKey();

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Реєструємо ресівер для отримання даних
        registerDataReceiver();

        // Ініціалізуємо binding і toolbar
        setupUI();

        // Обробка даних з bundle
        bundleProcessor();

        // Встановлюємо початкові значення
        binding.status.setText(receiverStatus);
        binding.name.setText(receiverName);
        binding.profile.setImageBitmap(QRCode.getQRCode(receiverId));

        // Ініціалізація RecyclerView
        setupRecyclerView();

        // Відправляємо тестове повідомлення
        sendMessage(new Message(test, Side.Receiver, UUID.randomUUID().toString()));

        // Обробники натискання
        setupClickListeners();
    }

    private void registerDataReceiver() {
        IntentFilter filter = new IntentFilter(FIELD.DATA_TO_CHAT.getFIELD());
        registerReceiver(dataReceiver, filter);
    }

    private void setupUI() {
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(20);
        binding.recyclerView.setDrawingCacheEnabled(true);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.back.setOnClickListener(v -> {
            sendDataBackToActivity(FIELD.USER_END.getFIELD());
            finish();
        });

        binding.sendBtn.setOnClickListener(v -> {
            String messageTxt = binding.messageBox.getText().toString();
            if (!messageTxt.isEmpty()) {
                send(new Message(messageTxt, Side.Sender, UUID.randomUUID().toString()));
                binding.messageBox.setText("");
            }
        });

        binding.attachmentBtn.setOnClickListener(v -> new Open(ChatActivity.this));
        binding.profile.setOnClickListener(view -> new QR(this, receiverId));
    }


    private void handleReceivedData(String data) {
        new OperationMSG(this).onReceived(senderKey, data);
    }

    // Метод для відправки даних назад у Activity
    @Override
    public void sendDataBackToActivity(String response) {
        Intent intent = new Intent(FIELD.REPLY_FROM_CHAT.getFIELD());
        intent.putExtra(FIELD.DATA_FROM_CHAT.getFIELD(), response);
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
            new OperationMSG(this).onSend(senderId, receiverId, message.getMessage(), message.getMessageId(), receiverKey);
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }


    private void addMessageFile(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addFile(String url, String has) {
        try {
            String messageId = UUID.randomUUID().toString();
            if (url.endsWith(".jpg") || url.endsWith(".png")) {
                ImageData imageData = new ImageData().convertImage(url);
                Message message = new Message("", Uri.parse(url), imageData.getImageBytes(), imageData.getWidth(), imageData.getHeight(), Side.Sender, messageId);
                message.setHas(has);
                addMessageFile(message);
                new OperationMSG(this).onSendFile(senderId, receiverId, message.getMessage(), url, has, receiverKey, messageId);

            } else {
                Message message = new Message("There will be information about your message :\n", Uri.parse(url), Side.Sender, messageId);
                message.setHas(has);
                addMessageFile(message);
                new OperationMSG(this).onSendFile(senderId, receiverId, message.getMessage(), url, has, receiverKey, messageId);
            }
        } catch (Exception e) {
            Log.e("ChatActivity","Помилка під час додовання файлу :"+e);
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
            /**ОБЛАСТЬ ЦЬОГО КОДУ ТРЕБА ПЕРЕРРОБИТИ ТАК ЯК МИ СТВОРЮЄМО НОВИЙ ОБЄКТ Message А ЦЕ НЕ КОРЕКТНО ТОМУ ЩО КОЖНЕ ПОВІДОМЛЕННЯ МАЄ ID НОМЕР
             * ЗА ЯКИМ З НИМ МОЖНО ВЗАЄМОДІЯТИ */
            try {
                Message message;
                if (url.endsWith(".jpg") || url.endsWith(".png")) {
                    ImageData imageData = new ImageData().convertImage(url);
                    message = new Message("", Uri.parse(url), imageData.getImageBytes(), imageData.getWidth(), imageData.getHeight(), Side.Sender);
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
                Log.e("ChatActivity","Помилка під час відкриття файлу :"+e);
            }
        });
    }

    @Override
    public void sendMessage(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }

    @Override
    public void sendMessageFile(Message message) {
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }

    @Override
    public void addReceiverPublicKey(String rPublicKey) throws Exception {
        receiverPublicKey = keyGenerator.decodePublicKey(rPublicKey);
    }

    @Override
    public void addReceiverKey(String aesKey) throws Exception {
        receiverKey = Encryption.RSA.decrypt(aesKey, privateKey);
    }

    @Override
    public void addNotifier(String messageId, String messageStatus) {
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.getMessageId().equals(messageId)) {
                message.setMessageStatus(messageStatus);
                adapter.notifyItemChanged(i); // Оновлюємо лише один елемент
                break; // Завершуємо цикл, оскільки повідомлення знайдено
            }
        }
    }
}