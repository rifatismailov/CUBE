package com.example.cube;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.cube.contact.UserData;
import com.example.cube.control.FIELD;
import com.example.cube.socket.Envelope;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Клас для обробки операцій з повідомленнями, таких як отримання, збереження та відкриття повідомлень.
 * Цей клас взаємодіє з інтерфейсами для оновлення UI та збереження отриманих повідомлень.
 */
public class Operation {
    Operable operable;

    /**
     * Конструктор класу, що приймає об'єкт, який реалізує інтерфейс {@link Operable}.
     * Цей об'єкт використовується для додавання повідомлень та оновлення адаптера UI.
     *
     * @param operable Об'єкт, що реалізує інтерфейс {@link Operable}.
     */
    public Operation(Operable operable) {
        this.operable = operable;
    }

    /**
     * Обробляє отримані повідомлення та виконують відповідні операції в залежності від типу повідомлення.
     * Різні типи операцій (повідомлення, обмін ключами, хендшейк) обробляються відповідно.
     *
     * @param message Повідомлення у форматі JSON, яке потрібно обробити.
     */
    public void onReceived(String message) {
        try {
            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            String sender = envelope.toJson().getString(FIELD.SENDER_ID.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());

            // Обробка різних операцій на основі отриманого повідомлення
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                operable.addMessage(message);
            } else if (operation.equals(FIELD.HANDSHAKE.getFIELD())) {
                operable.addHandshake(envelope);
                operable.addMessage(message);
            } else if (operation.equals(FIELD.KEY_EXCHANGE.getFIELD())) {
                operable.addAESKey(sender, receivedMessage);
                operable.addMessage(message);
            } else if (operation.equals(FIELD.STATUS_MESSAGE.getFIELD())) {
                // Обробка статусних повідомлень
                operable.addMessage(message);
                //String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
               // String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());
                //Log.e("Operation", "Отримано STATUS_MESSAGE [" + sender + "]: " + status + " [ " + messageID + "]" );
            }
        } catch (JSONException e) {
            Log.e("Operation", "Помилка під час отримання JSON у методі onReceived: " + e);
        }
    }

    /**
     * Відкриває збережені повідомлення після певної затримки, щоб дозволити активності чату ініціалізуватися.
     * Метод використовує Handler для відкладеного виконання на основному потоці.
     *
     * @param receiverId Ідентифікатор отримувача повідомлення.
     * @param saveMessage Словник збережених повідомлень.
     */
    public void openSaveMessage(String receiverId, HashMap<Integer, Envelope> saveMessage) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Iterator<Map.Entry<Integer, Envelope>> iterator = saveMessage.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (envelope.getSenderId().equals(receiverId)) {
                    try {
                        String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
                        if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                            operable.addMessage(envelope.toJson().toString());
                        }
                        iterator.remove(); // Видаляємо елемент після обробки
                    } catch (JSONException e) {
                        Log.e("Operation", "Помилка під час отримання JSON у методі openSaveMessage: " + e);
                    }
                }
            }
        }, 1000);  // Затримка 1 секунда
    }

    /**
     * Зберігає отримане повідомлення у словнику та оновлює кількість повідомлень для відповідного користувача.
     * Також оновлюється адаптер для відображення змін у списку користувачів.
     *
     * @param envelope Дані повідомлення, яке потрібно зберегти.
     * @param saveMessage Словник для зберігання повідомлень.
     * @param numMessage Лічильник номерів повідомлень.
     * @param userList Список користувачів для оновлення.
     * @return Оновлений лічильник номерів повідомлень.
     */
    public int saveMessage(Envelope envelope, HashMap<Integer, Envelope> saveMessage, int numMessage, List<UserData> userList) {
        try {
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                saveMessage.put(numMessage, envelope);
                numMessage++;
                for (UserData user : userList) {
                    // Оновлюємо кількість повідомлень для користувача
                    if (user.getId().equals(envelope.getSenderId())) {
                        user.setSize(user.getSize() + 1);
                        user.setMessageSize("" + user.getSize());  // Оновлюємо messageSize
                        break;  // Вихід після оновлення користувача
                    }
                }
                // Оновлюємо адаптер для відображення змін у списку користувачів
                operable.updateAdapter();
            } else if (operation.equals(FIELD.HANDSHAKE.getFIELD())) {
                operable.addHandshake(envelope);
            } else if (operation.equals(FIELD.KEY_EXCHANGE.getFIELD())) {
                operable.addAESKey(envelope.getSenderId(), receivedMessage);
            } else if (operation.equals(FIELD.STATUS_MESSAGE.getFIELD())) {
                // Обробка статусних повідомлень
                String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
                String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return numMessage;
    }

    /**
     * Інтерфейс для взаємодії з іншими компонентами, такими як UI та адаптери.
     * Використовується для додавання повідомлень, хендшейків, обміну AES-ключами та оновлення адаптерів.
     */
    public interface Operable {
        void addMessage(String message);

        void addHandshake(Envelope envelope);

        void addAESKey(String sender, String receivedMessage);

        void updateAdapter();
    }
}
