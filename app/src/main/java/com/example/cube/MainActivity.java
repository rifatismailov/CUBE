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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import com.example.cube.chat.ChatActivity;
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
import com.example.cube.socket.Envelope;
import com.example.cube.socket.Listener;
import com.example.qrcode.QR;
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
        View.OnClickListener, Listener.DataListener,
        ContactCreator.CreatorOps, AccountManager.AccountOps, Operation.Operable, NavigationManager.Navigation, AdapterView.OnItemLongClickListener {

    private ActivityMainBinding binding;
    private TextView user_name;
    private TextView user_id;
    private String userId;           // ID користувача
    private String receiverId;       // ID отримувача
    private String name;             // Ім'я користувача
    private String lastName;         // Прізвище користувача
    private String password;         // Пароль
    private UserData user;           // Об'єкт користувача
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    ContactManager manager;

    private final List<UserData> userList = new ArrayList<>();  // Список користувачів
    private Map<String, UserData> contacts = new HashMap<>();  // Контакти користувачів

    private List<Logger> logs;
    private LogAdapter logAdapter;

    private SecretKey secretKey;  // AES-ключ

    private final HashMap<Integer, Envelope> saveMessage = new HashMap<>();  // Збережені повідомлення
    private int numMessage = 0;  // Лічильник повідомлень

    private UserAdapter userAdapter;                // Адаптер для відображення користувачів
    private Listener listener;      // З'єднання з сервером
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

        if (listener == null)
            startConnect(userId);
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
        manager.setContacts(contacts, secretKey);
    }


    private void startConnect(String userId) {
        listener = new Listener(this, userId, "192.168.1.237", 8080);
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
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        manager = new ContactManager(db);
        logs = new ArrayList<>();
        logAdapter = new LogAdapter(this, logs);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);


        binding.log.setLayoutManager(layoutManager);
        binding.log.setAdapter(logAdapter);

        password = "1234567890123456";  // Пароль
        byte[] keyBytes = password.getBytes();  // Генерація байт ключа
        secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
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

        externalDir = new File(getExternalFilesDir(null), "cube");
        new AccountManager(this).readAccount(externalDir);

        binding.setting.setOnClickListener(this);
        binding.fab.setOnClickListener(this);
        if (userId != null)
            // Створення підключення до сервера
            startConnect(userId);
        else
            scannerQrAccount();

        initUserList();


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
     * Ініціалізація списку користувачів та його заповнення з файлу.
     */
    private void initUserList() {
        userList.clear();
        //ContactManager manager=new ContactManager(db);
        contacts =manager.getContacts(secretKey);
        for (Map.Entry<String, UserData> entry : contacts.entrySet()) {
            userList.add(entry.getValue());
        }
        userAdapter = new UserAdapter(this, R.layout.iteam_user, userList);
        binding.contentMain.userList.setAdapter(userAdapter);
        binding.contentMain.userList.setOnItemClickListener(this);
        binding.contentMain.userList.setOnItemLongClickListener(this);

    }

    @Override
    public synchronized String getReceiverId() {
        return receiverId;
    }

    /**
     * Отримує дані з активності чату та передає їх на сервер.
     *
     * @param data Отримані дані.
     */
    private void receivingData(String data) {
        if (data.equals("endUser")) {
            receiverId = null;
            //listener.setReceiverId(receiverId);
        } else {
            // якщо нема публічного ключа кому ми відправляємо то повідомлення не буде відправлено
            if (user != null) {
                if (!data.isEmpty()) {
                    if (user.getReceiverPublicKey() != null) {
                        listener.sendData(data);
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
        intent.putExtra(FIELD.STATUS_USER.getFIELD(), "online");
        intent.putExtra(FIELD.PUBLIC_KEY.getFIELD(), userData.getPublicKey());
        intent.putExtra(FIELD.PRIVATE_KEY.getFIELD(), userData.getPrivateKey());
        intent.putExtra(FIELD.RECEIVER_PUBLIC_KEY.getFIELD(), userData.getReceiverPublicKey());
        intent.putExtra(FIELD.SENDER_KEY.getFIELD(), userData.getSenderKey());
        intent.putExtra(FIELD.RECEIVER_KEY.getFIELD(), userData.getReceiverKey());
        startActivity(intent);

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
                Log.e("KeyGeneration", "Error generating RSA keys: " + e.toString());
            }

            // Треба додати генерацію ключа AES
            String key = KeyGenerator.AES.generateKey(16);
            if (key == null || key.isEmpty()) {
                Log.e("AESKeyGeneration", "Failed to generate AES key");
                return;
            }
            user.setSenderKey(key);

            // відправка публічного ключа отримувачу
            listener.sendHandshake(userId, receiverId, FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), user.getPublicKey());
            //Анулюймо користувача так як нам треба отримувати повідомлення якщо вони і будуть йти
            receiverId = null;
            //serverConnection.setReceiverId(receiverId);


        } else {
            try {
                if (user.getReceiverPublicKey() == null) {
                    // Якщо в нас ReceiverPublicKey відсутній то ми ще раз відправляємо свій ключ якщо по якимось причинам в нас не пройшов хеншейк.
                    // Сервер отримає хеншейк та якщо отримувач ще не відправив свій ключ то він збереже його
                    listener.sendHandshake(userId, receiverId, FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), user.getPublicKey());
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
                        }
                    } else {
                        try {
                            PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(user.getReceiverPublicKey());
                            String AES = Encryption.RSA.encrypt(user.getSenderKey(), receiverPublicKey);
                            if (AES != null && !AES.isEmpty()) {
                                listener.sendHandshake(userId, receiverId, FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES);
                            } else {
                                Log.e("AESKeyExchange", "Failed to encrypt AES key");
                            }
                        } catch (Exception e) {
                            Log.e("AESKeyExchange", "Error during AES key encryption: " + e.toString());
                        }

                    }
                }
            } catch (Exception e) {
                Log.e("ReceiverPublicKey", e.toString());
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        user = userList.get(i);
        user.setSize(0);
        receiverId = user.getId();
        //generate PublicKey
        KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA();
        keyGenerator.key();
        user.setPublicKey(keyGenerator.getPublicKey());
        user.setPrivateKey(keyGenerator.getPrivateKey());
        // Треба додати генерацію ключа AES
        String key = KeyGenerator.AES.generateKey(16);
        user.setSenderKey(key);
        // відправка публічного ключа отримувачу
        listener.sendHandshake(userId, receiverId, FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), user.getPublicKey());
        //Анулюймо користувача так як нам треба отримувати повідомлення якщо вони і будуть йти
        receiverId = null;
        //serverConnection.setReceiverId(receiverId);

        return false;
    }

    /**
     * Отримання повідомлення та відправка у клас для обробки операції.
     *
     * @param message Текст повідомлення.
     */
    @Override
    public void onReceived(String message) {
        new Operation(this).onReceived(message);
    }

    /**
     * Зберігає отримане повідомлення.
     *
     * @param envelope дані повідомлення, яке зберігається.
     */
    @Override
    public void saveMessage(Envelope envelope) {
        Log.e("Exchange", "Збереження повідомлення: " + envelope);
        // Оновлення інтерфейсу користувача на основі нових повідомлень
        runOnUiThread(() -> {
            numMessage = new Operation(this).saveMessage(envelope, saveMessage, numMessage, userList);
        });
    }

    @Override
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
        Log.e("Exchange", "Отримано receiverKey від [" + sender + "]: ");

        try {
            JSONObject jsonObject = new JSONObject(receivedMessage);
            String receiverKey = jsonObject.getString(FIELD.AES_KEY.getFIELD());
            Log.e("Exchange", "receiverKey : " + receiverKey);

            for (UserData user : userList) {
                if (user.getId().equals(sender)) {
                    PrivateKey privateKey = new KeyGenerator.RSA().decodePrivateKey(user.getPrivateKey());
                    String AES = Encryption.RSA.decrypt(receiverKey, privateKey);
                    Log.e("Exchange", "Отримано AES [" + sender + "]: " + AES);
                    user.setReceiverKey(AES);
                    manager.updateContact(user,secretKey);
                    break;
                }
            }
            userAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("Exchange", "Помилка під час парсинга ключа [" + sender + "]: " + e);

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
            //Перевіряємо якщо publicKey не пустий або якщо він змінився додаємо
            if (!publicKey.isEmpty() || !publicKey.equals(publicKey)) {
                Log.d("Exchange", "Отримано handshake від [" + sender + "]: " + publicKey);
                updateReceiverPublicKey(sender, publicKey);
                //Для відправки спочатку отримуємо публічний ключ отримувача для шифрування AES ключа
                PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(user.getReceiverPublicKey());
                // Шифруємо AES ключ за допомогою публічного ключа отримувача
                String AES = Encryption.RSA.encrypt(user.getSenderKey(), receiverPublicKey);
                listener.sendHandshake(userId, sender, FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES);
            }
        } catch (JSONException e) {
            Log.e("Exchange", "Помилка під час парсінгу JSON: " + e);

        } catch (Exception e) {
            Log.e("Exchange", "Помилка під час шифрування ключа AES: " + e);
        }
    }

    /**
     * Зберігає отримане Handshake.
     *
     * @param sender    відправник.
     * @param publicKey ключ відправника.
     */
    private void updateReceiverPublicKey(String sender, String publicKey) {
        // Перевіряємо відправника з поточним юзерами які в нас є та додаємо ключ
        for (UserData user : userList) {
            if (user.getId().equals(sender)) {
                user.setReceiverPublicKey(publicKey);
                manager.updateContact(user,secretKey);
                Log.e("Exchange", "Оновлено публічний ключ для користувача: " + sender);
                break;
            }
        }
        // Оновлюємо мапу контактів
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
            new AccountManager(this).writeAccount(externalDir, result.getContents());
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
            // Додає новий контакт із QR-коду
            new AccountManager(this).createContact(result.getContents());
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
        new QR(qrCodeAddContact);
    }

    @Override
    public void onClick(View view) {
        // Визначаємо дії при натисканні на різні елементи інтерфейсу
        if (view == binding.setting) {
            // showPopupWindow = new ShowPopupWindow(this);
            /// showPopupWindow.showPopupWindow(view, "Це вспливаюче вікно зліва!");
            drawerLayout.openDrawer(GravityCompat.START);
            // Запуск сканера QR-коду для отримання налаштувань
            // new QR(qrCodeLauncher);
        } else if (view == binding.fab) {
            // Відкриваємо вікно для додавання нового клієнта
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
            new AccountManager(this).createContact(contact);
        }
    }


}