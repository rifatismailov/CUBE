package com.example.cube.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import com.example.cube.chat.message.BundleProcessor;
import com.example.cube.chat.message.FileData;
import com.example.cube.R;
import com.example.cube.control.FIELD;
import com.example.cube.db.DatabaseHelper;
import com.example.cube.db.MessageManager;
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
import com.example.folder.file.FileOMG;
import com.example.qrcode.QR;
import com.example.qrcode.QRCode;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatActivity extends AppCompatActivity implements Folder, OperationMSG.OperableMSG, FileOMG {
    private ActivityChatBinding binding;
    private MessageManager manager;
    private MessagesAdapter adapter;
    private ArrayList<Message> messages;
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
        //DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = new DatabaseHelper(this).getWritableDatabase();
        manager = new MessageManager(db);


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
        //readMessage(new Message(test, Side.Receiver, UUID.randomUUID().toString()));

        // Обробники натискання
        setupClickListeners();
        showMessage();
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
        binding.camera.setOnClickListener(view -> clearMessage());
    }

    private void clearMessage() {
        manager.deleteMessagesByReceiverId(receiverId);
        finish();
    }

    private void showMessage() {
        List<Message> messagesdb = manager.getMessagesByReceiverId(receiverId);
        messages.addAll(messagesdb);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size()); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size()); // Прокрутити до нового елемента
        });
    }

    private void handleReceivedData(String data) {
        Log.e("Listener", "Data " + data);

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
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        manager.addMessage(message);
        messages.add(message);
        runOnUiThread(() -> {
            new OperationMSG(this).onSend(senderId, receiverId, message.getMessage(), message.getMessageId(), receiverKey);
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }


    private void addMessageFile(Message message) {
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        manager.addMessage(message);
        messages.add(message);
        runOnUiThread(() -> {
            adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addFile(String messageId,String url, String has) {
        try {
            Message message;
            FileData fileData;
            String fileName = new File(url).getName();
            if (url.endsWith(".jpg") || url.endsWith(".png")) {
                fileData = new FileData().convertImage(url);
                message = new Message("", Uri.parse(url), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Sender, messageId);
            } else {
                fileData = new FileData().convertFilePreviewLocal(fileName, url, has);
                message = new Message("There will be information about your message ", Uri.parse(url), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Sender, messageId);
            }
            message.setFileName(fileName);
            message.setFileSize(fileData.getFileSize(new File(url)));
            message.setTypeFile(fileData.getFileType(new File(url)));
            message.setHas(has);
            message.setDataCreate(fileData.getFileDate(new File(url)));
            addMessageFile(message);
            new OperationMSG(this).onSendFile(senderId, receiverId, message.getMessage(), url, has, receiverKey, messageId);

        } catch (Exception e) {
            Log.e("ChatActivity", "Помилка під час додовання файлу :" + e);
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItem(int position, @NonNull String url, String has) {
        updateItemAsync(position, url, has);
    }

    // Використовуємо ExecutorService для асинхронної обробки зображення
    private final ExecutorService update_execService = Executors.newSingleThreadExecutor();

    public void updateItemAsync(int position, @NonNull String url, String has) {
        Message message = messages.get(position);
        update_execService.execute(() -> {

            try {
                String fileName = new File(url).getName();
                FileData fileData;
                if (url.endsWith(".jpg") || url.endsWith(".png")) {
                    fileData = new FileData().convertImage(url);
                } else {
                    fileData = new FileData().convertFilePreviewLocal(fileName, url, has);
                }
                message.setUrl(Uri.parse(url));
                message.setImage(fileData.getImageBytes());
                message.setImageWidth(fileData.getWidth());
                message.setImageHeight(fileData.getHeight());
                message.setFileName(fileName);
                message.setFileSize(fileData.getFileSize(new File(url)));
                message.setTypeFile(fileData.getFileType(new File(url)));
                message.setHas(has);
                message.setDataCreate(fileData.getFileDate(new File(url)));

                // Оновлюємо адаптер у головному потоці після обробки
                runOnUiThread(() -> {
                    manager.updateMessage(message);
                    adapter.updateItem(position, message);
                    adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
                    // binding.recyclerView.smoothScrollToPosition(messages.size() - 1); // Прокрутити до нового елемента
                });

            } catch (Exception e) {
                Log.e("ChatActivity", "Помилка під час відкриття файлу :" + e);
            }
        });
    }

    // Перевірка, чи знаходиться RecyclerView на останній позиції
    private boolean isRecyclerViewAtBottom() {
        // Отримуємо поточну позицію прокручування
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
        assert layoutManager != null;
        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
        int totalItemCount = layoutManager.getItemCount();

        // Порівнюємо з останнім елементом у списку
        return lastVisiblePosition == totalItemCount - 2;
    }

    private void autoScroll(Message message) {
        adapter.notifyItemInserted(messages.size() - 1); // Повідомити, що новий елемент було вставлено
        if (isRecyclerViewAtBottom()) {
            // Якщо на останньому елементі — прокручуємо до нового
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    public void readMessage(Message message) {
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        manager.addMessage(message);
        messages.add(message);
        runOnUiThread(() -> autoScroll(message));
    }

    @Override
    public void readMessageFile(Message message) {
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        manager.addMessage(message);
        messages.add(message);
        runOnUiThread(() -> autoScroll(message));
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
                manager.updateMessage(message);
                adapter.notifyItemChanged(i); // Оновлюємо лише один елемент
                break; // Завершуємо цикл, оскільки повідомлення знайдено
            }
        }
    }
    @Override
    public void setProgressShow(String messageId,int progress){
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.getMessageId().equals(messageId)) {
                message.setProgress(progress);
                //manager.updateMessage(message);
                adapter.notifyItemChanged(i); // Оновлюємо лише один елемент
                break; // Завершуємо цикл, оскільки повідомлення знайдено
            }
        }
    }

}