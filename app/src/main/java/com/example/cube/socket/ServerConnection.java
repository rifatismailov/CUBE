package com.example.cube.socket;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Клас для роботи з серверним підключенням.
 * Використовується для надсилання, отримання даних та перевірки стану підключення.
 */
public class ServerConnection {
    private final String SERVER_IP;  // IP сервера
    private final int SERVER_PORT;  // Порт сервера

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private ScheduledExecutorService scheduler;
    private ExecutorService connect; // Окремий ексекутор для підклюкача
    private ExecutorService listenerExecutor;  // Окремий ексекутор для слухача
    private ExecutorService senderExecutor;  // Окремий ексекутор для слухача


    private final ConnectionListener listener;
    private String userId;
    private String receiverId;
    private boolean connectUSER = false;
    private boolean statusCONNECT = false;
    private int checkCONNECT = 0;

    public ServerConnection(ConnectionListener listener, String SERVER_IP, int SERVER_PORT) {
        this.listener = listener;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // Метод підключення до сервера
    public void connectToServer() {
        connect = Executors.newFixedThreadPool(1);
        senderExecutor = Executors.newFixedThreadPool(1);
        connect.execute(() -> {
            while (true) {
                try {
                    listener.setLogs("[INFO] [Connect]", "Спроба перепідключення...");
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    listener.setLogs("[INFO] [Connect]", "Успішно перепідключено.");

                    // Реєстрація користувача знову після підключення
                    registerUser();
                    checkCONNECT = 0;
                    statusCONNECT = false;

                    // Відновлення перевірки з'єднання
                    startConnectionChecker();

                    // **Важливо:** Перезапустити слухання повідомлень з новим потоком
                    listenerExecutor = Executors.newFixedThreadPool(1);
                    listenerExecutor.execute(this::listenForMessages);
                    listener.onConnected();
                    break; // Вихід з циклу при успішному підключенні
                } catch (IOException e) {
                    listener.setLogs("[ERROR] [Connect]", "Не вдалося перепідключитися, спроба знову через 5 секунд.");
                    try {
                        Thread.sleep(5000); // Очікування 5 секунд перед повторною спробою
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    // Метод для перевірки підключення
    private void startConnectionChecker() {

        listener.setLogs("[INFO] [Check connect]", "Запуск переврки....");
        if (scheduler != null) {
            scheduler.shutdownNow(); // Завершити старий екземпляр
        }
        scheduler = Executors.newScheduledThreadPool(1); // Створити новий екземпляр

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                listener.setLogs("[INFO] [Check connect]", "Перевірка підключення....");

                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    listener.setLogs("[INFO] [Check connect]", "Підключення втрачено, запуск перепідключення.");
                    stopConnectionChecker(); // Зупинка поточного перевіряча
                    connectToServer();       // Запуск перепідключення
                }
                if (!connectUSER) {
                    registerUser();
                }
                // Перевірка статусу підключення
                if (statusCONNECT) {
                    checkCONNECT = 0;
                } else {
                    checkCONNECT++;
                }
                if (checkCONNECT >= 2) {
                    listener.setLogs("[INFO] [Check connect]", "Сервер не відповідає, запуск перепідключення.");
                    connectUSER = false;
                    stopConnectionChecker(); // Зупинка поточного перевіряча
                    connectToServer(); // Запуск перепідключення
                }
                sendData(new Envelope(userId, userId, "ping", "ping").toJson().toString());
                statusCONNECT = false;
            } catch (Exception e) {
                listener.setLogs("[ERROR] [Check connect]", "Помилка перевірки з'єднання - " + e);
            }
        }, 0, 5, TimeUnit.SECONDS); // Перевірка кожні 5 секунд
    }


    // Реєстрація користувача
    private void registerUser() {
        sendData("{\"userId\":\"" + this.userId + "\"}");
        connectUSER = true;
    }

    // Надсилання даних на сервер
    public void sendData(String data) {
        if (!senderExecutor.isShutdown() && !senderExecutor.isTerminated()) {
            senderExecutor.execute(() -> {
                if (output != null && !socket.isClosed() && socket.isConnected()) {
                    output.println(data);
                } else {
                    listener.setLogs("[INFO] [Register user]", "Неможливо надіслати дані, сокет закритий.");
                }
            });
        } else {
            listener.setLogs("[INFO] [Register user]", "Sender Executor вимкнено. Неможливо надіслати дані.");
        }

    }

    // Читання даних з сервера
    public void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                if (message != null) {
                    if (!message.startsWith("{") && !message.endsWith("}")) {
                        if (message.contains("USER_CONNECT")) {
                            listener.setLogs("[INFO] [Check connect]", "Сервер на зв'язку....");
                            statusCONNECT = true;
                        }
                        if (message.contains("ID_NOT_CORRECT")) {
                            if (userId != null && !userId.equals("null")) {
                                listener.setLogs("[INFO] [Register user]", "Помилка авторизації на сервері. Виконуємо наступну спробу");

                                registerUser();
                            }

                        } else {
                            System.out.println(message);
                        }
                    } else {
                        if (!listener.getReceiverId().isEmpty() && receiverId == null) {
                            receiverId = listener.getReceiverId();
                        }
                        JSONObject object = new JSONObject(message);
                        Envelope envelope = new Envelope(object);

                        listener.setLogs("[INFO] [Check receiver]", "Відправник " + receiverId);

                        if (userId.equals(envelope.getSenderId()) && receiverId.equals(envelope.getReceiverId())) {
                            listener.onMessageReceived(message);
                            Thread.sleep(100); // 100 мс
                        } else if (receiverId != null && receiverId.equals(envelope.getSenderId())) {
                            listener.onMessageReceived(message);
                            Thread.sleep(100); // 100 мс
                        } else {
                            listener.saveMessage(envelope);
                        }
                    }
                }
            }
        } catch (IOException e) {
            listener.setLogs("[ERROR] [Listen messages]", " Потік читання закрито або виникла помилка - " + e);

        } catch (JSONException | InterruptedException e) {
            listener.setLogs("[ERROR] [Listen messages]", " Помилка обробки JSON під час отримання повідомлення - " + e.getMessage());

        }
    }


    private void stopSCHEDULI() {
        scheduler.shutdown();// Завершить роботу з усіма активними потоками в пулі
        try {
            if (!scheduler.awaitTermination(6, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();  // Якщо потоки не завершились, примусово завершити
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void stopCONNECT() {
        connect.shutdown();  // Завершить роботу з усіма активними потоками в пулі
        try {
            if (!connect.awaitTermination(60, TimeUnit.SECONDS)) {
                connect.shutdownNow();  // Якщо потоки не завершились, примусово завершити
            }
        } catch (InterruptedException e) {
            connect.shutdownNow();
        }
    }

    private void stopLISTENER() {
        if (listenerExecutor != null) {
            listenerExecutor.shutdownNow(); // Завершуємо ексекутор для слухача
        }
    }

    private void stopSENDER() {
        if (senderExecutor != null) {
            senderExecutor.shutdownNow(); // Завершуємо ексекутор для слухача
        }
    }

    private void closeConnections() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            listener.setLogs("[ERROR] [Close connections]", "Помилка закриття потоків - " + e);
        }
    }

    // Зупинка перевірки підключення
    public void stopConnectionChecker() {
        stopSCHEDULI();
        stopCONNECT();
        stopLISTENER();
        stopSENDER();
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            listener.setLogs("[ERROR] [Close connections]", "Помилка закриття потоків - " + e);
        }
    }

    public void sendHandshake(String userId, String receiverId, String operation, String nameKey, String key) {
        String keyMessage = "{\"" + nameKey + "\": \"" + key + "\" }";
        sendData(new Envelope(userId, receiverId, operation, keyMessage).toJson().toString());
    }

    public String getReceiverId() {
        return receiverId;
    }

    // Інтерфейс для сповіщення про стан підключення
    public interface ConnectionListener {
        void onConnected();

        void onMessageReceived(String message);

        void saveMessage(Envelope envelope);

        void setLogs(String clas, String log);

        String getReceiverId();
    }
}
