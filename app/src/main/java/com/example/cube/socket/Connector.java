package com.example.cube.socket;

import static java.lang.Thread.sleep;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Connector {
    private final String SERVER_IP; // IP-адреса сервера
    private final int SERVER_PORT;  // Порт сервера
    private String PING_COMMAND;    // Команда для перевірки зв'язку
    private String REGISTRATION_COMMAND; // Команда для реєстрації на сервері

    private Socket socket;  // Сокет для з'єднання
    private BufferedReader input; // Потік для читання даних
    private PrintWriter output;   // Потік для надсилання даних
    private ScheduledExecutorService scheduler; // Планувальник для перевірки підключення
    private ExecutorService connect;  // Виконавець для обробки підключення
    private ExecutorService listenerExecutor; // Виконавець для слухача повідомлень
    private ExecutorService senderExecutor;   // Виконавець для надсилання повідомлень
    private final List<String> saveData = new ArrayList<>(); // Буфер для збереження даних у разі помилок

    private final Listener listener; // Інтерфейс для сповіщення про стан
    private String userId;           // Ідентифікатор користувача
    private boolean connectUSER = false; // Стан реєстрації користувача
    private boolean statusCONNECT = false; // Стан з'єднання
    private int checkCONNECT = 0;    // Лічильник перевірок з'єднання

    /**
     * Конструктор класу Connector.
     *
     * @param listener    Інтерфейс для обробки подій.
     * @param SERVER_IP   IP-адреса сервера.
     * @param SERVER_PORT Порт сервера.
     */
    public Connector(Listener listener, String SERVER_IP, int SERVER_PORT) {
        this.listener = listener;
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    /**
     * Встановлення команди реєстрації.
     *
     * @param REGISTRATION_COMMAND Команда реєстрації.
     */
    public void setREGISTRATION_COMMAND(String REGISTRATION_COMMAND) {
        this.REGISTRATION_COMMAND = REGISTRATION_COMMAND;
    }

    /**
     * Встановлення команди перевірки з'єднання (PING).
     *
     * @param PING_COMMAND Команда для перевірки зв'язку.
     */
    public void setPING_COMMAND(String PING_COMMAND) {
        this.PING_COMMAND = PING_COMMAND;
    }

    /**
     * Встановлення ідентифікатора користувача.
     *
     * @param userId Ідентифікатор користувача.
     */
    public synchronized void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Підключення до сервера. Автоматично намагається перепідключитися у разі помилки.
     * <p>
     * Опис:
     * Цей метод виконує спробу підключення до сервера за вказаними IP-адресою та портом.
     * Якщо підключення вдається, виконується реєстрація користувача та налаштовується перевірка з'єднання.
     * У разі помилки (наприклад, якщо сервер недоступний), метод автоматично намагається перепідключитися через 5 секунд.
     * <p>
     * Логіка роботи:
     * 1. Створення двох пулів потоків: один для підключення та інший для надсилання даних.
     * 2. Постійна спроба підключитися до сервера в нескінченному циклі, поки підключення не буде успішним.
     * 3. Після успішного підключення ініціалізуються всі необхідні ресурси (вхідний/вихідний потоки, реєстрація користувача, перевірка з'єднання, слухач повідомлень).
     * 4. Якщо є збережені повідомлення, вони відправляються на сервер після встановлення з'єднання.
     * 5. У разі помилки в процесі підключення, здійснюється пауза в 5 секунд і спроба перепідключитися.
     */
    public void connectToServer() {
        connect = Executors.newFixedThreadPool(1); // Створення потоку для підключення
        senderExecutor = Executors.newFixedThreadPool(1); // Потік для надсилання даних

        connect.execute(() -> {
            while (true) {
                try {
                    // Створення нового сокета та підключення до сервера
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Вхідний потік
                    output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true); // Вихідний потік

                    // Реєстрація користувача після підключення
                    registration();
                    checkCONNECT = 0; // Скидання лічильника невдалих підключень
                    statusCONNECT = false; // Статус підключення (false – не підключено)

                    // Відновлення перевірки з'єднання
                    checker();

                    // Перезапуск слухача повідомлень в новому потоці
                    listenerExecutor = Executors.newFixedThreadPool(1);
                    listenerExecutor.execute(this::listener);

                    // Надсилання збережених даних (якщо є) після успішного підключення
                    if (!saveData.isEmpty()) {
                        for (String sData : saveData) {
                            sendData(sData);
                            saveData.remove(sData);
                        }
                    }

                    break; // Вихід з циклу при успішному підключенні
                } catch (IOException e) {
                    // Логування помилки при підключенні
                    listener.setLogs("[ERROR]", "Не вдалося пере підключитися, спроба знову через 5 секунд.");
                    sleeper(); // Затримка перед наступною спробою підключення
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

    /**
     * Перевірка стабільності підключення. Викликає метод `checker`.
     * <p>
     * Опис:
     * Цей метод забезпечує перевірку стабільності підключення до сервера. Він створює новий екземпляр запланованого виконавця `scheduler` для періодичної перевірки статусу з'єднання.
     * У разі виявлення проблем з підключенням, викликається перепідключення до сервера.
     * Якщо підключення стабільне, перевіряється статус користувача та підключення для забезпечення коректної роботи.
     * <p>
     * Логіка роботи:
     * 1. Завершується попередній екземпляр планувальника (якщо він є).
     * 2. Створюється новий планувальник для періодичної перевірки кожні 5 секунд.
     * 3. Перевіряється стан сокета та підключення.
     * 4. У разі необхідності викликаються методи для перепідключення та реєстрації.
     * 5. Проводиться перевірка статусу підключення та кількість невдалих спроб підключення.
     */
    private void checker() {
        listener.setLogs("[INFO]", "Запуск перевірки....");

        // Завершення старого екземпляра планувальника, якщо він є
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        // Створення нового екземпляра планувальника
        scheduler = Executors.newScheduledThreadPool(1);

        // Періодична перевірка кожні 5 секунд
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                // Перевірка стану сокета
                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    stopConnectionChecker(); // Зупинка поточного перевіряча
                    connectToServer();       // Запуск перепідключення
                }

                // Перевірка статусу користувача
                if (!connectUSER) {
                    registration(); // Якщо користувач не підключений, виконується реєстрація
                }

                // Перевірка статусу підключення
                if (statusCONNECT) {
                    checkCONNECT = 0; // Підключення успішне, скидаємо лічильник
                } else {
                    checkCONNECT++; // Підключення не успішне, збільшуємо лічильник
                }

                // Якщо кількість невдалих підключень досягла 2, виконати перепідключення
                if (checkCONNECT >= 2) {
                    connectUSER = false;
                    stopConnectionChecker(); // Зупинка поточного перевіряча
                    connectToServer(); // Запуск перепідключення
                }

                // Пінг для перевірки стабільності підключення
                ping();

                // Скидання статусу підключення
                statusCONNECT = false;
            } catch (Exception e) {
                // Логування помилки при перевірці
                Log.e("CONNECTOR", "Помилка під час перевірки " + e);
            }
        }, 0, 5, TimeUnit.SECONDS); // Перевірка кожні 5 секунд
    }


    /**
     * Реєстрація користувача на сервері.
     */
    private void registration() {
        sendData(REGISTRATION_COMMAND);
        connectUSER = true;
    }

    /**
     * Надсилання команди для перевірки зв'язку (PING).
     */
    private void ping() {
        sendData(PING_COMMAND);
    }

    /**
     * Надсилання даних на сервер.
     *
     * @param data Дані для надсилання.
     *             <p>
     *             Опис:
     *             Метод надсилає дані на сервер через потоковий виконавець `senderExecutor`.
     *             Якщо сокет активний і доступний, дані надсилаються безпосередньо.
     *             У разі невдачі, дані зберігаються у черзі `saveData` для повторного надсилання.
     *             Також здійснюється обробка ситуацій із закритим або недоступним сокетом.
     *             <p>
     *             Логіка роботи:
     *             1. Перевіряє стан виконавця (`senderExecutor`) та сокета (`socket`).
     *             2. Використовує чергу `saveData` для збереження невдалих спроб надсилання.
     *             3. У разі проблем, повторно ініціалізує виконавець та надсилає всі збережені дані.
     */
    public synchronized void sendData(String data) {
        if (!senderExecutor.isShutdown() && !senderExecutor.isTerminated()) {
            senderExecutor.execute(() -> {
                if (output != null && !socket.isClosed() && socket.isConnected()) {
                    // Надсилання збережених даних із черги, якщо є
                    if (!saveData.isEmpty()) {
                        for (String sData : saveData) {
                            output.println(sData);
                        }
                        saveData.clear(); // Очищення черги після успішної відправки
                    }
                    // Надсилання нових даних
                    output.println(data);
                } else {
                    // Логування, якщо сокет недоступний
                    listener.setLogs("[INFO]", "Неможливо надіслати дані, сокет закритий.");
                }
            });
        } else {
            // Логування, якщо виконавець недоступний
            listener.setLogs("[INFO]", "Неможливо надіслати дані.");
            // Збереження даних у чергу
            saveData.add(data);
            // Зупинка існуючого виконавця
            stopSender();
            // Перезапуск виконавця
            senderExecutor = Executors.newFixedThreadPool(1);
            // Повторне надсилання збережених даних
            if (!saveData.isEmpty()) {
                for (String sData : saveData) {
                    sendData(sData);
                }
                saveData.clear(); // Очищення черги після повторної відправки
            }
        }
    }


    /**
     * Прослуховування повідомлень від сервера.
     * Цей метод читає вхідні повідомлення з сервера через потік вводу.
     * Обробляє різні типи повідомлень, такі як підтвердження з'єднання, помилки підключення,
     * або авторизації. Якщо повідомлення є у форматі JSON, передає його до слухача для подальшої обробки.
     * <p>
     * Логіка обробки:
     * 1. Перевіряє, чи повідомлення є JSON.
     * 2. Обробляє текстові повідомлення про статус підключення чи помилки.
     * 3. Передає JSON-повідомлення у метод `onListener` слухача.
     * <p>
     * Обробляє винятки для забезпечення стабільності роботи програми.
     */
    public void listener() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                // Перевірка, чи повідомлення не є JSON
                if (!message.startsWith("{") && !message.endsWith("}")) {
                    if (message.contains("CONNECT_SUCCESSFUL")) {
                        // Сервер підтвердив успішне підключення
                        listener.setLogs("[INFO]", "Сервер на зв'язку....");
                        statusCONNECT = true;
                    } else if (message.contains("CONNECT__FAILED")) {
                        // Сервер не дозволив підключення
                        if (userId != null && !userId.equals("null")) {
                            listener.setLogs("[INFO]", "Помилка підключення до серверу. Виконуємо наступну спробу");
                        }
                    } else if (message.contains("REGISTRATION_FAILED")) {
                        // Сервер відмовив у реєстрації
                        if (userId != null && !userId.equals("null")) {
                            listener.setLogs("[INFO]", "Помилка авторизації на сервері. Виконуємо наступну спробу");
                            registration();
                        }
                    }
                } else {
                    // Передача JSON-повідомлень для подальшої обробки
                    listener.onListener(message);
                }
            }
        } catch (IOException e) {
            Log.e("[ERROR] [Listen messages]", " Потік читання закрито або виникла помилка - " + e);
        }
    }


    private void stopSchedule() {
        scheduler.shutdown();// Завершить роботу з усіма активними потоками в пулі
        try {
            if (!scheduler.awaitTermination(6, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();  // Якщо потоки не завершились, примусово завершити
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void stopConnect() {
        connect.shutdown();  // Завершить роботу з усіма активними потоками в пулі
        try {
            if (!connect.awaitTermination(60, TimeUnit.SECONDS)) {
                connect.shutdownNow();  // Якщо потоки не завершились, примусово завершити
            }
        } catch (InterruptedException e) {
            connect.shutdownNow();
        }
    }

    private void stopListener() {
        if (listenerExecutor != null) {
            listenerExecutor.shutdownNow(); // Завершуємо ексекутор для слухача
        }
    }

    private void stopSender() {
        if (senderExecutor != null) {
            senderExecutor.shutdownNow(); // Завершуємо ексекутор для слухача
        }
    }

    /**
     * Зупинка всіх потоків та закриття з'єднання.
     */
    public void stopConnectionChecker() {
        stopSchedule();
        stopConnect();
        stopListener();
        stopSender();
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            Log.e("[ERROR] [Close connections]", "Помилка закриття потоків - " + e);
        }
    }

    /**
     * Інтерфейс для обробки подій та логів.
     */
    public interface Listener {
        void onListener(String message);

        void setLogs(String clas, String log);
    }
}
