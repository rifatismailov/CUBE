package com.example.cube;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;


import com.example.cube.databinding.ActivityMainBinding;
import com.example.cube.permission.Permission;
import com.example.cube.socket.ServerConnection;
import com.example.folder.file.FileDetect;
import com.example.qrcode.QR;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Основний клас активності MainActivity, що відповідає за обробку даних користувача, підключення до сервера та взаємодію з контактами.
 * Реалізує кілька інтерфейсів для обробки кліків та отримання повідомлень від сервера.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, ServerConnection.ConnectionListener, Add_Client.Add_Client_Interface {

    private ActivityMainBinding binding;
    private List<UserData> userList = new ArrayList<>();  // Список користувачів
    private UserAdapter userAdapter;  // Адаптер для відображення користувачів
    private ServerConnection serverConnection;  // З'єднання з сервером

    private String userId;  // ID користувача
    private String receiverId;  // ID отримувача
    private String name;  // Ім'я користувача
    private String lastName;  // Прізвище користувача
    private String password = "1234567890123456";  // Пароль
    private byte[] keyBytes = password.getBytes();  // Генерація байт ключа
    private final SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");  // AES-ключ
    private HashMap<Integer, Envelope> saveMessage = new HashMap<>();  // Збережені повідомлення
    private int numMessage = 0;  // Лічильник повідомлень
    private Map<String, UserData> contacts = new HashMap<>();  // Контакти користувачів

    /**
     * Метод для зчитування даних з JSON-файлу та їх обробки.
     * @param jsonObject JSON-об'єкт, що містить дані користувача.
     */
    private void readJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            Toast.makeText(this, "Не вдалося завантажити файл JSON.", Toast.LENGTH_SHORT).show();
        } else {
            String userId = jsonObject.optString("userId", "");
            String name = jsonObject.optString("name", "");
            String lastName = jsonObject.optString("lastName", "");
            String password = jsonObject.optString("password", "");

            if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(name) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Невірні дані у файлі JSON.", Toast.LENGTH_SHORT).show();
            } else {
                this.userId = userId;
                this.name = name;
                this.lastName = lastName;
                this.password = password;
                binding.id.setText(this.userId);
                binding.name.setText(this.name + " " + this.lastName);
            }
        }
    }

    /**
     * BroadcastReceiver для отримання даних з ChatActivity через broadcast.
     */
    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String dataFromChat = intent.getStringExtra("data_from_chat");
            if (dataFromChat != null) {
                receivingData(dataFromChat);
            }
        }
    };

    /**
     * Метод onCreate викликається при створенні активності.
     * @param savedInstanceState Збережений стан активності.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Permission(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        JSONObject jsonObject = new FileDetect().readJsonFromFile(this, "cube.json");
        if (jsonObject != null) {
            readJson(jsonObject);
        }
        binding.setting.setOnClickListener(this);
        binding.fab.setOnClickListener(this);

        // Створення підключення до сервера
        serverConnection = new ServerConnection(this);
        serverConnection.connectToServer();
        serverConnection.setUserId(userId);
        initUserList();
    }



    /**
     * Ініціалізація списку користувачів та його заповнення з файлу.
     */
    private void initUserList() {
        userList.clear();

        try {
            File externalDir = new File(getExternalFilesDir(null), "cube");
            contacts = (Map<String, UserData>) new FileDetect().loadFromFile(externalDir + "/contacts.cube", secretKey);

            for (Map.Entry<String, UserData> entry : contacts.entrySet()) {
                userList.add(entry.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        userAdapter = new UserAdapter(this, R.layout.iteam_user, userList);
        binding.contentMain.userList.setAdapter(userAdapter);
        binding.contentMain.userList.setOnItemClickListener(this);
    }

    /**
     * Додає повідомлення до broadcast для передачі його в ChatActivity.
     * @param message Текст повідомлення.
     */
    private void addMessage(String message) {
        Intent intent = new Intent("com.example.cube.DATA_TO_CHAT");
        intent.putExtra("data_from_MainActivity", message);
        sendBroadcast(intent);
    }

    /**
     * Отримує дані з активності чату та передає їх на сервер.
     * @param data Отримані дані.
     */
    private void receivingData(String data) {
        if (data.equals("endUser")) {
            serverConnection.setReceiverId(null);
        } else {
            serverConnection.sendData(data);
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
     * @param view Поточний елемент View.
     * @param userData Дані користувача для чату.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void startChat(View view, UserData userData) {
        IntentFilter filter = new IntentFilter("com.example.cube.REPLY_FROM_CHAT");
        registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("senderId", userId);
        intent.putExtra("name", userData.getName());
        intent.putExtra("receiverId", userData.getId());
        intent.putExtra("status", "online");
        intent.putExtra("publicKey", userData.getPublicKey());
        startActivity(intent);
    }

    /**
     * Обробка натискання на елемент списку користувачів.
     * @param adapterView Віджет-список.
     * @param view Об'єкт View.
     * @param i Позиція вибраного елемента.
     * @param l Ідентифікатор вибраного елемента.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        UserData userData = userList.get(i);
        receiverId = userData.getId();
        Toast.makeText(this, "" + userId, Toast.LENGTH_SHORT).show();
        serverConnection.setReceiverId(receiverId);

        if (userData.getReceiverPublicKey() == null || userData.getReceiverPublicKey().isEmpty()) {
            sendKey(receiverId);
        } else {
            startChat(binding.getRoot().getRootView(), userData);
            openSaveMessage();
            userData.setMessageSize("");
            userAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Відправка ключа шифрування для безпечного з'єднання.
     * @param receiverId Ідентифікатор отримувача.
     */
    public void sendKey(String receiverId) {
        byte[] keyBytes = password.getBytes();  // 16-байтний ключ
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        String publicKey = "{\"publicKey\":\"" + password + "\"}";
        receivingData(new Envelope(userId, receiverId, publicKey).toJson().toString());
    }

    /**
     * Відправка збережених повідомлень після затримки, щоб активність чату встигла ініціалізуватися.
     */
    private void openSaveMessage() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Iterator<Map.Entry<Integer, Envelope>> iterator = saveMessage.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (envelope.getSenderId().equals(receiverId)) {
                    addMessage(envelope.toJson().toString());
                    Log.d("MainActivity", "Збережено повідомлення " + numMessage + " " + envelope.toJson().toString());
                    iterator.remove();
                }
            }
        }, 1000);  // Відкладення на 1 секунду
    }
    /**
     * Лаунчер для сканування QR-коду для додавання аккаунту користувача.
     * Використовує ActivityResultLauncher для сканування та обробки результату.
     * Якщо вміст QR-коду не порожній, додається новий аккаунту користувача.
     */
    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        try {
            if (result.getContents() != null) {
                // Створюємо JSONObject з JSON-рядка, отриманого з QR-коду
                JSONObject jsonObject = new JSONObject(result.getContents());
                // Зберігаємо JSON-дані в файл "cube.json"
                new FileDetect().saveJsonToFile(this, "cube.json", jsonObject);
                // Читаємо дані з JSON-об'єкта
                readJson(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Тут можна також відобразити повідомлення про помилку користувачу
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
            addContact(result.getContents());
        }
    });

    @Override
    public void onClick(View view) {
        // Визначаємо дії при натисканні на різні елементи інтерфейсу
        if (view == binding.setting) {
            // Запуск сканера QR-коду для отримання налаштувань
            new QR(qrCodeLauncher);
        } else if (view == binding.fab) {
            // Відкриваємо вікно для додавання нового клієнта
            new Add_Client(MainActivity.this);
        }
    }

    @Override
    public void onConnected() {
        // Викликається при підключенні до сервера
        serverConnection.listenForMessages();
    }

    @Override
    public void onMessageReceived(String message) {
        // Обробка отриманих повідомлень
        addMessage(message);
    }

    /**
     * Зберігає отримане повідомлення в системі.
     * @param envelope дані повідомлення, яке зберігається.
     */
    @Override
    public void saveMessage(Envelope envelope) {
        saveMessage.put(numMessage, envelope);
        numMessage++;
        // Оновлення інтерфейсу користувача на основі нових повідомлень
        runOnUiThread(() -> {
            for (UserData user : userList) {
                // Знайдіть користувача за його id та оновіть кількість повідомлень
                if (user.getId().equals(envelope.getSenderId())) {
                    user.setMessageSize("" + numMessage);  // Оновлюємо messageSize
                    break;  // Вихід після оновлення користувача
                }
            }
            // Оновлюємо адаптер для відображення змін у списку користувачів
            userAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Обробляє процедуру рукостискання (handshake) з клієнтом.
     * @param senderId ідентифікатор відправника.
     * @param publicKey публічний ключ відправника.
     */
    @Override
    public void clientHandshake(String senderId, String publicKey) {
        for (UserData user : userList) {
            // Знайдіть користувача за його id
            if (user.getId().equals(senderId)) {
                // Якщо у користувача ще немає публічного ключа, встановіть його
                if (user.getReceiverPublicKey() == null) {
                    user.setReceiverPublicKey(publicKey);  // Встановлюємо публічний ключ
                    sendKey(senderId);  // Відправляємо ключ
                    break;  // Вихід після оновлення
                } else break;
            }
        }
        // Оновлюємо адаптер для відображення змін
        userAdapter.notifyDataSetChanged();
    }

    /**
     * Додає новий контакт, отриманий через QR-код або інші джерела.
     * @param contact дані контакту у вигляді рядка JSON.
     */
    private void addContact(String contact) {
        try {
            // Отримуємо директорію для зберігання файлу
            File externalDir = new File(getExternalFilesDir(null), "cube");

            // Створюємо JSONObject з контактних даних
            JSONObject jsonObject = new JSONObject(contact);
            String name_contact = jsonObject.getString("name_contact");
            String id_contact = jsonObject.getString("id_contact");
            String public_key_contact = jsonObject.getString("public_key_contact");

            // Додаємо новий контакт до списку користувачів
            userList.add(new UserData(id_contact, public_key_contact, name_contact, ""));

            // Оновлюємо мапу контактів
            for (UserData user : userList) {
                contacts.put(user.getId(), user);
            }

            // Зберігаємо контакти у файл з шифруванням
            new FileDetect().saveToFile(contacts, externalDir + "/contacts.cube", secretKey);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Зберігає контакт, отриманий ззовні, до локальної системи.
     * @param contact контакт у вигляді JSON-рядка.
     */
    @Override
    public void save_contact(String contact) {
        if (contact != null) {
            addContact(contact);
        }
    }

    /**
     * Викликає сканер QR-коду для додавання контакту.
     */
    @Override
    public void scanner_qr_contact() {
        new QR(qrCodeAddContact);
    }

}