package com.example.cube;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;


import com.example.cube.databinding.ActivityMainBinding;
import com.example.cube.permission.Permission;
import com.example.folder.file.FileDetect;
import com.example.qrcode.QR;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ActivityMainBinding binding;
    private List<UserData> userList = new ArrayList<>();
    private UserAdapter userAdapter;


    private static final String SERVER_IP = "192.168.193.183";  // IP сервера
    private static final int SERVER_PORT = 8080;  // Порт сервера

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String userId;
    private String receiverId;
    private String name;
    private String lastName;
    private String password;
    private HashMap<Integer, Envelope> saveMessage = new HashMap<>();
    private int numMessage = 0;

    private ScheduledExecutorService scheduler;

    public void startConnection() {
        connectToServer();
        startConnectionChecker();
    }

    // Метод підключення до сервера
    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            registerUser();
            sendDataToChatActivity(); // Запускаємо прослуховування після підключення
        } catch (Exception e) {
            Log.e("MainActivity", "Помилка підключення до сервера", e);
        }
    }

    // Метод для перевірки підключення
    private void startConnectionChecker() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            if (socket == null || socket.isClosed() || !socket.isConnected()) {
                reconnectToServer(); // Перепідключення до сервера
            }
        }, 0, 5, TimeUnit.SECONDS); // Перевірка кожні 5 секунд
    }


    // Метод для перепідключення до сервера
    private void reconnectToServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Закриваємо старий сокет
            }
            connectToServer(); // Спробуйте підключитися знову
        } catch (IOException e) {
        }
    }

    // Метод для зупинки перевірки підключення
    public void stopConnectionChecker() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        try {
            if (socket != null) {
                socket.close(); // Закриваємо сокет
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Помилка закриття сокета", e);
        }
    }

    private void readJson(JSONObject jsonObject) {
        if (jsonObject == null) {
            // Файл не знайдено або порожній, показуємо повідомлення
            Toast.makeText(this, "Не вдалося завантажити файл JSON.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Отримуємо дані з JSON з перевіркою на існування ключів
        String userId = jsonObject.optString("userId", null);
        String name = jsonObject.optString("name", null);
        String lastName = jsonObject.optString("lastName", null);
        String password = jsonObject.optString("password", null);
        Toast.makeText(this, "Name " + userId, Toast.LENGTH_SHORT).show();

        // Перевіряємо, чи всі дані присутні
        if (userId == null || name == null || lastName == null || password == null) {
            Toast.makeText(this, "Невірні дані у файлі JSON.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Зберігаємо дані в поля класу
        this.userId = userId;
        this.name = name;
        this.lastName = lastName;
        this.password = password;

        // Встановлюємо дані у відповідні поля на екрані
        binding.id.setText(this.userId);
        binding.name.setText(this.name + " " + this.lastName);
    }

    // Метод для реєстрації користувача
    @SuppressLint("SetTextI18n")
    private void registerUser() {
        // Формуємо повідомлення для реєстрації
        String registerMessage = "{\"userId\":\"" + this.userId + "\"}";

        // Надсилаємо повідомлення на сервер для реєстрації в
        new Thread(() -> output.println(registerMessage)).start();  // Надсилаємо повідомлення на сервер для реєстрації
    }

    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Отримання даних від ChatActivity
            String dataFromChat = intent.getStringExtra("data_from_chat");
            if (dataFromChat != null) {
                // Обробляємо дані або зберігаємо їх
                receivingData(dataFromChat);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Permission(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        JSONObject jsonObject = new FileDetect().readJsonFromFile(this, "cube.json");
        if (jsonObject != null) {
            // Файл не знайдено або порожній, показуємо повідомлення
            readJson(jsonObject);
        }
        binding.setting.setOnClickListener(this);
        new Thread(this::startConnection).start();
        initUserList();

    }


    private void initUserList() {
        userList.clear();
        userList.add(new UserData("H652882301", "yt23HSGFD634FFD", "Rifat Ismailov", "12"));
        userList.add(new UserData("H652882302", "yt23HSGFD634FFD", "Rifat Ismailov", "2"));
        userList.add(new UserData("H652882303", "yt23HSGFD634FFD", "Rifat Ismailov", "4"));
        userList.add(new UserData("H652882304", "yt23HSGFD634FFD", "Rifat Ismailov", "22"));
        userList.add(new UserData("H652882305", "yt23HSGFD634FFD", "Rifat Ismailov", "31"));
        userList.add(new UserData("H652882306", "yt23HSGFD634FFD", "Rifat Ismailov", "34"));
        userList.add(new UserData("H652882307", "yt23HSGFD634FFD", "Rifat Ismailov", "4"));
        userList.add(new UserData("H652882308", "yt23HSGFD634FFD", "Rifat Ismailov", "1"));
        userList.add(new UserData("H652882309", "yt23HSGFD634FFD", "Rifat Ismailov", "6"));
        userList.add(new UserData("H652882310", "yt23HSGFD634FFD", "Rifat Ismailov", "2"));
        userList.add(new UserData("H652882311", "yt23HSGFD634FFD", "Rifat Ismailov", "7"));
        userList.add(new UserData("H652882312", "yt23HSGFD634FFD", "Rifat Ismailov", "13"));
        userAdapter = new UserAdapter(this, R.layout.iteam_user, userList);
        binding.userList.setAdapter(userAdapter);
        binding.userList.setOnItemClickListener(this);
    }


    private void sendDataToChatActivity() {
        new Thread(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    //Log.d("ChatActivity", "Отримано повідомлення: " + message);
                    // Перевірка, що message не null перед відправленням
                    Log.d("MainActivity", "Message " + message);

                    if (message != null) {
                        JSONObject object = new JSONObject(message);
                        Envelope envelope = new Envelope(object);

                        if (userId.equals(envelope.getSenderId()) && receiverId.equals(envelope.getReceiverId())) {
                            addMessage(message);
                            Thread.sleep(100); // 100 мс
                        } else if (receiverId != null && receiverId.equals(envelope.getSenderId())) {
                            addMessage(message);
                            Thread.sleep(100); // 100 мс
                        } else {
                            saveMessage.put(numMessage, envelope);
                            Log.d("MainActivity", "Збережено повідомлення " + numMessage);
                            numMessage++;
                        }

                    } else {
                        Log.e("MainActivity", "Отримано null повідомлення");
                    }
                }
            } catch (IOException e) {
                Log.e("MainActivity", "Помилка при отриманні повідомлень", e);
            } catch (InterruptedException | JSONException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void addMessage(String message) {
        Intent intent = new Intent("com.example.cube.DATA_TO_CHAT");
        intent.putExtra("data_from_MainActivity", message);
        sendBroadcast(intent);
    }


    // Метод для отримання даних з ChatActivity
    private void receivingData(String data) {
        // Збереження даних або робота з ними
        //Log.d("MainActivity", "Збережено дані: " + data);
        new Thread(() -> output.println(data)).start();  // Надсилаємо повідомлення на сервер

        //sendMessage(data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Від'єднання ресивера, коли активність знищується
        unregisterReceiver(dataReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void startChat(View view, UserData userData) {
        // Реєструємо ресивер для отримання даних від ChatActivity
        IntentFilter filter = new IntentFilter("com.example.cube.REPLY_FROM_CHAT");
        registerReceiver(dataReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        // Запускаємо ChatActivity
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("senderId", userId);
        intent.putExtra("name", userData.getName());
        intent.putExtra("receiverId", userData.getId());
        intent.putExtra("status", "online");
        intent.putExtra("publicKey", userData.getPublicKey());
        startActivity(intent);
        // Відправляємо дані в ChatActivity через broadcast
        sendDataToChatActivity();
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        UserData userData = userList.get(i);
        receiverId = userData.getId();
        startChat(binding.getRoot().getRootView(), userData);
        openSameMessage();
    }

    //Для безпечного видалення елементів під час ітерації використовуйте Iterator. Ось як ви можете це зробити:
    //Iterator.remove(): Використовується для безпечного видалення елементів під час ітерації через колекцію, не викликаючи помилок.
    //Використовуйте метод Handler для відтермінованої відправки: Проблема полягає в тому, що нова активність ще не готова до прийому повідомлень,
    // і ми використовуємо Handler щоб відтермінувати відправку повідомлення:
    private void openSameMessage() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Iterator<Map.Entry<Integer, Envelope>> iterator = saveMessage.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (envelope.getSenderId().equals(receiverId)) {
                    addMessage(envelope.toJson().toString());
                    Log.d("MainActivity", "Збережено повідомлення " + numMessage + " " + envelope.toJson().toString());
                    iterator.remove();  // Безпечно видаляємо елемент під час ітерації
                }
            }
        }, 1000);  // Відкладення на 1 секунду для того, щоб активність встигла ініціалізуватися
    }


    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<ScanOptions> qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        try {
            if (result.getContents() != null) {
                // Створюємо JSONObject з JSON-рядка, отриманого з QR-коду
                JSONObject jsonObject = new JSONObject(result.getContents());
                new FileDetect().saveJsonToFile(this, "cube.json", jsonObject);
                readJson(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            // Тут можна також відобразити повідомлення про помилку користувачу
        }
    });


    @Override
    public void onClick(View view) {
        if (view == binding.setting) {
            new QR(qrCodeLauncher);
        }
    }
}