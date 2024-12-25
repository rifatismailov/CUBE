package com.example.cube.socket;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Клас для роботи з серверним підключенням.
 * Використовується для надсилання, отримання даних та перевірки стану підключення.
 * З'єднується з сервером, обробляє отримані повідомлення та виконує відправку даних.
 */
public class Listener implements Connector.Listener {

    private final Connector connector;
    private final DataListener listener;
    private final String userId;

    /**
     * Конструктор класу Listener.
     * Ініціалізує з'єднання з сервером, реєструє користувача та налаштовує команди.
     *
     * @param listener Об'єкт, який обробляє отримані повідомлення та стан підключення.
     * @param userId Ідентифікатор користувача.
     * @param SERVER_IP IP-адреса сервера.
     * @param SERVER_PORT Порт сервера.
     */
    public Listener(DataListener listener, String userId, String SERVER_IP, int SERVER_PORT) {
        this.listener = listener;
        this.userId = userId;
        connector = new Connector(this, SERVER_IP, SERVER_PORT);
        connector.connectToServer();
        connector.setUserId(userId);
        connector.setREGISTRATION_COMMAND("{\"userId\":\"" + this.userId + "\"}");
        connector.setPING_COMMAND(new Envelope(userId, userId, "ping", "ping", "").toJson().toString());
    }

    /**
     * Надсилає дані на сервер.
     *
     * @param data Дані, які потрібно відправити.
     */
    public synchronized void sendData(String data) {
        connector.sendData(data);
    }

    /**
     * Обробляє отримані повідомлення від сервера.
     * Якщо повідомлення отримано від цього ж користувача або для нього, воно передається в listener.
     * Якщо повідомлення не для цього користувача, воно зберігається.
     *
     * @param message Повідомлення, яке отримано від сервера.
     */
    @Override
    public void onListener(String message) {
        try {
            if (message != null) {
                JSONObject object = new JSONObject(message);
                Envelope envelope = new Envelope(object);
                Log.e("Listener","A "+envelope.toJson().toString());

                // Якщо повідомлення від нас самих (SenderId і ReceiverId однакові).
                if (userId.equals(envelope.getSenderId()) && listener.getReceiverId().equals(envelope.getReceiverId())) {
                    listener.onReceived(message);
                    //Thread.sleep(100); // 100 мс затримка
                    Log.e("Listener","B "+envelope.toJson().toString());


                    // Якщо повідомлення для поточного користувача
                } else if (listener.getReceiverId() != null && listener.getReceiverId().equals(envelope.getSenderId())) {
                    listener.onReceived(message);
                    Log.e("Listener","C "+envelope.toJson().toString());

                    //Thread.sleep(100); // 100 мс затримка

                    // Якщо повідомлення не для поточного користувача, зберігаємо його
                } else {
                    listener.saveMessage(envelope);
                    Log.e("Listener","D "+envelope.toJson().toString());

                }
            }
            //| InterruptedException
        } catch (JSONException  e) {
            listener.setLogs("[ERROR] [Listen messages]", " Помилка обробки JSON під час отримання повідомлення - " + e.getMessage());
        }
    }

    /**
     * Записує лог повідомлення.
     *
     * @param clas Назва класу, що викликає метод.
     * @param log Текст повідомлення для логу.
     */
    @Override
    public void setLogs(String clas, String log) {
        listener.setLogs(clas, log);
    }

    /**
     * Надсилає хендшейк (handshake) повідомлення з даними користувача.
     * Використовується для встановлення безпечного з'єднання.
     *
     * @param userId Ідентифікатор користувача.
     * @param receiverId Ідентифікатор отримувача.
     * @param operation Операція (наприклад, шифрування).
     * @param nameKey Назва ключа.
     * @param key Значення ключа.
     */
    public void sendHandshake(String userId, String receiverId, String operation, String nameKey, String key) {
        String keyMessage = "{\"" + nameKey + "\": \"" + key + "\" }";
        sendData(new Envelope(userId, receiverId, operation, keyMessage, "").toJson().toString());
    }

    /**
     * Інтерфейс для сповіщення про стан підключення.
     * Використовується для отримання повідомлень, збереження повідомлень та запису логів.
     */
    public interface DataListener {

        /**
         * Метод для обробки отриманих повідомлень.
         *
         * @param message Повідомлення, що було отримано.
         */
        void onReceived(String message);

        /**
         * Метод для збереження повідомлень.
         *
         * @param envelope Об'єкт Envelope, що містить повідомлення для збереження.
         */
        void saveMessage(Envelope envelope);

        /**
         * Метод для запису логів.
         *
         * @param clas Назва класу, що викликає метод.
         * @param log Текст повідомлення для логу.
         */
        void setLogs(String clas, String log);

        /**
         * Отримує ідентифікатор отримувача для поточного користувача.
         *
         * @return Ідентифікатор отримувача.
         */
        String getReceiverId();
    }
}
