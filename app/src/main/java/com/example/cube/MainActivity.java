package com.example.cube;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cube.chat.ChatActivity;
import com.example.cube.chat.message.Message;
import com.example.cube.contact.ContactCreator;
import com.example.cube.contact.ContactInterface;
import com.example.cube.contact.ContactAdapter;
import com.example.cube.contact.ContactData;
import com.example.cube.contact.ContactSelector;
import com.example.cube.databinding.ActivityMainBinding;
import com.example.cube.db.ContactManager;
import com.example.cube.db.MessageMainManager;
import com.example.cube.db.MessageManager;
import com.example.cube.encryption.Encryption;
import com.example.cube.encryption.KeyGenerator;
import com.example.cube.notification.NotificationAdapter;
import com.example.cube.notification.NotificationLogger;
import com.example.cube.navigation.NavigationManager;
import com.example.cube.permission.Permission;
import com.example.cube.control.FIELD;
import com.example.cube.sound.SoundPlayer;
import com.example.database_cube.DatabaseHelper;
import com.example.folder.FileData;
import com.example.folder.download.Downloader;
import com.example.folder.file.FileOMG;
import com.example.folder.file.FilePathBuilder;
import com.example.folder.file.Folder;
import com.example.folder.upload.FileEncryption;
import com.example.qrcode.QR;
import com.example.setting.AccountDialog;
import com.example.setting.SettingDialog;
import com.example.setting.UrlBuilder;
import com.example.setting.UserSetting;
import com.example.setting.greate_image.ImageExplorer;
import com.example.web_socket_service.socket.Envelope;
import com.example.web_socket_service.socket.IOService;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        ContactInterface, FileOMG, Folder, Downloader.DownloaderHandler, SettingDialog.IClassSetting {

    private ActivityMainBinding binding;
    private TextView user_name;
    private TextView user_id;
    private ContactManager contactManager;
    private Manager manager;
    private MessageManager messageManager;
    private SecretKey secretKey;  // AES-ключ
    private DrawerLayout drawerLayout;
    private NavigationManager navigationManager;
    private List<NotificationLogger> notificationLoggers;
    private NotificationAdapter notificationAdapter;
    private MessageMainManager messageMainManager;
    private Map<String, ContactData> contacts = new HashMap<>();  // Контакти користувачів
    private ContactAdapter contactAdapter;                // Адаптер для відображення користувачів
    private Operation operation;
    private final ContactSelector contactSelector = new ContactSelector();
    private final List<ContactData> contactDataList = new ArrayList<>();  // Список користувачів
    private final HashMap<String, Envelope> saveMessage = new HashMap<>();  // Збережені повідомлення
    private final HashMap<String, String> avatar_map = new HashMap<>();
    private SoundPlayer soundPlayer;

    @Override
    public void setAccount(String userId, String name, String lastName, String password, String avatarImageUrl, String accountImageUrl) {
        String accountName = name + " " + lastName;
        binding.id.setText(userId);
        binding.name.setText(accountName);
        user_name.setText(accountName);
        user_id.setText(userId);
        navigationManager.setAvatarImage(avatarImageUrl);
        navigationManager.setAccountImage(accountImageUrl);
    }


    /**
     * Додає новий контакт, отриманий через QR-код або з поля вводу контакту.
     *
     * @param id_contact дані контакту у вигляді рядка JSON.
     */

    @Override
    public void setContact(String id_contact, String name_contact, String lastName_contact) {
        Pair<Boolean, String> result = contactManager.getContactById(id_contact);
        if (result.first) {
            Toast.makeText(this, "Контакт вже існує: " + result.second, Toast.LENGTH_SHORT).show();
        } else {
            // Додаємо новий контакт до списку користувачів
            ContactData newUser = new ContactData(id_contact, name_contact, lastName_contact, "");
            contactDataList.add(newUser);
            contacts.put(newUser.getId(), newUser);   // Оновлюємо мапу контактів
            contactManager.setContacts(contacts);// Зберігаємо контакти у базу даних
            request(getContactToJsonArray());// оновлюємо ключ зєднання
            Toast.makeText(this, "Контакт додано", Toast.LENGTH_SHORT).show();
            initUserList();
        }
    }


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
        File externalDir = new File(getExternalFilesDir(null), "key");

        // Створюємо директорію, якщо її не існує
        if (!externalDir.exists() && !externalDir.mkdirs()) {
            Log.e("MainActivity", "Не вдалося створити директорію");
        }

        String filePath = externalDir + "/SecretKey.key";
        File keyFile = new File(filePath);
        //FileData.write(KeyGenerator.AES.generateKey(16), filePath);
        if (!keyFile.exists()) {
            // Генеруємо новий ключ, якщо файлу немає
            String key = KeyGenerator.AES.generateKey(16);
            FileData.write(key, filePath);
            byte[] keyBytes = KeyGenerator.AES.hexToBytes(key);
            secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
        } else {
            // Читаємо існуючий ключ
            String key = FileData.read(filePath);
            byte[] keyBytes = KeyGenerator.AES.hexToBytes(key);
            secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
        }
        soundPlayer = new SoundPlayer();
        contactSelector.setContact("");
        //обнуляємо контакт для відображення кількості повідомлень у списку не унеможливлення передачі до чат активності так як він не запушений
        /*З початку Android 13 необхідно запитувати у користувача дозвіл на відображення нотифікацій. Це робиться так:*/
        setContentView(binding.getRoot());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }

        notificationLoggers = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, notificationLoggers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.log.setLayoutManager(layoutManager);
        binding.log.setAdapter(notificationAdapter);
        drawerLayout = findViewById(R.id.drawer_layout);
        user_name = findViewById(R.id.user_name);
        user_id = findViewById(R.id.user_id);

        // Використання NavigationManager для обробки меню
        navigationManager = new NavigationManager(this);


//        byte[] keyBytes = "1234567890123456".getBytes();  // Генерація байт ключа
//        secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        manager = new Manager(this, db, secretKey);
        messageManager = new MessageManager(db);
        messageMainManager = new MessageMainManager(db);
        //messageMainManager.deleteAllMessages();

        operation = new Operation(this, messageMainManager);
        manager.readAccount();
        contactManager = new ContactManager(db, secretKey);
        registerChatActivityReceiver();
        registerIOServiceReceiver();
        initUserList();
        startService();
        binding.setting.setOnClickListener(this);
        binding.fab.setOnClickListener(this);
        new Handler().postDelayed(() -> {
            if (manager.userSetting().getId() == null)
                scannerQrAccount();
        }, 100);
        //deleteAll();
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
     * Метод відображення останнього повідомлення у списку контактів
     *
     * @param contactData контакт в якому буде відображення останнього повідомлення
     */
    private void openLastMessage(ContactData contactData) {
        try {
            Message lastMessage = messageManager.getLastMessageByReceiverId(contactData.getId());
            if (lastMessage != null) {
                if (lastMessage.getTypeFile() != null) {
                    contactData.setMessageType(lastMessage.getTypeFile());
                    contactData.setMessage(lastMessage.getFileName());
                } else {
                    contactData.setMessageType(FIELD.MESSAGE.getFIELD());
                    contactData.setMessage(lastMessage.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "помилка під час обробки повідомлення " + e);
        }

    }

    /**
     * Ініціалізація списку контактів.
     */
    private void initUserList() {
        contactDataList.clear();
        contacts = contactManager.getContacts();
        for (Map.Entry<String, ContactData> entry : contacts.entrySet()) {
            ContactData contactData = entry.getValue();

            int messageCount = messageMainManager.getMessageCountBySenderAndOperation(contactData.getId(), FIELD.MESSAGE.getFIELD()) +
                    messageMainManager.getMessageCountBySenderAndOperation(contactData.getId(), FIELD.FILE.getFIELD());
            if (messageCount == 0) {
                contactData.setMessageSize("");  // обнуляємо messageSize
                openLastMessage(contactData);
            } else {
                contactData.setMessageSize("" + messageCount);
            }
            contactDataList.add(contactData);
        }
        contactAdapter = new ContactAdapter(this, R.layout.iteam_user, contactDataList);
        binding.contentMain.userList.setAdapter(contactAdapter);
        binding.contentMain.userList.setOnItemClickListener(this);
        binding.contentMain.userList.setOnItemLongClickListener(this);
    }

    /**
     * Отримує дані з активності чату та передає їх на сервер.
     *
     * @param data Отримані дані.
     */
    private void receivingData(String data) {
        if ("endUser".equals(data)) {
            contactSelector.setContact("");
        } else {
            if (contactSelector.getContactData() != null) {
                if (!data.isEmpty()) {
                    if (contactSelector.getContactData().getReceiverPublicKey() != null) {
                        setMessage(data);
                    }
                }
            }
        }
    }

    /**
     * Метод передає повідомлення у
     * метод sendMessageToService та обробляє повідомлення для
     * видалення з бази даних тимчасово зберігання
     *
     * @param message повідомлення яке передається
     */
    @Override
    public void setMessage(String message) {
        try {
            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            // Отримуємо значення messageStatus, перевіряємо на null і обрізаємо пробіли
            String status = envelope.getMessageStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "unknown";  // Встановлюємо значення за замовчуванням
            } else {
                status = status.trim();
            }
            switch (status) {
                case "delivered_to_user":
                case "update_to_message":
                    sendMessageToService(message);
                    messageMainManager.deleteMessageById(envelope.getMessageId());
                    break;
                default:
                    sendMessageToService(message);
                    break;
            }
        } catch (Exception e) {
            Log.e("MainActivity", "помилка під час обробки повідомлення " + e);
        }
    }

    /**
     * BroadcastReceiver для отримання даних з ChatActivity через broadcast.
     */
    private final BroadcastReceiver chatActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String dataFromChat = intent.getStringExtra(FIELD.DATA_FROM_CHAT.getFIELD());
            String sleep = intent.getStringExtra("sleep");
            String awake = intent.getStringExtra("awake");
            if (dataFromChat != null) {
                receivingData(dataFromChat);
            }

            if (sleep != null) {
                for (int i = 0; i < contactDataList.size(); i++) {
                    ContactData contactData = contactDataList.get(i);
                    if (sleep.equals(contactData.getId())) {
                        openLastMessage(contactData);
                    }
                }
                contactAdapter.notifyDataSetChanged(); // Оновлюємо лише один елемент
                contactSelector.setContact("");
                notifyIdReciverChanged("");
            }

            if (awake != null) {
                notifyIdReciverChanged(awake);
                contactSelector.setContact(awake);
                openSaveMessage();
            }
        }
    };

    /**
     * Метод обробки отриманих повідомлень
     *
     * @param message повідомлення яке було отримано
     *                а також виконує функцію звукового сповіщення під час збереження повідомлення
     */
    private void byMessage(String message) {
        try {

            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            if (contactSelector.getContact().equals(envelope.getSenderId())) {
                openSaveMessage();
                onReceived(message);
            } else {
                saveMessage(message);
                notificationMessage(envelope);
                // Перевіряємо повідомлення на належність до якого воно відноситься
                // сповіщаємо якщо це message або file
                Set<String> allowedStatuses = new HashSet<>(Arrays.asList("message", "file"));
                if (allowedStatuses.contains(envelope.getOperation())) {
                    soundPlayer.playNotificationSound(this);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка отримання повідомлення: " + e);
        }
    }

    /**
     * Метод перевірки існування контакту
     */
    private boolean checkContact(Envelope envelope) {
        boolean checkContact = false;

        Map<String, ContactData> contacts = contactManager.getContacts();
        for (Map.Entry<String, ContactData> entry : contacts.entrySet()) {
            if (envelope.getSenderId().equals(entry.getValue().getId())) {
                checkContact = true;
                break;
            }
        }
        return checkContact;
    }


    /**
     * Метод збереження повідомлення
     */
    private void bySaveMessage(String save_message) {
        try {
            Envelope envelope = new Envelope(new JSONObject(save_message));
            if (checkContact(envelope)) {
                saveMessage(save_message);
                notificationMessage(envelope);
                operation.setMessageStatus(envelope);
            }
//            else {
//              //  operation.setMessageStatus(envelope);
//            }
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка збереження повідомлення: " + e);
        }
    }

    private void byNotification(String notification) {
        try {
            String[] notificationAll = notification.split(":");
            setNotification(notificationAll[0], "");
            // navigationManager.setNotification(notificationAll[0]);
        } catch (Exception e) {
            Log.e("MainActivity", "помилка під час отримання інформації про статус з web socket");
        }
    }

    /**
     * BroadcastReceiver для отримання даних з IOService через broadcast.
     */
    private final BroadcastReceiver ioServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(FIELD.CUBE_RECEIVED_MESSAGE.getFIELD())) {
                // Отримання повідомлень
                String message = intent.getStringExtra(FIELD.MESSAGE.getFIELD());
                if (message != null) {
                    byMessage(message);
                }
                // отримання та збереження повідомлень
                String save_message = intent.getStringExtra(FIELD.SAVE_MESSAGE.getFIELD());
                if (save_message != null) {
                    bySaveMessage(save_message);
                }
                // отримання статусу контактів онлайн оффлайн нема на зв'язку
                String contact_status = intent.getStringExtra(FIELD.CONTACT_STATUS.getFIELD());
                if (contact_status != null) {
                    getStatus(contact_status);
                }
                // отримання інформації стосовно підключення
                String notification = intent.getStringExtra(FIELD.NOTIFICATION.getFIELD());
                if (notification != null) {
                    byNotification(notification);
                }
            }
        }
    };

    /**
     * Метод відображення останнього непрочитаного повідомлень у списку контактів
     *
     * @param envelope саме повідомлення
     */
    private void notificationMessage(Envelope envelope) {
        try {
            for (int i = 0; i < contactDataList.size(); i++) {
                ContactData contactData = contactDataList.get(i);
                if (contactData.getId().equals(envelope.getSenderId())) {
                    if (envelope.getOperation().equals(FIELD.FILE.getFIELD())) {
                        contactData.setMessageType(FIELD.FILE.getFIELD());
                        String filename = Encryption.AES.decrypt(envelope.getFileUrl(), contactData.getSenderKey());
                        contactData.setMessage(filename);
                    } else {
                        String rMessage = Encryption.AES.decrypt(envelope.getMessage(), contactData.getSenderKey());
                        contactData.setMessageType(FIELD.MESSAGE.getFIELD());
                        contactData.setMessage(rMessage);
                    }
                }
            }
            contactAdapter.notifyDataSetChanged(); // Оновлюємо лише один елемент
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка під час отримання повідомлення");

        }
    }

    /**
     * Метод відображення стану контакту
     *
     * @param contact_status стан контакту (Онлайн, оффлайн
     */
    private void getStatus(String contact_status) {
        try {
            JSONArray jsonArray = new JSONArray(contact_status);
            for (int i = 0; i < jsonArray.length(); i++) {
                String status = jsonArray.getString(i); // Отримуємо рядок із JSON-масиву
                String[] openStatus = status.split("=");
                for (int contact = 0; contact < contactDataList.size(); contact++) {
                    ContactData contactData = contactDataList.get(contact);
                    if (contactData.getId().equals(openStatus[0])) {
                        contactData.setStatusContact(openStatus[1]);
                    }
                    if (contactSelector.getContact().equals(openStatus[0])) {
                        // Відправляємо статус у Чат Activity якщо воно запушеною
                        addStatus(openStatus[1]);
                    }
                }
            }
            contactAdapter.notifyDataSetChanged(); // Оновлюємо лише один елемент
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка під час отримання масиву строк з JSONArray: " + e);
        }
    }

    /**
     * Метод отриманих у Chat Activity збережених повідомлень
     */
    private void openSaveMessage() {
        if (!contactSelector.getContact().isEmpty()) {
            operation.openSaveMessage(contactSelector.getContact());
            for (ContactData user : contactDataList) {
                // Оновлюємо кількість повідомлень для користувача
                if (user.getId().equals(contactSelector.getContact())) {
                    user.setMessageSize("");  // Оновлюємо messageSize
                    break;  // Вихід після оновлення користувача
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handleActivityCommand("sleep");
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleActivityCommand("awake");
        handleActivityCommand("reborn");
    }

    /**
     * Видалення ресиверів при знищенні активності.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            handleActivityCommand("died");//Сповіщаємо сервіс про те що Головне активність знищена
            unregisterReceiver(chatActivityReceiver);
            unregisterReceiver(ioServiceReceiver);//Знищуємо BroadcastReceiver
        } catch (IllegalArgumentException e) {
            Log.e("MainActivity", "Receiver не зареєстровано або вже видалений.");
        }
    }

    /**
     * Реєстрація chatActivityReceiver для отримання даних з ChatActivity через broadcast.
     */
    @SuppressLint("InlinedApi")
    private void registerChatActivityReceiver() {
        IntentFilter filter = new IntentFilter(FIELD.REPLY_FROM_CHAT.getFIELD());
        registerReceiver(chatActivityReceiver, filter, RECEIVER_NOT_EXPORTED);
    }

    /**
     * Реєстрація ioServiceReceiver для отримання даних з IOService через broadcast.
     */
    @SuppressLint("InlinedApi")
    private void registerIOServiceReceiver() {
        IntentFilter filter = new IntentFilter(FIELD.CUBE_RECEIVED_MESSAGE.getFIELD());
        registerReceiver(ioServiceReceiver, filter, RECEIVER_EXPORTED);

    }

    /**
     * Метод для запуску ChatActivity з передачею даних про користувача та контакту з ким від буде спілкуватися.
     *
     * @param view        Поточний елемент View.
     * @param contactData Дані контакту для чату.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void startChat(View view, @NonNull ContactData contactData) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(FIELD.SENDER_ID.getFIELD(), manager.userSetting().getId());
        intent.putExtra(FIELD.NAME.getFIELD(), contactData.getName());
        intent.putExtra(FIELD.LAST_NAME.getFIELD(), contactData.getLastName());
        intent.putExtra(FIELD.RECEIVER_ID.getFIELD(), contactData.getId());
        intent.putExtra(FIELD.STATUS.getFIELD(), contactData.getStatusContact());
        intent.putExtra(FIELD.PUBLIC_KEY.getFIELD(), contactData.getPublicKey());
        intent.putExtra(FIELD.PRIVATE_KEY.getFIELD(), contactData.getPrivateKey());
        intent.putExtra(FIELD.RECEIVER_PUBLIC_KEY.getFIELD(), contactData.getReceiverPublicKey());
        intent.putExtra(FIELD.SENDER_KEY.getFIELD(), contactData.getSenderKey());
        intent.putExtra(FIELD.RECEIVER_KEY.getFIELD(), contactData.getReceiverKey());
        intent.putExtra(FIELD.AVATAR_ORG.getFIELD(), contactData.getAvatarImageUrl());
        intent.putExtra(FIELD.AVATAR.getFIELD(), contactData.getAccountImageUrl());
//        intent.putExtra(FIELD.SERVER_IP.getFIELD(), manager.userSetting().getServerIp());
//        intent.putExtra(FIELD.SERVER_PORT.getFIELD(), manager.userSetting().getServerPort());
        intent.putExtra(FIELD.FILE_SERVER_IP.getFIELD(), manager.userSetting().getFileServerIp());
        intent.putExtra(FIELD.FILE_SERVER_PORT.getFIELD(), manager.userSetting().getFileServerPort());
        Log.e("MainActivity", manager.userSetting().getFileServerIp() + " " + manager.userSetting().getFileServerPort());

        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Метод який стартує сервіс для обміну повідомленнями
     * У методі перевіряється параметри на наявність в них даних це
     *
     * @Id ідентифікаційним номер користувача
     * @Server Ip адрес серверу повідомлень
     * @Server Port порт серверу повідомлень
     */
    public void startService() {
        if (manager.userSetting().getId() != null &&
                manager.userSetting().getServerIp() != null &&
                manager.userSetting().getServerPort() != null &&
                !manager.userSetting().getId().isEmpty() &&
                !manager.userSetting().getServerIp().isEmpty() &&
                !manager.userSetting().getServerPort().isEmpty()) {

            if (new UrlBuilder.IPValidator().validate(manager.userSetting().getServerIp()) &&
                    new UrlBuilder.PortValidator().validate(manager.userSetting().getServerPort())) {

                Intent serviceIntent = new Intent(this, IOService.class);
                String setting = new UserSetting.Builder()
                        .setId(manager.userSetting().getId())
                        .setServerIp(manager.userSetting().getServerIp())
                        .setServerPort(manager.userSetting().getServerPort())
                        .build().toJson("userId", "serverIp", "serverPort").toString();
                serviceIntent.putExtra(FIELD.CUBE_SEND_TO_SETTING.getFIELD(), setting);
                serviceIntent.putExtra(FIELD.MAIN_ACTIVITY_COMMAND.getFIELD(), "reborn");
                serviceIntent.putExtra(FIELD.MAIN_ACTIVITY_REGISTRATION.getFIELD(), getContactToJsonArray());

                if (!isMyServiceRunning(IOService.class)) {  // Якщо сервіс ще не запущений
                    startService(serviceIntent);
                    Log.d("MainActivity", "Сервіс запущено");
                } else {
                    Log.d("MainActivity", "Сервіс вже працює, повторний запуск не потрібен");
                }
            }
        }
    }

    private String getContactToJsonArray() {
        String request = "";
        try {
            Map<String, ContactData> contacts = contactManager.getContacts();
            JSONArray jsonContact = new JSONArray();
            for (Map.Entry<String, ContactData> entry : contacts.entrySet()) {
                jsonContact.put(entry.getValue().getId());
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", manager.userSetting().getId());
            jsonObject.put("contacts", jsonContact);
            request = jsonObject.toString();
        } catch (Exception e) {
            Log.e("MainActivity", "Json error" + e);
        }
        return request;
    }

    private void notifyIdReciverChanged(String receiverId) {
        Intent intent = new Intent(FIELD.CUBE_ID_RECIVER.getFIELD());
        intent.putExtra(FIELD.RECEIVER_ID.getFIELD(), receiverId);
        sendBroadcast(intent);
    }

    private void sendMessageToService(String message) {
        Intent intent = new Intent(FIELD.CUBE_SEND_TO_SERVER.getFIELD());
        intent.putExtra(FIELD.MESSAGE.getFIELD(), message);
        sendBroadcast(intent);  // Надсилає повідомлення сервісу
    }

    private void setSettingService(String message) {
        Intent intent = new Intent(FIELD.CUBE_SEND_TO_SETTING.getFIELD());
        intent.putExtra(FIELD.SETTING.getFIELD(), message);
        sendBroadcast(intent);  // Надсилає повідомлення сервісу
    }

    private void request(String message) {
        Intent intent = new Intent("MAIN_ACTIVITY_REGISTRATION");
        intent.putExtra("request", message);
        sendBroadcast(intent);  // Надсилає повідомлення сервісу
    }

    /**
     * Відправляє команди до сервісу про стан активності або зміни
     */
    private void handleActivityCommand(String life) {
        Intent intent = new Intent(FIELD.MAIN_ACTIVITY_COMMAND.getFIELD());
        intent.putExtra(FIELD.COMMAND.getFIELD(), life);
        sendBroadcast(intent);
    }

    private void generationStartWithSendKey(ContactData contactData) {
        try {
            KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA(); // Генерація та додовання RSA ключа
            keyGenerator.key();
            contactData.setPublicKey(keyGenerator.getPublicKey());
            contactData.setPrivateKey(keyGenerator.getPrivateKey());
            String key = KeyGenerator.AES.generateKey(16);    // Генерація та додовання AES ключа
            if (!key.isEmpty()) {
                contactData.setSenderKey(key);
                sendHandshake(
                        manager.userSetting().getId(),
                        contactSelector.getContact(),
                        FIELD.HANDSHAKE.getFIELD(),
                        FIELD.PUBLIC_KEY.getFIELD(),
                        // відправка публічного ключа отримувачу та чекаємо ключа від нього, після буде автоматично обмін AES ключа
                        contactData.getPublicKey(), operation.getTime());
            } else {
                return;
            }
            //Анулюймо контакт так як нам треба отримувати повідомлення якщо вони і будуть йти
            contactSelector.setContact("");
        } catch (Exception e) {
            Log.e("MainActivity", "Error generating RSA keys: " + e);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void getReceiverKey(ContactData contactData) {
        try {
            // Якщо в нас ReceiverPublicKey відсутній то ми ще раз відправляємо свій ключ якщо по якимось причинам в нас не пройшов хеншейк.
            // Сервер отримає хеншейк та якщо отримувач ще не відправив свій ключ то він збереже його
            if (contactData.getReceiverPublicKey() == null) {
                sendHandshake(manager.userSetting().getId(), contactSelector.getContact(), FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), contactData.getPublicKey(), operation.getTime());
                contactSelector.setContact("");  //Анулюймо контакт так як нам треба отримувати повідомлення якщо вони і будуть йти
            } else {
                if (contactSelector.getContact() != null && !contactSelector.getContact().isEmpty()) {
                    if (!contactData.getReceiverKey().isEmpty()) {
                        startChat(binding.getRoot().getRootView(), contactData);
                        openSaveMessage();
                        contactData.setMessageSize("");
                        contactAdapter.notifyDataSetChanged();
                        notifyIdReciverChanged(contactSelector.getContact());
                    }
                } else {
                    try {
                        PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(contactData.getReceiverPublicKey());
                        String AES = Encryption.RSA.encrypt(contactData.getSenderKey(), receiverPublicKey);
                        if (AES != null && !AES.isEmpty()) {
                            sendHandshake(manager.userSetting().getId(), contactSelector.getContact(), FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES, operation.getTime());
                        } else {
                            Log.e("MainActivity", "Failed to encrypt AES key");
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error during AES key encryption: " + e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }
    }

    /**
     * Обробка натискання на елемент списку контактів.
     *
     * @param adapterView Віджет списку.
     * @param view        Об'єкт View.
     * @param i           Позиція вибраного елемента.
     * @param l           Ідентифікатор вибраного елемента.
     *                    Пояснення до коду: пишу для себе, але це може бути корисно й для вас.
     * @onItemClick Відповідає за обробку натискання на контакти, які у вас є.
     * Під час натискання перевіряються такі параметри:
     * @publicKey Перевірка наявності publicKey — це RSA-ключ,
     * за допомогою якого шифруються спеціальні повідомлення, що надсилатимуться від
     * контакту, з яким ви будете спілкуватися.
     * @senderKey Перевірка наявності senderKey — це AES-ключ,
     * за допомогою якого шифруються повідомлення, які надсилає до вас контакт,
     * з яким ви спілкуєтеся.
     * @receiverPublicKey Перевірка наявності receiverPublicKey контакту,
     * якому ви хочете написати. Це RSA-ключ, за допомогою якого
     * шифруються спеціальні повідомлення для відправлення до цього контакту.
     * @receiverKey Перевірка наявності receiverKey користувача, якому ви пишете.
     * Це AES-ключ, за допомогою якого шифруються повідомлення для
     * відправлення до контакту, з яким ви спілкуєтеся.
     */

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        ContactData contactData = contactDataList.get(i);
        contactSelector.setContact(contactData);
        try {
            if (contactData.getPublicKey().isEmpty() || contactData.getPublicKey() == null) {
                generationStartWithSendKey(contactData);
            } else {
                getReceiverKey(contactData);
            }
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }
    }

    /**
     * Обробка довгого натискання для генерації нових ключів
     * в подальшому буде змінено на більш функціональний підхід
     * наприклад через меню
     */

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        ContactData contactData = contactDataList.get(i);
        contactSelector.setContact(contactData);
        KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA();
        keyGenerator.key();
        contactData.setPublicKey(keyGenerator.getPublicKey());
        contactData.setPrivateKey(keyGenerator.getPrivateKey());
        String key = KeyGenerator.AES.generateKey(16);
        contactData.setSenderKey(key);
        sendHandshake(manager.userSetting().getId(), contactSelector.getContact(), FIELD.HANDSHAKE.getFIELD(), FIELD.PUBLIC_KEY.getFIELD(), contactData.getPublicKey(), operation.getTime());
        contactSelector.setContact("");
        return false;
    }

    /**
     * Отримання повідомлення та відправка у клас Operation для обробки операції.
     *
     * @param message Текст повідомлення.
     */
    public void onReceived(String message) {
        operation.onReceived(message);
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

    /**
     * Додає статусу до broadcast для передачі його в ChatActivity.
     *
     * @param status Текст статусу.
     */
    public void addStatus(String status) {
        Intent intent = new Intent(FIELD.DATA_TO_CHAT.getFIELD());
        intent.putExtra(FIELD.STATUS.getFIELD(), status);
        sendBroadcast(intent);
    }

    /**
     * Зберігає отримане повідомлення.
     *
     * @param message дані повідомлення, яке зберігається.
     */
    public void saveMessage(String message) {
        try {
            Envelope envelope = new Envelope(new JSONObject(message));
            runOnUiThread(() -> operation.saveMessage(envelope, saveMessage, contactDataList));
        } catch (Exception e) {
            Log.e("MainActivity", "Save Message Error: " + e);
        }
    }

    public void setNotification(String info, String log) {
        runOnUiThread(() -> {
            notificationLoggers.add(new NotificationLogger(info, log));
            // Повідомити, що новий елемент було вставлено
            notificationAdapter.notifyItemInserted(notificationLoggers.size() - 1);
            // Прокрутити до нового елемента
            binding.log.smoothScrollToPosition(notificationLoggers.size() - 1);
        });
    }


    /**
     * Метод передача зображення аккаунту
     *
     * @param recipient отримувача хто зробив запит на отримання зображень
     *                  Під час передачі ми передаємо два зображення:
     *                  1. Маленьке — для контактів
     *                  2. Велике — для аватара
     *                  Під час передачі передається посилання на файл (він шифрується та відправляється у зашифрованому вигляді).
     *                  Передається ID, яке складається з:
     *                  - отримувача
     *                  - Назви коду файлу до якого він належить. Маленьке — для контактів або велике — для аватара
     *                  - Хеш-суми
     *                  зазначений метод передає данні для шифрування та данні для відправки файлу до отримувача.
     */

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void giveAvatar(String recipient) {
        try {
            File avatarImage = FilePathBuilder
                    .withDirectory(FilePathBuilder.getDirectory(this, "imageProfile"))
                    .setFileName(manager.userSetting().getAvatarImageUrl())
                    .newFile();

            File accountImage = FilePathBuilder
                    .withDirectory(FilePathBuilder.getDirectory(this, "imageProfile"))
                    .setFileName(manager.userSetting().getAccountImageUrl())
                    .newFile();

            for (ContactData user : contactDataList) {
                if (user.getId().equals(recipient)) {
                    String key = user.getSenderKey();
                    uploadFile(avatarImage, recipient + ":avatarImageUrl:" + FileData.getFileHash(avatarImage.toString(), "SHA-256"), key);
                    uploadFile(accountImage, recipient + ":accountImage:" + FileData.getFileHash(accountImage.toString(), "SHA-256"), key);
                    break;
                }
            }
        } catch (RuntimeException e) {
            Log.e("MainActivity", "Помилка виконання у методі [giveAvatar]: " + e.getMessage());
        } catch (Exception e) {
            Log.e("MainActivity", "Несподівана помилка у методі [giveAvatar]: " + e.getMessage());
        }
    }


    /**
     * Метод для обробки відповіді після запиту на отримання зображень контакту
     *
     * @param envelope    тіло повідомлення що містить основну інформацію для завантаження файлу
     *                    а саме посилання на файл хеш суму
     * @param avatar_name код зображення до якого він належить це може бути:
     *                    1. Маленьке — для контактів
     *                    2. Велике — для аватара
     *                    Після отримання даних формуємо ідентифікатори для подальшої обробки файлів
     * @envelope.getMessageId() Id повідомлення за яким прийшов відповідь тоб то від відправника
     * @envelope.getSenderId() Id відправника
     * @avatar_name містить код зображення
     * ці данні додаються у HasMap (avatar_map.put(positionID, value)) для подальшої обробки
     */
    private void addAvatar(Envelope envelope, String avatar_name) {
        try {
            for (int position = 0; position < contactDataList.size(); position++) {
                if (contactDataList.get(position).getId().equals(envelope.getSenderId())) {
                    addPositionID(envelope.getMessageId(), envelope.getSenderId() + ":" + avatar_name);
                    String fileUrl = Encryption.AES.decrypt(envelope.getFileUrl(), contactDataList.get(position).getReceiverKey());
                    String fileHash = Encryption.AES.decrypt(envelope.getFileHash(), contactDataList.get(position).getReceiverKey());
                    /*Потрібно реалізація перевірки хеш суми яку ми отримали з хеш сумою файлу після декодування для безпеки*/
                    URL url = new URL(fileUrl);
                    new Downloader(this, url, FilePathBuilder.getDirectory(this, "imageProfile"),
                            position, envelope.getMessageId(), fileHash);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка у методі [addAvatar] " + e);
        }
    }

    /**
     * Отримання зображення контакту який в нас збережений
     */
    @Override
    public void getAvatar(Envelope envelope) {
        addAvatar(envelope, "avatar");
    }


    /**
     * Отримання зображення контакту який в нас оригінального збережений
     */
    @Override
    public void getAvatarORG(Envelope envelope) {
        addAvatar(envelope, "avatar_org");
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void uploadFile(File file, String sender, String key) {
        String serverUrl = new UrlBuilder.Builder()
                .setProtocol("http")
                .setIp(manager.userSetting().getFileServerIp())
                .setPort(manager.userSetting().getFileServerPort())
                .setDirectory("/api/files/upload")
                .build()
                .buildUrl();
        // Виконуємо шифрування у фоновому потоці
        new Handler().postDelayed(() -> {
            try {
                FileEncryption fileEncryption = new FileEncryption(this, sender, serverUrl);
                SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
                String encryptedFile = fileEncryption.getEncFile(file, secretKey);
                addPositionID(sender, encryptedFile);
                fileEncryption.fileEncryption();
            } catch (Exception e) {
                Log.e("MainActivity", "Помилка під час шифрування файлу [uploadFile]" + e);
            }
        }, 1);
    }

    // Метод для додавання значення
    public void addPositionID(String positionID, String value) {
        avatar_map.put(positionID, value);
    }

    // Метод для отримання та автоматичного видалення значення
    public String getAvatarInfoAndRemove(String positionID) {
        return avatar_map.remove(positionID);
    }


    /**
     * Метод для додовання AES ключа контакту
     *
     * @param sender          відправник в нас збережений як контакт
     * @param receivedMessage саме повідомлення з ключем яки зашифрований нашим публічним ключем RSA
     *                        після отримання даних додаємо до бази даних контакту
     */
    @Override
    public void addAESKey(String sender, String receivedMessage) {
        try {
            JSONObject jsonObject = new JSONObject(receivedMessage);
            String receiverKey = jsonObject.getString(FIELD.AES_KEY.getFIELD());
            for (ContactData user : contactDataList) {
                if (user.getId().equals(sender)) {
                    PrivateKey privateKey = new KeyGenerator.RSA().decodePrivateKey(user.getPrivateKey());
                    String AES = Encryption.RSA.decrypt(receiverKey, privateKey);
                    user.setReceiverKey(AES);
                    contactManager.updateContact(user, secretKey);
                    break;
                }
            }
            contactAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка під час парсинга ключа [" + sender + "]: " + e);
        }
    }

    /**
     * Отримання та відправу Handshake.
     *
     * @param envelope повідомлення, дані якого зберігається.
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

    public void sendHandshake(String userId, String receiverId, String operation, String nameKey, String key, String time) {
        sendMessageToService(new Envelope.Builder().
                setSenderId(userId).
                setReceiverId(receiverId).
                setOperation(operation).
                setMessage(String.format("{\"" + nameKey + "\":\"%s\"}", key)).
                setMessageId("").
                build().
                toJson("senderId", "receiverId", "operation", "message", "messageId").
                toString());
    }

    /**
     * Зберігає отримане Handshake.
     *
     * @param sender    відправник.
     * @param publicKey ключ відправника.
     */
    private void updateReceiverPublicKey(String sender, String publicKey) {
        try {
            for (ContactData user : contactDataList) {
                if (user.getId().equals(sender)) {
                    user.setReceiverPublicKey(publicKey);
                    PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(user.getReceiverPublicKey());
                    String AES = Encryption.RSA.encrypt(user.getSenderKey(), receiverPublicKey);
                    sendHandshake(manager.userSetting().getId(), sender, FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES, operation.getTime());
                    contactManager.updateContact(user, secretKey);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error of update key " + e);
        }
        contactAdapter.notifyDataSetChanged();
    }

    /**
     * Оновлення адаптера контактів
     */
    @Override
    public void updateAdapter() {
        contactAdapter.notifyDataSetChanged();
    }

    /**
     * Метод виклику сканера для сканування Qr коду з даними аккаунту
     * Qr код містить:
     * ID користувача
     * Імʼя та прізвище
     * особистий пароль згенерований під час реєстрації на сервері
     * Дані для підключення до серверів
     */
    @Override
    public void scannerQrAccount() {
        //manager.clearMessagesTable();
        new QR(qrCodeAddAccount);
    }

    /**
     * Лаунчер для сканування QR-коду для додавання аккаунту користувача.
     * Використовує ActivityResultLauncher для сканування та обробки результату.
     * Якщо вміст QR-коду не порожній, додається новий аккаунту користувача.
     */
    @SuppressLint("SetTextI18n")
    private ActivityResultLauncher<ScanOptions> qrCodeAddAccount = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            manager.writeAccount(manager.getJson(result.getContents()));
        }
    });

    /**
     * Викликає сканер QR-коду для додавання контакту.
     */
    @Override
    public void scannerQrContact() {
        new QR(qrCodeAddContact);
    }

    /**
     * Лаунчер для сканування QR-коду для додавання контакту.
     * Використовує ActivityResultLauncher для сканування та обробки результату.
     * Якщо вміст QR-коду не порожній, додається новий контакт.
     */
    @SuppressLint("SetTextI18n")
    private ActivityResultLauncher<ScanOptions> qrCodeAddContact = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            manager.createContact(result.getContents());
        }
    });


    // Константа для запиту вибору зображення
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageExplorer imageExplorer;

    @Override
    public void imageNavigation() {
        // Ініціалізація об'єкта ImageExplorer для відображення діалогового вікна для вибору зображення
        imageExplorer = new ImageExplorer(this);
        //imageExplorer.show(getSupportFragmentManager(), "imageExplorer");
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

    /**
     * Отримання зображень для аккаунту та додовання посилань у базу даних
     *
     * @param avatarImageUrl  оригінальний розмір зображення
     * @param accountImageUrl зображення яку було зрізано (це може бути ваше облича яке буде відображене у користувачів з якими ви спілкуєтесь)
     */
    @Override
    public void setImageAccount(String avatarImageUrl, String accountImageUrl) {
        runOnUiThread(() -> {
            UserSetting userSetting = new UserSetting(manager.getAccount());
            userSetting.setAvatarImageUrl(avatarImageUrl);
            userSetting.setAccountImageUrl(accountImageUrl);
            manager.writeAccount(userSetting.toJson());
            navigationManager.setAvatarImage(avatarImageUrl);
            navigationManager.setAccountImage(accountImageUrl);
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
                Log.e("MainActivity", e.toString());
            }
        }
    }


    @Override
    public void onClick(View view) {
        if (view == binding.setting) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (view == binding.fab) {
            new ContactCreator(this).show();
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
        ContactData contactData = contactDataList.get(position);
        sendHandshake(manager.userSetting().getId(), contactData.getId(), FIELD.GET_AVATAR.getFIELD(), "get_avatar", contactData.getPublicKey(), operation.getTime());
    }

    /**
     * Прогрес завантаження зображень контакту після запиту на отримання зображень
     */
    @Override
    public void setProgressShow(String positionId, int progress, String info) {
        try {
            String body_map = avatar_map.get(positionId);
            String[] getBody = body_map.split(":");
            for (int position = 0; position < contactDataList.size(); position++) {
                if (contactDataList.get(position).getId().equals(getBody[0])) {
                    contactAdapter.setProgressForPosition(position, progress);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "[ Progress ]: " + e);
        }
    }

    /**
     * Метод який виконується після шифрування файлу д відправи
     *
     * @param positionId Id позиції за яким виконується дія а саме:
     *                   в нас два зображення оригінальне та обрізане з обличчям.
     *                   Вони обробляються в різних позиціях окремо і щоб зрозуміти
     *                   хто який ми робимо ідентифікатор Id що містить такі данні
     * @param info
     */

    @Override
    public void endProgress(String positionId, String info) {
        try {
            String path = getAvatarInfoAndRemove(positionId);
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            String serverUrl = new UrlBuilder.Builder()
                    .setProtocol("http")
                    .setIp(manager.userSetting().getFileServerIp())
                    .setPort(manager.userSetting().getFileServerPort())
                    .setDirectory("/api/files/download")
                    .setFileName(fileName)
                    .build()
                    .buildUrl();
            String[] position = positionId.split(":");
            for (ContactData user : contactDataList) {
                if (user.getId().equals(position[0])) {
                    if (position[1].equals("avatarImageUrl")) {
                        String rMessage = Encryption.AES.encrypt("avatar_org", user.getSenderKey());
                        String Url = Encryption.AES.encrypt(serverUrl, user.getSenderKey());
                        String Has = Encryption.AES.encrypt(position[2], user.getSenderKey());

                        sendMessageToService(new Envelope(manager.userSetting().getId(), position[0], FIELD.AVATAR_ORG.getFIELD(),
                                rMessage,
                                Url, Has, UUID.randomUUID().toString(), operation.getTime()).toJson().toString());
                    } else {
                        String rMessage = Encryption.AES.encrypt("avatar", user.getSenderKey());
                        String Url = Encryption.AES.encrypt(serverUrl, user.getSenderKey());
                        String Has = Encryption.AES.encrypt(position[2], user.getSenderKey());

                        sendMessageToService(new Envelope(manager.userSetting().getId(), position[0], FIELD.AVATAR.getFIELD(),
                                rMessage,
                                Url, Has, UUID.randomUUID().toString(), operation.getTime()).toJson().toString());
                    }
                    break;
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка після шифрування файлу для відправки аватар до контактів " + e);
        }
    }

    @Override
    public void addFile(String messageId, String url, String encFile, String has) {
    }

    /**
     * Метод за допомогою якого ми оновлюємо зображення аватару контактів
     *
     * @param position   позиція контакту
     * @param positionId Id позиції
     * @param url        місце розташування зображення контакту
     * @param has        Хеш сума зображення
     *                   після завантаження файлу та декодування йго у зображення
     *                   зображення зберігаєтеся у спеціальні директорії (Директорія /storage/emulated/0/Android/data/com.example.cube/files/imageProfile)
     *                   та отримується посилання на нього (Приклад /storage/emulated/0/Android/data/com.example.cube/files/imageProfile/file.png)
     *                   яке зберігається у базі даних контакту за допомогою методу contactManager.updateContact(user, secretKey);
     *                   куди передається об'єкт UserData з посиланням на файли зображення AvatarImageUrl та AccountImageUrl
     */
    @Override
    public void updateItem(int position, String positionId, String url, String has) {
        try {
            String avatar = getAvatarInfoAndRemove(positionId);
            String[] positionName = avatar.split(":");
            for (ContactData user : contactDataList) {
                if (user.getId().equals(positionName[0])) {
                    if (positionName[1].equals("avatar_org")) {
                        user.setAvatarImageUrl(url);
                    }
                    if (positionName[1].equals("avatar")) {
                        user.setAccountImageUrl(url);
                    }
                    contactManager.updateContact(user, secretKey);
                    break;
                }
            }
            contactAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e("MainActivity", "[Помилка під час отримання посилань на файл] " + e);
        }

    }

    /**
     * Метод для отримання AES ключа для розшифрування файлу
     *
     * @param positionId Id-Повідомлення яке прийшло та за яким збережений Id контакту та код зображення
     *                   Звертаємося до avatar_map та отримуємо масив через який ми отримуємо ID контакту для отримання ключа
     *                   для розшифрування файлу
     */
    @Override
    public String getKey(String positionId) {
        String body_map = avatar_map.get(positionId);
        String[] position = body_map.split(":");
        for (ContactData user : contactDataList) {
            if (user.getId().equals(position[0])) {
                return user.getReceiverKey();
            }
        }
        return null;
    }

    /**
     * Метод виклику вікна налаштування
     */
    @Override
    public void showSetting() {
        SettingDialog inputDialog = new SettingDialog(this, manager.getAccount());
        inputDialog.show();
    }

    /**
     * Метод отримання налаштування
     */
    @Override
    public void onSetting(JSONObject jsonObject) {
        manager.writeAccount(jsonObject);
        String setting = new UserSetting.Builder()
                .setId(manager.userSetting().getId())
                .setServerIp(manager.userSetting().getServerIp())
                .setServerPort(manager.userSetting().getServerPort())
                .build().toJson("userId", "serverIp", "serverPort").toString();
        setSettingService(setting);
    }

    @Override
    public void showAccount() {
        AccountDialog accountDialog = new AccountDialog(this, manager.getAccount());
        accountDialog.show(getSupportFragmentManager(), "AccountDialog");
    }

    @Override
    public void logout() {
        contactManager.deleteAll();
        request(getContactToJsonArray());
        deleteAll();
        finish();
    }

    /**
     * Метод який видаляє всі данні
     */
    private void deleteAll() {
        deleteDatabase("cube.db");
        finish();
    }
}