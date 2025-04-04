package com.example.cube.chat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.example.cube.chat.message.BundleProcessor;
import com.example.cube.sound.SoundPlayer;
import com.example.folder.FileData;
import com.example.cube.R;
import com.example.cube.chat.message.MessageDiffCallback;
import com.example.cube.control.FIELD;
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

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cube.databinding.ActivityChatBinding;
import com.example.database_cube.DatabaseHelper;
import com.example.folder.explorer.FileExplorer;
import com.example.folder.file.Folder;
import com.example.folder.download.Downloader;
import com.example.folder.file.FileOMG;
import com.example.qrcode.QR;
import com.example.qrcode.QRCode;
import com.example.setting.UrlBuilder;
import com.example.setting.UserSetting;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatActivity extends AppCompatActivity implements Folder, OperationMSG.OperableMSG, FileOMG, Downloader.DownloaderHandler {
    private ActivityChatBinding binding;
    private MessageManager manager;
    private MessagesAdapter adapter;
    private ArrayList<Message> messages;
    private String senderId;       // ІД відправника
    private String receiverName;
    private String receiverLastName;
    private String receiverId;
    private String receiverStatus;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private PublicKey receiverPublicKey;
    private KeyGenerator.RSA keyGenerator;
    private String senderKey;
    private String receiverKey;
    private String avatarImageUrl;
    private String accountImageUrl;
    private String fileServerIP;
    private String fileServerPort;
    private OperationMSG operationMSG;
    private SoundPlayer soundPlayer;
    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            // Отримання даних від Activity1
            String dataFromActivity1 = intent.getStringExtra(FIELD.DATE_FROM_USERS_ACTIVITY.getFIELD());
            if (dataFromActivity1 != null) {
                // Обробка отриманих даних
                handleReceivedData(dataFromActivity1);
                Log.e("MainActivity", "Chat message: " + dataFromActivity1);

            }
            String status = intent.getStringExtra(FIELD.STATUS.getFIELD());
            if (status != null) {
                Log.e("ChatActivity", "status: " + status);

                // Встановлюємо статусу контакту

                switch (status) {
                    case "disconnect":
                        binding.imageAccount.updateStatusColor("00");
                        binding.status.setText("disconnect");
                        break;
                    case "died":
                        binding.status.setText("offline");
                        binding.imageAccount.updateStatusColor("01");
                        break;
                    case "reborn":
                        binding.status.setText("online");
                        binding.imageAccount.updateStatusColor("10");
                        break;
                }
            }
        }
    };

    private void bundleProcessor() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            BundleProcessor bundleProcessor = new BundleProcessor(bundle);
            senderId = bundleProcessor.getSenderId();
            receiverName = bundleProcessor.getReceiverName();
            receiverLastName = bundleProcessor.getReceiverLastName();
            receiverId = bundleProcessor.getReceiverId();
            receiverStatus = bundleProcessor.getReceiverStatus();
            publicKey = bundleProcessor.getPublicKey();
            privateKey = bundleProcessor.getPrivateKey();
            receiverPublicKey = bundleProcessor.getReceiverPublicKey();
            senderKey = bundleProcessor.getSenderKey();
            receiverKey = bundleProcessor.getReceiverKey();
            avatarImageUrl = bundleProcessor.getAvatarImageUrl();
            accountImageUrl = bundleProcessor.getAccountImageUrl();
            fileServerIP = bundleProcessor.getFileServerIP();
            fileServerPort = bundleProcessor.getFileServerPort();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = new DatabaseHelper(this).getWritableDatabase();
        manager = new MessageManager(db);
        operationMSG=new OperationMSG(this);
        soundPlayer = new SoundPlayer();
        // Реєструємо ресівер для отримання даних
        registerDataReceiver();

        // Ініціалізуємо binding і toolbar
        setupUI();

        // Обробка даних з bundle
        bundleProcessor();
//
//        // Встановлюємо початкові значення
//        if(receiverStatus!=null) {
//            if (receiverStatus.equals("00")) {
//                binding.status.setText("disconnect");
//            }
//            if (receiverStatus.equals("01")) {
//                binding.status.setText("offline");
//            }
//            if (receiverStatus.equals("10")) {
//                binding.status.setText("online");
//            }
//        }
        binding.name.setText(receiverName);

        if (accountImageUrl != null && !accountImageUrl.isEmpty()) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2; // Зменшити розмір у два рази
            Bitmap bitmap = BitmapFactory.decodeFile(accountImageUrl, options);
            binding.imageAccount.setImageBitmap(bitmap);
        } else {

            String jsonData = new UserSetting.Builder()
                    .setId(receiverId)
                    .setName(receiverName)
                    .setLastName(receiverLastName)
                    .build().toJson("userId", "name", "lastName").toString();
            binding.imageAccount.setImageBitmap(QRCode.getQRCode(jsonData, receiverName.substring(0, 2)));
        }
        // Ініціалізація RecyclerView
        setupRecyclerView();
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

        binding.attachmentBtn.setOnClickListener(v -> {
            String serverUrl = new UrlBuilder.Builder()
                    .setProtocol("http")
                    .setIp(fileServerIP)
                    .setPort(fileServerPort)
                    .setDirectory("/api/files/upload")
                    .build()
                    .buildUrl();
            FileExplorer inputDialog = new FileExplorer(this, serverUrl, senderKey);
            inputDialog.show();
        });

        binding.imageAccount.setOnClickListener(view -> {
            String jsonData = new UserSetting.Builder()
                    .setId(receiverId)
                    .setName(receiverName)
                    .setLastName(receiverLastName)
                    .build().toJson("userId", "name", "lastName").toString();
            new QR(this, jsonData, accountImageUrl).show();
        });

        binding.camera.setOnClickListener(view -> clearMessage());
    }

    private void clearMessage() {
        manager.deleteMessagesByReceiverId(receiverId);
        finish();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void showMessage() {
        List<Message> messagesdb = manager.getMessagesByReceiverId(receiverId);

        // Сортуємо повідомлення за часом (перетворюємо String у long)
        Collections.sort(messagesdb, (m1, m2) -> {
            long time1 = parseTimestamp(m1.getTimestamp());
            long time2 = parseTimestamp(m2.getTimestamp());
            return Long.compare(time1, time2);
        });

        messages.addAll(messagesdb);

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged(); // Оновлюємо адаптер
            binding.recyclerView.smoothScrollToPosition(messages.size()); // Прокручуємо вниз
        });
    }


    private void handleReceivedData(String data) {
        operationMSG.onReceived(senderKey, data);
    }

    // Метод для відправки даних назад у Activity
    @Override
    public void sendDataBackToActivity(String response) {
        Intent intent = new Intent(FIELD.REPLY_FROM_CHAT.getFIELD());
        intent.putExtra(FIELD.DATA_FROM_CHAT.getFIELD(), response);
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Надсилаємо Intent про засинання
        Intent intent = new Intent(FIELD.REPLY_FROM_CHAT.getFIELD());
        intent.putExtra("sleep", receiverId);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Надсилаємо Intent про пробудження
        Intent intent = new Intent(FIELD.REPLY_FROM_CHAT.getFIELD());
        intent.putExtra("awake", receiverId);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Від'єднання ресівера, коли активність знищується
        sendDataBackToActivity(FIELD.USER_END.getFIELD());
        unregisterReceiver(dataReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void send(Message message) {
        runOnUiThread(() -> {
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            message.setTimestamp(getTime());
            manager.addMessage(message);

            // Створюємо копію списку перед оновленням
            List<Message> newList = new ArrayList<>(messages);
            newList.add(message);

            // Відправляємо повідомлення
            operationMSG.onSend(senderId, receiverId, message.getMessage(), message.getMessageId(), receiverKey, message.getTimestamp());

            // Використовуємо DiffUtil для оновлення списку
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(messages, newList));

            messages.clear();
            messages.addAll(newList);
            diffResult.dispatchUpdatesTo(adapter);

            // Плавна прокрутка до останнього повідомлення
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
        });
    }


    private void addMessageFile(Message message, String encFile) {
        runOnUiThread(() -> {
            message.setSenderId(senderId);
            message.setReceiverId(receiverId);
            manager.addMessage(message);

            // Створюємо копію списку для DiffUtil
            List<Message> newList = new ArrayList<>(messages);
            newList.add(message);

            // Будуємо URL
            String fileName = new File(encFile).getName();
            String url = new UrlBuilder.Builder()
                    .setProtocol("http")
                    .setIp(fileServerIP)
                    .setPort(fileServerPort)
                    .setDirectory("/api/files/download/")
                    .setFileName(fileName)
                    .build()
                    .buildUrl();

            // Відправляємо файл
            //            operationMSG.onSendFile(senderId, receiverId, message.getMessage(), url, message.getHas(), receiverKey, message.getMessageId(), message.getTimestamp());
            operationMSG.onSendFile(message, url, receiverKey);

            // Використовуємо DiffUtil для оптимального оновлення
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(messages, newList));

            messages.clear();
            messages.addAll(newList);
            diffResult.dispatchUpdatesTo(adapter);

            // Плавна прокрутка до нового повідомлення
            binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
        });
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void addFile(String messageId, String url, String encFile, String has) {
        try {
            Message message;
            FileData fileData;
            String fileName = new File(url).getName();
            String messageTxt = binding.messageBox.getText().toString();

            if (!messageTxt.isEmpty()) {
                binding.messageBox.setText("");
            }

            if (url.matches(".*\\.(jpg|jpeg|png|webp|bmp|gif|heic|heif|tiff|tif)$")) {
                fileData = new FileData().convertImage(url);
                message = new Message(messageTxt, Uri.parse(url), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Sender, messageId);
            } else {
                fileData = new FileData().convertFilePreviewLocal(fileName, url, has);
                message = new Message(messageTxt, Uri.parse(url), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Sender, messageId);
            }

            message.setFileName(fileName);
            message.setFileSize(fileData.getFileSize(new File(url)));
            message.setTypeFile(fileData.getFileType(new File(url)));
            message.setHas(has);
            message.setDataCreate(fileData.getFileDate(new File(url)));
            message.setTimestamp(getTime());
            addMessageFile(message, encFile);

        } catch (Exception e) {
            Log.e("ChatActivity", "Error adding file :" + e);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItem(int position, String positionId, @NonNull String url, String has) {
        updateItemAsync(position, positionId, url, has);
    }

    private final ExecutorService update_execService = Executors.newSingleThreadExecutor();

    public void updateItemAsync(int position, String positionId, @NonNull String url, String has) {
        update_execService.execute(() -> {
            try {
                File file = new File(url);
                String fileName = file.getName();
                FileData fileData;

                // Визначаємо, чи є файл зображенням
                if (url.matches(".*\\.(jpg|jpeg|png|webp|bmp|gif|heic|heif|tiff|tif)$")) {
                    fileData = new FileData().convertImage(url);
                } else {
                    fileData = new FileData().convertFilePreviewLocal(fileName, url, has);
                }

                // Оновлюємо повідомлення в основному списку
                Message updatedMessage = messages.get(position);

                // Оновлюємо дані в повідомленні
                updatedMessage.setUrl(Uri.parse(url));
                updatedMessage.setImage(fileData.getImageBytes());
                updatedMessage.setImageWidth(fileData.getWidth());
                updatedMessage.setImageHeight(fileData.getHeight());
                updatedMessage.setFileName(fileName);
                updatedMessage.setFileSize(fileData.getFileSize(file));
                updatedMessage.setTypeFile(fileData.getFileType(file));
                updatedMessage.setHas(has);
                updatedMessage.setDataCreate(fileData.getFileDate(file));
                updatedMessage.setMessageStatus("loaded");

                // Оновлюємо в адаптері без використання DiffUtil
                runOnUiThread(() -> {
                    // Оновлення даних у базі
                    manager.updateMessage(updatedMessage);

                    // Оновлення елемента в адаптері
                    adapter.notifyItemChanged(position);
                });

            } catch (Exception e) {
                Log.e("ChatActivity", "Error opening file: " + e);
            }
        });
    }


    // Перевірка, чи знаходиться RecyclerView на останній позиції
    private boolean isRecyclerViewAtBottom() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
        if (layoutManager == null) return false;

        int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
        int totalItemCount = layoutManager.getItemCount();

        return totalItemCount > 0 && lastVisiblePosition >= totalItemCount - 2;
    }

    private void autoScroll() {
        //adapter.notifyDataSetChanged(); // Оновлює весь список
        adapter.notifyItemInserted(messages.size() - 1);
        binding.recyclerView.postDelayed(() -> {
            if (isRecyclerViewAtBottom()) {
                binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
            }
        }, 100); // Додаємо затримку
    }

    /**
     * Метод додовання повідомлення яке прийшло від сервера
     *
     * @param message повідомлення яке прийшло
     */


    @Override
    public void readMessage(Message message) {
//        new OperationMSG(this).returnAboutDeliver(message);

        try {
            boolean messageExists = false; // Позначка для перевірки, чи знайдено повідомлення
            List<Message> newList = new ArrayList<>(messages); // Копія старого списку для DiffUtil

            for (int i = 0; i < messages.size(); i++) {
                Message currentMessage = messages.get(i);
                if (message.getMessageId().equals(currentMessage.getMessageId())) {
                    // Перевірка хешу повідомлення на дублювання
                    if (currentMessage.getHash_m().equals(message.getHash_m())) {
                        return; // Повідомлення дубльоване, виходимо з методу
                    }
                    // Оновлення існуючого повідомлення
                    manager.updateMessage(message);
                    adapter.notifyItemChanged(i);
                    messageExists = true;
                    break;
                }
            }

            if (!messageExists) { // Якщо повідомлення не знайдено
                message.setSenderId(senderId);
                message.setReceiverId(receiverId);
                manager.addMessage(message);  // Додаємо нове повідомлення
                newList.add(message);
                // Оновлення списку за допомогою DiffUtil
                // **Сортуємо за часом** (від старих до нових)
                Collections.sort(newList, (m1, m2) -> {
                    long time1 = parseTimestamp(m1.getTimestamp());
                    long time2 = parseTimestamp(m2.getTimestamp());
                    return Long.compare(time1, time2);
                });
                // Використовуємо DiffUtil для плавного оновлення
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(messages, newList));
                messages.clear();
                messages.addAll(newList);

                runOnUiThread(() -> {
                    diffResult.dispatchUpdatesTo(adapter);
                    autoScroll(); // Прокрутка вниз
                });

                if (!messageExists) {
                    soundPlayer.playNotificationSoundChat(this);
                }
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "Error while receiving message: " + e);
        }
    }
    // Метод для безпечного перетворення рядка у long
    private long parseTimestamp(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException | NullPointerException e) {
            return 0; // або інше значення за замовчуванням
        }
    }

    /**
     * Метод додовання повідомлення з файлом від сервера
     *
     * @param message повідомлення яке прийшло
     */
    @Override
    public void readMessageFile(Message message) {
//        new OperationMSG(this).returnAboutDeliver(message);
        try {
            boolean messageExists = false; // Позначка для перевірки, чи знайдено повідомлення
            List<Message> newList = new ArrayList<>(messages); // Копія старого списку для DiffUtil

            for (int i = 0; i < messages.size(); i++) {
                Message currentMessage = messages.get(i);
                if (message.getMessageId().equals(currentMessage.getMessageId())) {
                    // Перевірка хешу файлу на дублювання
                    if (currentMessage.getHash_f().equals(message.getHash_f())) {
                        return; // Повідомлення дубльоване, виходимо з методу
                    }
                    // Оновлення існуючого повідомлення
                    manager.updateMessage(message);
                    adapter.notifyItemChanged(i); // Оновлюємо список
                    messageExists = true;
                    break;
                }
            }

            if (!messageExists) { // Якщо повідомлення не знайдено
                message.setSenderId(senderId);
                message.setReceiverId(receiverId);
                manager.addMessage(message);  // Додаємо нове повідомлення
                newList.add(message);
                // **Сортуємо за часом** (від старих до нових)
                Collections.sort(newList, (m1, m2) -> {
                    long time1 = parseTimestamp(m1.getTimestamp());
                    long time2 = parseTimestamp(m2.getTimestamp());
                    return Long.compare(time1, time2);
                });
                // Оновлення списку за допомогою DiffUtil
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MessageDiffCallback(messages, newList));
                messages.clear();
                messages.addAll(newList);
                runOnUiThread(() -> {
                    diffResult.dispatchUpdatesTo(adapter);
                    autoScroll(); // Автоскрол після оновлення
                });
                soundPlayer.playNotificationSoundChat(this);
            }

        } catch (Exception e) {
            Log.e("ChatActivity", "Error while receiving message with file: " + e);
        }
    }


    @Override
    public void addReceiverPublicKey(String rPublicKey) throws Exception {
        receiverPublicKey = keyGenerator.decodePublicKey(rPublicKey);
    }

    @Override
    public void addReceiverKey(String aesKey) throws Exception {
        receiverKey = Encryption.RSA.decrypt(aesKey, privateKey);
    }

    int i = 0;

    @Override
    public void addNotifier(String messageId, String messageStatus) {
        Log.e("MainActivity", messageId+ " message: " + messageStatus);

        runOnUiThread(() -> {
            for (int i = 0; i < messages.size(); i++) {
                Message message = messages.get(i);
                if (message.getMessageId().equals(messageId)) {
                    if(message.getMessageStatus()!=null) {
                        if (!message.getMessageStatus().equals("received")) {
                            message.setMessageStatus(messageStatus);
                            manager.updateMessage(message);
                        }
                    }else {
                        message.setMessageStatus(messageStatus);
                        manager.updateMessage(message);
                    }

                    // Перевірка коректності адаптера
                    if (adapter != null) {
                        adapter.notifyItemChanged(i); // Оновлюємо тільки змінений елемент
                    } else {
                        Log.e("ChatActivity", "Adapter is NULL. Unable to notify");
                    }
                    break;
                }
            }
            // Створюємо множину дозволених статусів
            Set<String> allowedStatuses = new HashSet<>(Arrays.asList("delivered", "received", "server","ready"));

            if (allowedStatuses.contains(messageStatus)) {
                Message message = new Message();
                message.setMessageId(messageId);
                message.setSenderId(senderId);
                message.setReceiverId(receiverId);
                operationMSG.returnAboutDeliver(message, "update_to_message");
            }

        });
    }


    @Override
    public void setProgressShow(String messageId, int progress, String info) {
        try {
            runOnUiThread(() -> {
                for (int i = 0; i < messages.size(); i++) {
                    Message message = messages.get(i);
                    if (message.getMessageId().equals(messageId)) {
                        if (progress == 100) {
                            manager.updateMessage(message);
                            adapter.notifyItemChanged(i); // Оновлюємо лише один елемент
                        }
                        if (info.startsWith("ERROR")) {
                            String[] error = info.split(":");
                            message.setTimestamp(error[1]);
                            manager.updateMessage(message);
                            adapter.notifyItemChanged(i); // Оновлюємо лише один елемент
                        }
                        message.setProgress(progress);
                        adapter.notifyItemChanged(i); // Оновлюємо лише один елемент
                        break; // Завершуємо цикл, оскільки повідомлення знайдено
                    }
                }
            });
        } catch (Exception e) {

        }

    }

    /**Метод для оповіщення закінчення завантаження файлу на сервер */
    @Override
    public void endProgress(String messageId, String info) {
//        String messageJson = new Envelope.Builder().
//                setSenderId(senderId).
//                setReceiverId(receiverId).
//                setOperation("messageStatus").
//                setMessageStatus("ready").
//                setMessageId(messageId).
//                build().
//                toJson("senderId", "receiverId", "operation", "messageStatus", "messageId").
//                toString();
//        sendDataBackToActivity(messageJson);

    }

    private String getTime() {
        Date currentDate = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Формат дати і часу
        return formatter.format(currentDate);
    }

    @Override
    public String getKey(String positionId) {
        return receiverKey;
    }

}