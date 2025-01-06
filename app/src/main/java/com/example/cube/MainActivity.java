package com.example.cube;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.example.cube.chat.ChatActivity;
import com.example.cube.contact.ContactCreator;
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

import com.example.qrcode.QR;
import com.example.web_socket_service.socket.Envelope;
import com.example.web_socket_service.socket.IOService;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Основний клас активності MainActivity, що відповідає за обробку даних користувача, підключення до сервера та взаємодію з контактами.
 * Реалізує кілька інтерфейсів для обробки кліків та отримання повідомлень від сервера.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener,
        ContactCreator.CreatorOps, Manager.AccountOps, Operation.Operable, NavigationManager.Navigation, AdapterView.OnItemLongClickListener {

    private ActivityMainBinding binding;
    private TextView user_name;
    private TextView user_id;
    private String userId;           // ID користувача
    private String receiverId = null;       // ID отримувача
    private String name;             // Ім'я користувача
    private String lastName;         // Прізвище користувача
    private String password;         // Пароль
    private UserData user;           // Об'єкт користувача
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    ContactManager contactManager;
    Manager manager;
    private final List<UserData> userList = new ArrayList<>();  // Список користувачів
    private Map<String, UserData> contacts = new HashMap<>();  // Контакти користувачів

    private List<Logger> logs;
    private LogAdapter logAdapter;

    private SecretKey secretKey;  // AES-ключ

    private final HashMap<Integer, Envelope> saveMessage = new HashMap<>();  // Збережені повідомлення
    private int numMessage = 0;  // Лічильник повідомлень

    private UserAdapter userAdapter;                // Адаптер для відображення користувачів
    private File externalDir;  // Зовнішній каталог


    @Override
    public void setAccount(String userId, String name, String lastName, String password) {
        String accountName = name + " " + lastName;
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        binding.id.setText(userId);
        binding.name.setText(accountName);
        user_name.setText(accountName);
        user_id.setText(userId);

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
    private BroadcastReceiver serverMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals("CUBE_RECEIVED_MESSAGE")) {
                String message = intent.getStringExtra(FIELD.MESSAGE.getFIELD());
                if (message != null) {
                    Log.e("MainActivity", "message: " + message);

                    onReceived(message);
                }

                String save_message = intent.getStringExtra(FIELD.SAVE_MESSAGE.getFIELD());
                if (save_message != null) {
                    Log.e("MainActivity", "save_message: " + save_message);

                    saveMessage(save_message);
                }
            }
        }
    };

    private DrawerLayout drawerLayout;

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, null,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        user_name = findViewById(R.id.user_name);
        user_id = findViewById(R.id.user_id);
        Button accountButton = findViewById(R.id.nav_account);
        Button settingsButton = findViewById(R.id.nav_settings);
        Button logoutButton = findViewById(R.id.nav_logout);
        ImageButton add_accounte = findViewById(R.id.add_account);

        // Використання NavigationManager для обробки меню
        new NavigationManager(this, drawerLayout, add_accounte, accountButton, settingsButton, logoutButton);


        password = "1234567890123456";  // Пароль
        byte[] keyBytes = password.getBytes();  // Генерація байт ключа
        secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        externalDir = new File(getExternalFilesDir(null), "cube");
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
     * @user Основний користувач який був вибраний.
     * Пояснення до коду: пишу для себе але може бути корисно для вас також
     * @onItemClick відповідає за натискання на контактів які в вас є
     * Під час натискання перевіряється 4 параметра а саме:
     * @publicKey існування publicKey - це RSA ключ за допомогою якого шифруються спеціальні повідомлення які будуть відправляти вам користувач з яким ви будете спілкуватися.
     * @senderKey існування senderKey - це ASE ключ за допомогою якого шифруються повідомлення які буде відправляти вам користувач з яким ви будете спілкуватися.
     * @receiverPublicKey існування receiverPublicKey користувача якому ви пишете - це RSA ключ за допомогою якого шифруються спеціальні повідомлення для відправки до користувача з яким ви будете спілкуватися.
     * @receiverKey існування receiverKey користувача якому ви пишете - це ASE ключ за допомогою якого шифруються повідомлення які ви будете відправляти до користувача з яким ви будете спілкуватися.
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

    public void setLogs(String clas, String log) {
        runOnUiThread(() -> {
            logs.add(new Logger(clas, log));
            logAdapter.notifyItemInserted(logs.size() - 1); // Повідомити, що новий елемент було вставлено
            binding.log.smoothScrollToPosition(logs.size() - 1); // Прокрутити до нового елемента
            Log.e(clas, log);
        });
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
            manager.writeAccount(externalDir, result.getContents());
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


}