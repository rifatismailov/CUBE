package com.example.cube.socket;

import static java.lang.Thread.sleep;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Connector {
    private final String SERVER_IP;  // IP сервера
    private final int SERVER_PORT;  // Порт сервера
    private String PING_COMMAND;
    private String REGISTRATION_COMMAND;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private ScheduledExecutorService scheduler;
    private ExecutorService connect; // Окремий ексекутор для підклюкача
    private ExecutorService listenerExecutor;  // Окремий ексекутор для слухача
    private ExecutorService senderExecutor;  // Окремий ексекутор для слухача
    private final List<String> saveData = new ArrayList<>();

    private final Listener listener;
    private String userId;
    private boolean connectUSER = false;
    private boolean statusCONNECT = false;
    private int checkCONNECT = 0;

    public Connector(Listener listener, String SERVER_IP, int SERVER_PORT) {
        this.listener = listener;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public void setREGISTRATION_COMMAND(String REGISTRATION_COMMAND) {
        this.REGISTRATION_COMMAND = REGISTRATION_COMMAND;
    }

    public void setPING_COMMAND(String PING_COMMAND) {
        this.PING_COMMAND = PING_COMMAND;
    }

    //Якщо кілька потоків можуть змінювати receiverId, використовуйте synchronized або додайте механізм синхронізації:
    public synchronized void setUserId(String userId) {
        this.userId = userId;
    }

    // Метод підключення до сервера
    public void connectToServer() {
        connect = Executors.newFixedThreadPool(1);
        senderExecutor = Executors.newFixedThreadPool(1);
        connect.execute(() -> {
            while (true) {
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    // Реєстрація користувача знову після підключення
                    registration();
                    checkCONNECT = 0;
                    statusCONNECT = false;
                    // Відновлення перевірки з'єднання
                    checker();
                    // **Важливо:** Перезапустити слухання повідомлень з новим потоком
                    listenerExecutor = Executors.newFixedThreadPool(1);
                    listenerExecutor.execute(this::listenForMessages);

                    if (!saveData.isEmpty()) {
                        for (String sData : saveData) {
                            sendData(sData);
                            saveData.remove(sData);
                        }
                    }
                    break; // Вихід з циклу при успішному підключенні
                } catch (IOException e) {
                    listener.setLogs("[ERROR]", "Не вдалося пере підключитися, спроба знову через 5 секунд.");
                    sleeper();
                }
            }
        });
    }

    private void sleeper() {
        try {
            sleep(5000); // Очікування 5 секунд перед повторною спробою
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    // Метод для перевірки підключення
    private void checker() {
        listener.setLogs("[INFO]", "Запуск перевірки....");
        if (scheduler != null) {
            scheduler.shutdownNow(); // Завершити старий екземпляр
        }
        scheduler = Executors.newScheduledThreadPool(1); // Створити новий екземпляр

        scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    stopConnectionChecker(); // Зупинка поточного перевіряча
                    connectToServer();       // Запуск перепідключення
                }
                if (!connectUSER) {
                    registration();
                }
                // Перевірка статусу підключення
                if (statusCONNECT) {
                    checkCONNECT = 0;
                } else {
                    checkCONNECT++;
                }
                if (checkCONNECT >= 2) {
                    connectUSER = false;
                    stopConnectionChecker(); // Зупинка поточного перевіряча
                    connectToServer(); // Запуск перепідключення
                }
                ping();
                statusCONNECT = false;
            } catch (Exception e) {
                Log.e("CONNECTOR","Помилка під час перевірки "+e);
            }
        }, 0, 5, TimeUnit.SECONDS); // Перевірка кожні 5 секунд
    }


    // Реєстрація користувача
    private void registration() {
        sendData(REGISTRATION_COMMAND);
        connectUSER = true;
    }

    private void ping() {
        sendData(PING_COMMAND);
    }

    // Надсилання даних на сервер
    public synchronized void sendData(String data) {
        if (!senderExecutor.isShutdown() && !senderExecutor.isTerminated()) {
            senderExecutor.execute(() -> {
                if (output != null && !socket.isClosed() && socket.isConnected()) {
                    if (!saveData.isEmpty()) {
                        for (String sData : saveData) {
                            output.println(sData);
                            saveData.remove(sData);
                        }
                        output.println(data);
                    } else {
                        output.println(data);
                    }
                } else {
                    listener.setLogs("[INFO]", "Неможливо надіслати дані, сокет закритий.");
                }
            });
        } else {
            listener.setLogs("[INFO]", "Неможливо надіслати дані.");
            //якщо в нас не вдала відправка повідомлення зберагємо їх
            saveData.add(data);
            //зупиняємо потік
            stopSENDER();
            //запускаємо потік
            senderExecutor = Executors.newFixedThreadPool(1);
            // повторна передача повідомлень
            if (!saveData.isEmpty()) {
                for (String sData : saveData) {
                    sendData(sData);
                    saveData.remove(sData);
                }
            }
        }

    }

    // Читання даних з сервера
    public void listenForMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                if (!message.startsWith("{") && !message.endsWith("}")) {
                    if (message.contains("CONNECT_SUCCESSFUL")) {
                        listener.setLogs("[INFO]", "Сервер на зв'язку....");
                        statusCONNECT = true;
                    } else if (message.contains("CONNECT__FAILED")) {
                        if (userId != null && !userId.equals("null")) {
                            listener.setLogs("[INFO]", "Помилка підключення до сервері. Виконуємо наступну спробу");

                        }
                    } else if (message.contains("REGISTRATION_FAILED")) {
                        if (userId != null && !userId.equals("null")) {
                            listener.setLogs("[INFO]", "Помилка авторизації на сервері. Виконуємо наступну спробу");
                            registration();
                        }
                    }
                } else {
                    listener.onListener(message);
                }
            }
        } catch (IOException e) {
            Log.e("[ERROR] [Listen messages]", " Потік читання закрито або виникла помилка - " + e);
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
            Log.e("[ERROR] [Close connections]", "Помилка закриття потоків - " + e);
        }
    }

    // Інтерфейс для сповіщення про стан підключення
    public interface Listener {
        void onListener(String message);

        void setLogs(String clas, String log);
    }
}
