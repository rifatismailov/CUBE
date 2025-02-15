package com.example.cube;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.cube.contact.ContactData;
import com.example.cube.control.FIELD;
import com.example.cube.db.MessageMainManager;
import com.example.web_socket_service.socket.Envelope;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Клас для обробки операцій з повідомленнями, таких як отримання, збереження та відкриття повідомлень.
 * Цей клас взаємодіє з інтерфейсами для оновлення UI та збереження отриманих повідомлень.
 */
public class Operation {
    private final Operable operable;
    private final MessageMainManager messageManager;

    /**
     * Конструктор класу, що приймає об'єкт, який реалізує інтерфейс {@link Operable}.
     * Цей об'єкт використовується для додавання повідомлень та оновлення адаптера UI.
     *
     * @param operable Об'єкт, що реалізує інтерфейс {@link Operable}.
     */
    public Operation(Operable operable, MessageMainManager messageManager) {
        this.operable = operable;
        this.messageManager = messageManager;
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
            if (operation.equals(FIELD.HANDSHAKE.getFIELD())) {
                operable.addHandshake(envelope);
                operable.addMessage(message);
            } else if (operation.equals(FIELD.KEY_EXCHANGE.getFIELD())) {
                operable.addAESKey(sender, receivedMessage);
                operable.addMessage(message);
            } else if (operation.equals(FIELD.STATUS_MESSAGE.getFIELD())) {
                // Обробка статусних повідомлень
                operable.addMessage(message);
            } else if (operation.equals(FIELD.GET_AVATAR.getFIELD())) {
                // Обробка статусних повідомлень
                operable.giveAvatar(sender);
            } else if (operation.equals(FIELD.AVATAR.getFIELD())) {
                // Обробка статусних повідомлень
                operable.getAvatar(envelope);
            } else if (operation.equals(FIELD.AVATAR_ORG.getFIELD())) {
                // Обробка статусних повідомлень
                operable.getAvatarORG(envelope);
            } else {
                operable.addMessage(message);
            }
        } catch (JSONException e) {
            Log.e("Operation", "Помилка під час отримання JSON у методі onReceived: " + e);
        }
    }

    /**
     * Відкриває збережені повідомлення після певної затримки, щоб дозволити активності чату ініціалізуватися.
     * Метод використовує Handler для відкладеного виконання на основному потоці.
     *
     * @param receiverId  Ідентифікатор отримувача повідомлення.
     */
    public void openSaveMessage(String receiverId) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            HashMap<String, Envelope> messages = messageManager.getMessagesByReceiverId(receiverId);
            Iterator<Map.Entry<String, Envelope>> iterator = messages.entrySet().iterator(); // ОТРИМУЄМО ІТЕРАТОР

            while (iterator.hasNext()) {
                Map.Entry<String, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (envelope.getSenderId().equals(receiverId)) {
                    try {
                        String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
                        if (operation.equals(FIELD.MESSAGE.getFIELD()) || operation.equals(FIELD.FILE.getFIELD())) {
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
     * Відкриває збережені повідомлення після певної затримки, щоб дозволити активності чату ініціалізуватися.
     * Метод використовує Handler для відкладеного виконання на основному потоці.
     *
     * @param receiverId  Ідентифікатор отримувача повідомлення.
     * @param saveMessage Словник збережених повідомлень.
     */
    public void openSaveMessage(String receiverId, HashMap<String, Envelope> saveMessage) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Iterator<Map.Entry<String, Envelope>> iterator = saveMessage.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (envelope.getSenderId().equals(receiverId)) {
                    try {
                        String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
                        if (operation.equals(FIELD.MESSAGE.getFIELD()) || operation.equals(FIELD.FILE.getFIELD())) {
                            operable.addMessage(envelope.toJson().toString());
                        }
                      //  iterator.remove(); // Видаляємо елемент після обробки
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
     * @param envelope    Дані повідомлення, яке потрібно зберегти.
     * @param saveMessage Словник для зберігання повідомлень.
     * @param userList    Список користувачів для оновлення.
     * @return Оновлений лічильник номерів повідомлень.
     */
    public void saveMessage(Envelope envelope, HashMap<String, Envelope> saveMessage, List<ContactData> userList) {
        try {
            String message = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            if (operation.equals(FIELD.MESSAGE.getFIELD()) || operation.equals(FIELD.FILE.getFIELD())) {
                saveMessage.put(envelope.getMessageId(), envelope);
                messageManager.setMessage(envelope,getTime());
                for (ContactData user : userList) {
                    // Оновлюємо кількість повідомлень для користувача
                    if (user.getId().equals(envelope.getSenderId())) {
                        int messageCount = messageManager.getMessageCountBySenderAndOperation(user.getId(), FIELD.MESSAGE.getFIELD()) +
                                messageManager.getMessageCountBySenderAndOperation(user.getId(), FIELD.FILE.getFIELD());

                        if (messageCount == 0) {
                            user.setMessageSize("");  // Оновлюємо messageSize
                        } else {
                            user.setMessageSize("" + messageCount);
                            Log.e("Operation", "Count Save Message: " + messageCount);

                        }
                        break;  // Вихід після оновлення користувача
                    }
                }
                // Оновлюємо адаптер для відображення змін у списку користувачів
                operable.updateAdapter();
            } else if (operation.equals(FIELD.HANDSHAKE.getFIELD())) {
                operable.addHandshake(envelope);
            } else if (operation.equals(FIELD.KEY_EXCHANGE.getFIELD())) {
                operable.addAESKey(envelope.getSenderId(), message);
            } else if (operation.equals(FIELD.STATUS_MESSAGE.getFIELD())) {
                //розкодувати коли реалізуємо збереження повідомлень у директорії
                // saveMessage.put(numMessage, envelope);
                // numMessage++;
                // Обробка статусних повідомлень
//                String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
//                String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());
                saveMessage.put(envelope.getMessageId(), envelope);
                messageManager.setMessage(envelope,getTime());

            } else if (operation.equals(FIELD.GET_AVATAR.getFIELD())) {
                // Обробка статусних повідомлень
                operable.giveAvatar(envelope.getSenderId());
            } else if (operation.equals(FIELD.AVATAR.getFIELD())) {
                // Обробка статусних повідомлень
                operable.getAvatar(envelope);

            } else if (operation.equals(FIELD.AVATAR_ORG.getFIELD())) {
                // Обробка статусних повідомлень
                operable.getAvatarORG(envelope);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Зберігає отримане повідомлення у словнику та оновлює кількість повідомлень для відповідного користувача.
     * Також оновлюється адаптер для відображення змін у списку користувачів.
     *
     * @param envelope    Дані повідомлення, яке потрібно зберегти.
     * @param saveMessage Словник для зберігання повідомлень.
     * @param numMessage  Лічильник номерів повідомлень.
     * @param userList    Список користувачів для оновлення.
     * @return Оновлений лічильник номерів повідомлень.
     */
    public int saveMessage(Envelope envelope, HashMap<Integer, Envelope> saveMessage, int numMessage, List<ContactData> userList) {
        try {
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            if (operation.equals(FIELD.MESSAGE.getFIELD()) || operation.equals(FIELD.FILE.getFIELD())) {
                Envelope saveEnvelope = saveMessage.get(numMessage);
                if (saveEnvelope != null) {
                    System.out.println("Повідомлення: " + envelope.getMessage());
                } else {
                    System.out.println("Об'єкт не знайдено");
                }
                saveMessage.put(numMessage, envelope);
                numMessage++;
                for (ContactData user : userList) {
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
                //розкодувати коли реалізуємо збереження повідомлень у директорії
                // saveMessage.put(numMessage, envelope);
                // numMessage++;
                // Обробка статусних повідомлень
                String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
                String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());
            } else if (operation.equals(FIELD.GET_AVATAR.getFIELD())) {
                // Обробка статусних повідомлень
                operable.giveAvatar(envelope.getSenderId());
            } else if (operation.equals(FIELD.AVATAR.getFIELD())) {
                // Обробка статусних повідомлень
                operable.getAvatar(envelope);

            } else if (operation.equals(FIELD.AVATAR_ORG.getFIELD())) {
                // Обробка статусних повідомлень
                operable.getAvatarORG(envelope);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return numMessage;
    }

    private String getTime() {
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Формат дати і часу
        return formatter.format(currentDate);
    }
    /**
     * Інтерфейс для взаємодії з іншими компонентами, такими як UI та адаптери.
     * Використовується для додавання повідомлень, хендшейків, обміну AES-ключами та оновлення адаптерів.
     */
    public interface Operable {
        void addMessage(String message);

        void giveAvatar(String sender);

        void getAvatar(Envelope envelope);

        void getAvatarORG(Envelope envelope);

        void addAESKey(String sender, String receivedMessage);

        void addHandshake(Envelope envelope);

        void updateAdapter();
    }
}
