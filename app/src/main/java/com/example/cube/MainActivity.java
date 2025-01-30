package com.example.cube;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.cube.chat.ChatActivity;
import com.example.cube.contact.ContactCreator;
import com.example.cube.contact.ContactInterface;
import com.example.cube.contact.UserAdapter;
import com.example.cube.contact.UserData;
import com.example.cube.databinding.ActivityMainBinding;
import com.example.cube.db.DatabaseHelper;
import com.example.cube.db.ContactManager;
import com.example.cube.encryption.Encryption;
import com.example.cube.encryption.KeyGenerator;
import com.example.cube.log.LogAdapter;
import com.example.cube.log.Logger;
import com.example.cube.navigation.NavigationManager;
import com.example.cube.permission.Permission;
import com.example.cube.control.FIELD;
import com.example.folder.download.Downloader;
import com.example.folder.file.FileDetect;
import com.example.folder.file.FileOMG;
import com.example.folder.file.Folder;
import com.example.folder.upload.FileEncryption;
import com.example.image_account.ImageExplorer;
import com.example.qrcode.QR;
import com.example.web_socket_service.socket.Envelope;
import com.example.web_socket_service.socket.IOService;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Основний клас активності MainActivity, що відповідає за обробку даних користувача, підключення до сервера та взаємодію з контактами.
 * Реалізує кілька інтерфейсів для обробки кліків та отримання повідомлень від сервера.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener, ContactCreator.CreatorOps, Manager.AccountOps, Operation.Operable,
        NavigationManager.Navigation, AdapterView.OnItemLongClickListener, ImageExplorer.ImgExplorer,
        ContactInterface, FileOMG, Folder ,Downloader.DownloaderHandler{

    private ActivityMainBinding binding;
    private TextView user_name;
    private TextView user_id;
    private String userId;           // ID користувача
    private String receiverId = null;       // ID отримувача
    private String name;             // Ім'я користувача
    private String lastName;         // Прізвище користувача
    private String password;         // Пароль
    private String imageOrgName;
    private String imageName;
    private UserData user;           // Об'єкт користувача
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private ContactManager contactManager;
    private Manager manager;
    private final List<UserData> userList = new ArrayList<>();  // Список користувачів
    private Map<String, UserData> contacts = new HashMap<>();  // Контакти користувачів

    private List<Logger> logs;
    private LogAdapter logAdapter;

    private SecretKey secretKey;  // AES-ключ

    private final HashMap<Integer, Envelope> saveMessage = new HashMap<>();  // Збережені повідомлення
    private HashMap<String, String> avatar_map = new HashMap<>();

    private int numMessage = 0;  // Лічильник повідомлень

    private UserAdapter userAdapter;                // Адаптер для відображення користувачів
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private DrawerLayout drawerLayout;
    private NavigationManager navigationManager;

    @Override
    public void setAccount(String userId, String name, String lastName, String password, String imageOrgName, String imageName) {
        String accountName = name + " " + lastName;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.imageOrgName = imageOrgName;
        this.imageName = imageName;
        binding.id.setText(userId);
        binding.name.setText(accountName);
        user_name.setText(accountName);
        user_id.setText(userId);
        navigationManager.setAvatarImage(imageOrgName);
        navigationManager.setAccountImage(imageName);
    }

    /**
     * Додає новий контакт, отриманий через QR-код або інші джерела.
     *
     * @param id_contact дані контакту у вигляді рядка JSON.
     */
    @Override
    public void setContact(String id_contact, String public_key_contact, String name_contact) {
        // Додаємо новий контакт до списку користувачів
        UserData newUser = new UserData(id_contact, public_key_contact, name_contact, "");
        userList.add(newUser);

        // Оновлюємо мапу контактів
        contacts.put(newUser.getId(), newUser);

        // Зберігаємо контакти у базу даних
        contactManager.setContacts(contacts, secretKey);
    }


    /**
     * BroadcastReceiver для отримання даних з ChatActivity через broadcast.
     */
    private final BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String dataFromChat = intent.getStringExtra(FIELD.DATA_FROM_CHAT.getFIELD());
            if (dataFromChat != null) {
                receivingData(dataFromChat);
            }
        }
    };


    private final BroadcastReceiver serverMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(FIELD.CUBE_RECEIVED_MESSAGE.getFIELD())) {
                String message = intent.getStringExtra(FIELD.MESSAGE.getFIELD());

                if (message != null) {
                    onReceived(message);
                }

                String save_message = intent.getStringExtra(FIELD.SAVE_MESSAGE.getFIELD());
                if (save_message != null) {
                    saveMessage(save_message);
                }
                String notification = intent.getStringExtra(FIELD.NOTIFICATION.getFIELD());
                if (notification != null) {
                    //saveMessage(save_message);
                    setNotification("", notification);
                }
            }
        }
    };


    /**
     * Метод onCreate викликається при створенні активності.
     *
     * @param savedInstanceState Збережений стан активності.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Permission(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        logs = new ArrayList<>();
        logAdapter = new LogAdapter(this, logs);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.log.setLayoutManager(layoutManager);
        binding.log.setAdapter(logAdapter);

        // Ініціалізація DrawerLayout і NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);

        // Створення ActionBarDrawerToggle для відкриття/закриття меню
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, null,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        user_name = findViewById(R.id.user_name);
        user_id = findViewById(R.id.user_id);

        // Використання NavigationManager для обробки меню
        navigationManager = new NavigationManager(this, drawerLayout, findViewById(R.id.avatarImage), findViewById(R.id.accountImage),
                findViewById(R.id.nav_account), findViewById(R.id.nav_settings), findViewById(R.id.nav_logout));


        password = "1234567890123456";  // Пароль
        byte[] keyBytes = password.getBytes();  // Генерація байт ключа
        secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        manager = new Manager(this, db, secretKey);
        manager.readAccount();
        contactManager = new ContactManager(db);


        registerServer();

        startService();
        binding.setting.setOnClickListener(this);
        binding.fab.setOnClickListener(this);

        initUserList();
        new Handler().postDelayed(() -> {
            if (userId == null)
                scannerQrAccount();
        }, 100);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Ініціалізація списку користувачів.
     */
    private void initUserList() {
        userList.clear();
        contacts = contactManager.getContacts(secretKey);
        for (Map.Entry<String, UserData> entry : contacts.entrySet()) {
            userList.add(entry.getValue());
        }
        userAdapter = new UserAdapter(this, R.layout.iteam_user, userList);
        binding.contentMain.userList.setAdapter(userAdapter);
        binding.contentMain.userList.setOnItemClickListener(this);
        binding.contentMain.userList.setOnItemLongClickListener(this);
    }


    /**
     * Отримує дані з активності чату та передає їх на сервер.
     *
     * @param data Отримані дані.
     */
    private void receivingData(String data) {
        if (data.equals("endUser")) {
            receiverId = null;
            notifyIdReciverChanged(receiverId);
        } else {
            if (user != null) {
                if (!data.isEmpty()) {
                    if (user.getReceiverPublicKey() != null) {
                        Log.e("MainActivity", data);

                        sendMessageToService(data);
                    }
                }
            }
        }
    }

    /**
     * Видалення ресивера при знищенні активності.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dataReceiver);
        unregisterReceiver(serverMessageReceiver);
    }

    /**
     * Метод для запуску ChatActivity з передачею даних про користувача.
     *
     * @param view     Поточний елемент View.
     * @param userData Дані користувача для чату.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void startChat(View view, @NonNull UserData userData) {
        IntentFilter filter = new IntentFilter(FIELD.REPLY_FROM_CHAT.getFIELD());
        registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(FIELD.SENDER_ID.getFIELD(), userId);
        intent.putExtra(FIELD.NAME.getFIELD(), userData.getName());
        intent.putExtra(FIELD.RECEIVER_ID.getFIELD(), userData.getId());
        intent.putExtra(FIELD.STATUS.getFIELD(), "online");
        intent.putExtra(FIELD.PUBLIC_KEY.getFIELD(), userData.getPublicKey());
        intent.putExtra(FIELD.PRIVATE_KEY.getFIELD(), userData.getPrivateKey());
        intent.putExtra(FIELD.RECEIVER_PUBLIC_KEY.getFIELD(), userData.getReceiverPublicKey());
        intent.putExtra(FIELD.SENDER_KEY.getFIELD(), userData.getSenderKey());
        intent.putExtra(FIELD.RECEIVER_KEY.getFIELD(), userData.getReceiverKey());
        startActivity(intent);
    }

    private void registerServer() {
        IntentFilter filter = new IntentFilter(FIELD.CUBE_RECEIVED_MESSAGE.getFIELD());
        registerReceiver(serverMessageReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    public void startService() {
        // Запуск сервісу
        Intent serviceIntent = new Intent(this, IOService.class);
        serviceIntent.putExtra(FIELD.CUBE_ID_SENDER.getFIELD(), userId);
        serviceIntent.putExtra(FIELD.CUBE_IP_TO_SERVER.getFIELD(), "192.168.1.237");
        serviceIntent.putExtra(FIELD.CUBE_PORT_TO_SERVER.getFIELD(), "8080");
        startService(serviceIntent);
    }

    private void notifyIdReciverChanged(String receiverId) {
        Intent intent = new Intent(FIELD.CUBE_ID_RECIVER.getFIELD());
        intent.putExtra(FIELD.RECEIVER_ID.getFIELD(), receiverId);
        sendBroadcast(intent);
    }


    private void notifyIdSenderChanged(String senderId) {
        Intent intent = new Intent(FIELD.CUBE_ID_SENDER.getFIELD());
        intent.putExtra(FIELD.SENDER_ID.getFIELD(), senderId);
        sendBroadcast(intent);
    }

    private void sendMessageToService(String message) {
        Intent intent = new Intent(FIELD.CUBE_SEND_TO_SERVER.getFIELD());
        intent.putExtra(FIELD.MESSAGE.getFIELD(), message);
        sendBroadcast(intent);  // Надсилає повідомлення сервісу
    }

    private void notifyIpChanged(String ip) {
        Intent intent = new Intent(FIELD.CUBE_IP_TO_SERVER.getFIELD());
        intent.putExtra(FIELD.IP.getFIELD(), ip);
        sendBroadcast(intent);
    }

    private void notifyPortChanged(String port) {
        Intent intent = new Intent(FIELD.CUBE_PORT_TO_SERVER.getFIELD());
        intent.putExtra(FIELD.PORT.getFIELD(), port);
        sendBroadcast(intent);
    }

    /**
     * Обробка натискання на елемент списку користувачів.
     *
     * @param adapterView Віджет-список.
     * @param view        Об'єкт View.
     * @param i           Позиція вибраного елемента.
     * @param l           Ідентифікатор вибраного елемента.
     * @user Основний користувач, який був вибраний.
     * <p>
     * Пояснення до коду: пишу для себе, але може бути корисно для вас також.
     * @onItemClick Відповідає за обробку натискання на контакти, які у вас є.
     * <p>
     * Під час натискання перевіряються 4 параметри:
     * @publicKey Перевірка існування publicKey — це RSA-ключ, за допомогою якого
     * шифруються спеціальні повідомлення, що будуть відправлятися вам користувачем,
     * з яким ви спілкуєтеся.
     * @senderKey Перевірка існування senderKey — це AES-ключ, за допомогою якого
     * шифруються повідомлення, що буде відправляти вам користувач, з яким ви спілкуєтеся.
     * @receiverPublicKey Перевірка існування receiverPublicKey користувача, якому ви пишете —
     * це RSA-ключ, за допомогою якого шифруються спеціальні повідомлення для відправки
     * до користувача, з яким ви спілкуєтеся.
     * @receiverKey Перевірка існування receiverKey користувача, якому ви пишете —
     * це AES-ключ, за допомогою якого шифруються повідомлення, які ви будете відправляти
     * до користувача, з яким ви спілкуєтеся.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        user = userList.get(i);
        user.setSize(0);
        receiverId = user.getId();
        if (user.getPublicKey().isEmpty()) {
            //generate PublicKey
            try {
                KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA();
                keyGenerator.key();
                user.setPublicKey(keyGenerator.getPublicKey());
                user.setPrivateKey(keyGenerator.getPrivateKey());
            } catch (Exception e) {
                Log.e("MainActivity", "Error generating RSA keys: " + e.toString());
            }

            // Треба додати генерацію ключа AES
            String key = KeyGenerator.AES.generateKey(16);
            if (key == null || key.isEmpty()) {
                Log.e("MainActivity", "Failed to generate AES key");
                return;
            }
            user.setSenderKey(key);

            // відправка публічного ключа отримувачу
            sendHandshake(userId, receiverId, FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), user.getPublicKey());
            //Анулюймо користувача так як нам треба отримувати повідомлення якщо вони і будуть йти
            receiverId = null;
            //serverConnection.setReceiverId(receiverId);
        } else {
            try {
                if (user.getReceiverPublicKey() == null) {
                    // Якщо в нас ReceiverPublicKey відсутній то ми ще раз відправляємо свій ключ якщо по якимось причинам в нас не пройшов хеншейк.
                    // Сервер отримає хеншейк та якщо отримувач ще не відправив свій ключ то він збереже його
                    sendHandshake(userId, receiverId, FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), user.getPublicKey());
                    //Анулюймо користувача так як нам треба отримувати повідомлення якщо вони і будуть йти
                    receiverId = null;
                    //serverConnection.setReceiverId(receiverId);
                } else {
                    if (receiverId != null && !receiverId.isEmpty()) {
                        if (!user.getReceiverKey().isEmpty()) {
                            startChat(binding.getRoot().getRootView(), user);
                            new Operation(this).openSaveMessage(receiverId, saveMessage);
                            user.setMessageSize("");
                            userAdapter.notifyDataSetChanged();
                            notifyIdReciverChanged(receiverId);
                        }
                    } else {
                        try {
                            PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(user.getReceiverPublicKey());
                            String AES = Encryption.RSA.encrypt(user.getSenderKey(), receiverPublicKey);
                            if (AES != null && !AES.isEmpty()) {
                                sendHandshake(userId, receiverId, FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES);
                            } else {
                                Log.e("MainActivity", "Failed to encrypt AES key");
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Error during AES key encryption: " + e.toString());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", e.toString());
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        user = userList.get(i);
        user.setSize(0);
        receiverId = user.getId();
        KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA();
        keyGenerator.key();
        user.setPublicKey(keyGenerator.getPublicKey());
        user.setPrivateKey(keyGenerator.getPrivateKey());
        String key = KeyGenerator.AES.generateKey(16);
        user.setSenderKey(key);
        sendHandshake(userId, receiverId, FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), user.getPublicKey());
        receiverId = null;
        return false;
    }

    /**
     * Отримання повідомлення та відправка у клас для обробки операції.
     *
     * @param message Текст повідомлення.
     */
    public void onReceived(String message) {
        new Operation(this).onReceived(message);
    }

    /**
     * Зберігає отримане повідомлення.
     *
     * @param message дані повідомлення, яке зберігається.
     */
    public void saveMessage(String message) {
        try {
            Envelope envelope = new Envelope(new JSONObject(message));
            runOnUiThread(() -> {
                numMessage = new Operation(this).saveMessage(envelope, saveMessage, numMessage, userList);
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Save Message Error: " + e);
        }
    }

    public void setNotification(String clas, String log) {
        runOnUiThread(() -> {
            logs.add(new Logger(clas, log));
            logAdapter.notifyItemInserted(logs.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.log.smoothScrollToPosition(logs.size() - 1); // Прокрутити до нового елемента
            Log.e(clas, log);
        });
    }


    /**
     * Передача зображення акаунта
     * Під час передачі ми передаємо два зображення:
     * 1. Маленьке — для контактів
     * 2. Велике — для аватара
     * Під час передачі передається посилання на файл (він шифрується та відправляється у зашифрованому вигляді).
     * Передається ID, яке складається з:
     * - Відправника
     * - Назви поточного файлу
     * - Користувача, якому належить файл
     * - Хеш-суми
     * Також передається ключ для шифрування, де зазначається, хто запитує ці зображення.
     */

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void giveAvatar(String sender) {
        File file = new File(this.getExternalFilesDir(null), "imageProfile");
        FileDetect fileDetect = new FileDetect();
        String avatarImage = file + "/" + imageOrgName;
        String accountImage = file + "/" + imageName;
        for (UserData user : userList) {
            if (user.getId().equals(sender)) {
                String key = user.getSenderKey();
                uploadFile(new File(avatarImage), sender + ":imageOrgName:" + fileDetect.getFileHash(avatarImage, "SHA-256"), key);
                uploadFile(new File(accountImage), sender + ":accountImage:" + fileDetect.getFileHash(accountImage, "SHA-256"), key);
                break;
            }
        }
    }


    private void addAvatar(Envelope envelope,String avatar_name){
        try {
            //Thread.sleep(3000);
            for (int position = 0; position < userList.size(); position++) {
                if (userList.get(position).getId().equals(envelope.getSenderId())) {
                    addPositionID(envelope.getMessageId(),envelope.getSenderId()+ ":"+avatar_name);
                    Log.e("MainActivity", userList.get(position).getId() +" Key " + userList.get(position).getReceiverKey());
                    String message = Encryption.AES.decrypt(envelope.getMessage(), userList.get(position).getReceiverKey());
                    String fileUrl = Encryption.AES.decrypt(envelope.getFileUrl(), userList.get(position).getReceiverKey());
                    String fileHash = Encryption.AES.decrypt(envelope.getFileHash(), userList.get(position).getReceiverKey());
                    File externalDir = new File(getExternalFilesDir(null), "imageProfile");
                    Log.e("MainActivity", "message "+message+" messageID " + envelope.getMessageId()+" fileUrl "+fileUrl+" fileHash "+fileHash);
                    URL url = new URL(fileUrl);
                    new Downloader(this, url , externalDir, position, envelope.getMessageId());
                    break;
                }
            }
        }catch (Exception e){
            Log.e("MainActivity"," Error "+ e);

        }
    }
    /**
     * Отримання зображення контакту який в нас збережений
     */
    @Override
    public void getAvatar(Envelope envelope) {
            addAvatar(envelope,"avatar");
    }


    /**
     * Отримання зображення контакту який в нас збережений
     */
    @Override
    public void getAvatarORG(Envelope envelope) {
        addAvatar(envelope,"avatar_org");
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void uploadFile(File file, String sender, String key) {

        String serverUrl = "http://192.168.1.237:8020/api/files/upload";
        // Виконуємо шифрування у фоновому потоці
        new Handler().postDelayed(() -> {
            try {
                FileEncryption fileEncryption = new FileEncryption(this, sender, serverUrl);
                SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
                String encryptedFile = fileEncryption.getEncFile(file, secretKey);
                addPositionID(sender, encryptedFile);
                Log.e("MainActivity", " encryptedFileName " + encryptedFile);
                fileEncryption.fileEncryption();
            } catch (Exception e) {

            }
        }, 1);
//        new Thread(() -> {
//            try {
//                FileEncryption fileEncryption = new FileEncryption(this, sender, serverUrl);
//                SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
//                String encryptedFile = fileEncryption.getEncFile(file, secretKey);
//                addPositionID(sender, encryptedFile);
//                Log.e("MainActivity", " encryptedFileName " + encryptedFile);
//                fileEncryption.fileEncryption();
//            } catch (Exception e) {
//
//            }
//        }).start();
    }

    // Метод для додавання значення
    public void addPositionID(String positionID, String fileName) {
        avatar_map.put(positionID, fileName);
    }

    // Метод для отримання та автоматичного видалення значення
    public String getAvatarInfoAndRemove(String positionID) {
        return avatar_map.remove(positionID);
    }

    /**
     * Додає повідомлення до broadcast для передачі його в ChatActivity.
     *
     * @param message Текст повідомлення.
     */
    @Override
    public void addMessage(String message) {
        Intent intent = new Intent(FIELD.DATA_TO_CHAT.getFIELD());
        intent.putExtra(FIELD.DATE_FROM_USERS_ACTIVITY.getFIELD(), message);
        sendBroadcast(intent);
    }

    @Override
    public void addAESKey(String sender, String receivedMessage) {
        try {
            JSONObject jsonObject = new JSONObject(receivedMessage);
            String receiverKey = jsonObject.getString(FIELD.AES_KEY.getFIELD());
            for (UserData user : userList) {
                if (user.getId().equals(sender)) {
                    PrivateKey privateKey = new KeyGenerator.RSA().decodePrivateKey(user.getPrivateKey());
                    String AES = Encryption.RSA.decrypt(receiverKey, privateKey);
                    user.setReceiverKey(AES);
                    contactManager.updateContact(user, secretKey);
                    break;
                }
            }
            userAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка під час парсинга ключа [" + sender + "]: " + e);
        }
    }

    /**
     * Отримання та відправу Handshake.
     *
     * @param envelope дані повідомлення, яке зберігається.
     */
    @Override
    public void addHandshake(Envelope envelope) {
        try {
            String sender = envelope.getSenderId();
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            JSONObject jsonObject = new JSONObject(receivedMessage);
            String publicKey = jsonObject.getString(FIELD.PUBLIC_KEY.getFIELD());
            if (!publicKey.isEmpty() || !publicKey.equals(publicKey)) {
                updateReceiverPublicKey(sender, publicKey);
                PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(user.getReceiverPublicKey());
                String AES = Encryption.RSA.encrypt(user.getSenderKey(), receiverPublicKey);
                sendHandshake(userId, sender, FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES);
            }
        } catch (JSONException e) {
            Log.e("MainActivity", "Помилка під час парсінгу JSON: " + e);

        } catch (Exception e) {
            Log.e("MainActivity", "Помилка під час шифрування ключа AES: " + e);
        }
    }

    /**
     * Надсилає хендшейк (handshake) повідомлення з даними користувача.
     * Використовується для встановлення безпечного з'єднання.
     *
     * @param userId     Ідентифікатор користувача.
     * @param receiverId Ідентифікатор отримувача.
     * @param operation  Операція (наприклад, шифрування).
     * @param nameKey    Назва ключа.
     * @param key        Значення ключа.
     */
    public void sendHandshake(String userId, String receiverId, String operation, String nameKey, String key) {
        String keyMessage = "{\"" + nameKey + "\": \"" + key + "\" }";
        sendMessageToService(new Envelope(userId, receiverId, operation, keyMessage, "").toJson().toString());
    }

    /**
     * Зберігає отримане Handshake.
     *
     * @param sender    відправник.
     * @param publicKey ключ відправника.
     */
    private void updateReceiverPublicKey(String sender, String publicKey) {
        for (UserData user : userList) {
            if (user.getId().equals(sender)) {
                user.setReceiverPublicKey(publicKey);
                contactManager.updateContact(user, secretKey);
                break;
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateAdapter() {
        userAdapter.notifyDataSetChanged();
    }


    /**
     * Лаунчер для сканування QR-коду для додавання аккаунту користувача.
     * Використовує ActivityResultLauncher для сканування та обробки результату.
     * Якщо вміст QR-коду не порожній, додається новий аккаунту користувача.
     */
    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> qrCodeAddAccount = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            manager.writeAccount(result.getContents());
        }
    });

    /**
     * Лаунчер для сканування QR-коду для додавання контакту.
     * Використовує ActivityResultLauncher для сканування та обробки результату.
     * Якщо вміст QR-коду не порожній, додається новий контакт.
     */
    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> qrCodeAddContact = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            manager.createContact(result.getContents());
        }
    });

    @Override
    public void scannerQrAccount() {
        //manager.clearMessagesTable();
        new QR(qrCodeAddAccount);
    }

    // Константа для запиту вибору зображення
    private static final int PICK_IMAGE_REQUEST = 1;

    ImageExplorer imageExplorer;

    @Override
    public void imageNavigation() {
        // Ініціалізація об'єкта ImageExplorer для відображення діалогового вікна для вибору зображення
        imageExplorer = new ImageExplorer(this, "");
    }

    /**
     * Відкриває галерею для вибору зображення.
     * Цей метод створює намір (Intent) для відкриття галереї та вибору зображення.
     */
    @Override
    public void openImagePicker() {
        // Створення наміру для вибору зображення з галереї
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Запуск активності для вибору зображення (запит з кодом PICK_IMAGE_REQUEST)
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void setImageAccount(String imageOrgName, String imageName) {
        runOnUiThread(() -> {
            String jsonData = "{" +
                    "\"userId\":\"" + this.userId + "\"," +
                    "\"name\":\"" + this.name + "\"," +
                    "\"lastName\":\"" + this.lastName + "\"," +
                    "\"password\":\"" + this.password + "\"," +
                    "\"imageOrgName\":\"" + imageOrgName + "\"," +
                    "\"imageName\":\"" + imageName + "\"" +
                    "}";
            Log.e("DatabaseHelper", "setImageAccount. ");

            manager.writeAccount(jsonData);
            navigationManager.setAvatarImage(imageOrgName);
            navigationManager.setAccountImage(imageName);
        });
    }

    /**
     * Обробник результату вибору зображення.
     * Після вибору зображення з галереї цей метод отримує обраний файл і відправляє його в ImageExplorer.
     *
     * @param requestCode код запиту, який визначає, з якої активності повернувся результат
     * @param resultCode  код результату, який вказує, чи успішно завершилась операція
     * @param data        Дані, отримані з вибраної активності, які містять URI вибраного зображення
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Перевіряємо, чи це результат вибору зображення
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            // Отримуємо URI вибраного зображення
            Uri imageUri = data.getData();
            try {
                // Отримуємо Bitmap з обраного URI
                Bitmap selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                // Встановлюємо обране зображення в ImageExplorer
                imageExplorer.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                // Обробка помилки, якщо не вдалося отримати зображення
                e.printStackTrace();
            }
        }
    }

    /**
     * Викликає сканер QR-коду для додавання контакту.
     */

    @Override
    public void scannerQrContact() {
        deleteDatabase("cube.db");
        finish();
        // new QR(qrCodeAddContact);
    }

    @Override
    public void onClick(View view) {
        if (view == binding.setting) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (view == binding.fab) {
            new ContactCreator(this).showCreator();
        }
    }


    /**
     * Зберігає контакт, отриманий ззовні, до локальної системи.
     *
     * @param contact контакт у вигляді JSON-рядка.
     */

    @Override
    public void saveContact(String contact) {
        if (contact != null) {
            manager.createContact(contact);
        }
    }


    @Override
    public void onImageClickContact(int position) {
        user = userList.get(position);
        sendHandshake(userId, user.getId(), FIELD.GET_AVATAR.getFIELD(), "get_avatar", user.getPublicKey());
    }

    @Override
    public void setProgressShow(String positionId, int progress, String info) {
        Log.e("MainActivity", "[" + positionId + "]: " + progress + " " + info);
    }

    @Override
    public void endProgress(String positionId, String info) {
        try {
            String path = getAvatarInfoAndRemove(positionId);
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String serverUrl = "http://192.168.1.237:8020/api/files/download/" + fileName;
            String[] position = positionId.split(":");
            for (UserData user : userList) {
                if (user.getId().equals(position[0])) {
                    if (position[1].equals("imageOrgName")) {
                        String rMessage = Encryption.AES.encrypt("avatar_org", user.getSenderKey());
                        String Url = Encryption.AES.encrypt(serverUrl, user.getSenderKey());
                        String Has = Encryption.AES.encrypt(position[2], user.getSenderKey());

                        sendMessageToService(new Envelope(userId, position[0], FIELD.AVATAR_ORG.getFIELD(),
                                rMessage,
                                Url, Has, UUID.randomUUID().toString()).toJson().toString());
                    } else {
                        String rMessage = Encryption.AES.encrypt("avatar", user.getSenderKey());
                        String Url = Encryption.AES.encrypt(serverUrl,user.getSenderKey());
                        String Has = Encryption.AES.encrypt(position[2], user.getSenderKey());

                        sendMessageToService(new Envelope(userId, position[0], FIELD.AVATAR.getFIELD(),
                                rMessage,
                                Url, Has, UUID.randomUUID().toString()).toJson().toString());                    }
                    break;
                }
            }
            Log.e("MainActivity", "[" + positionId + " fileName " + fileName + " ]: " + info);
        }catch (Exception e){

        }

    }

    @Override
    public void addFile(String messageId, String url, String encFile, String has) {

    }

    @Override
    public void updateItem(int position, String positionId, String url, String has) {
        String avatar = getAvatarInfoAndRemove(positionId);
        String[] positionName = avatar.split(":");
        Log.e("MainActivity", "user "+positionName[0]+" avatar_name "+positionName[1]+" [" + positionId + " fileName " + url + " ]: " + has);

        for (UserData user : userList) {
            if (user.getId().equals(positionName[0])) {
                if(positionName[1].equals("avatar_org")){
                    user.setAvatarImageUrl(url);
                }
                if(positionName[1].equals("avatar")){
                    user.setAccountImageUrl(url);
                }
                contactManager.updateContact(user, secretKey);
                break;
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    /**Метод для отримання ключа для разщифрування файшлу
     * в нашому випадку в нас тут слугу  positionId Id-Повідомлення яке прийшло та за яким збережений Id контакту
     * Звертаємося до avatar_map та оримуємо ID контакту для отримання ключа*/
    @Override
    public String getKey(String positionId) {
        String body_map=avatar_map.get(positionId);
        String[] position = body_map.split(":");
        for (UserData user : userList) {
            if (user.getId().equals(position[0])) {
                return user.getReceiverKey();
            }}
        return "";
    }
}