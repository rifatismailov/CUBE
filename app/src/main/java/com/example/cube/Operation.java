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
        Log.e("MainActivity", "R message: " + message);

        try {
            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            String sender = envelope.toJson().getString(FIELD.SENDER_ID.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());

            Log.e("MainActivity", "R message: " + envelope.toJson());

            Log.e("MainActivity", "R message: " + operation);

            // Обробка різних операцій на основі отриманого повідомлення
            if (FIELD.HANDSHAKE.getFIELD().equals(operation)) {
                operable.addHandshake(envelope);
                operable.addMessage(message);
            } else if (FIELD.KEY_EXCHANGE.getFIELD().equals(operation)) {
                operable.addAESKey(sender, receivedMessage);
                operable.addMessage(message);
            } else if (FIELD.STATUS_MESSAGE.getFIELD().equals(operation)) {
                operable.addMessage(message); // Обробка статусу повідомлень
            } else if (FIELD.GET_AVATAR.getFIELD().equals(operation)) {
                operable.giveAvatar(sender);  // Обробка запиту на отримання зображення аккаунту
            } else if (FIELD.AVATAR.getFIELD().equals(operation)) {
                operable.getAvatar(envelope); // Обробка запрасованих зображення контакту
            } else if (FIELD.AVATAR_ORG.getFIELD().equals(operation)) {
                operable.getAvatarORG(envelope); // Обробка запрасованих зображення контакту
            } else {
                operable.addMessage(message); // Обробка  повідомлення

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
     */
    public void openSaveMessage(String receiverId) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            HashMap<String, Envelope> messages = messageManager.getMessagesByReceiverId(receiverId);
            Iterator<Map.Entry<String, Envelope>> iterator = messages.entrySet().iterator(); // ОТРИМУЄМО ІТЕРАТОР


            while (iterator.hasNext()) {
                Map.Entry<String, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (receiverId.equals(envelope.getSenderId())) {
                    operable.addMessage(envelope.toJson().toString());

//                    String operation = envelope.getOperation();
//                    if (FIELD.MESSAGE.getFIELD().equals(operation)) {
//                        operable.addMessage(envelope.toJson().toString());
//                    }
//                    if (FIELD.FILE.getFIELD().equals(operation)) {
//                        operable.addMessage(envelope.toJson().toString());
//                    }
                    iterator.remove(); // Видаляємо елемент після обробки
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
            if (FIELD.MESSAGE.getFIELD().equals(operation) || FIELD.FILE.getFIELD().equals(operation)) {
                saveMessage.put(envelope.getMessageId(), envelope);
                messageManager.setMessage(envelope, envelope.getTime());
                for (ContactData user : userList) {
                    // Оновлюємо кількість повідомлень для користувача
                    if (user.getId().equals(envelope.getSenderId())) {
                        int messageCount = messageManager.getMessageCountBySenderAndOperation(user.getId(), FIELD.MESSAGE.getFIELD()) +
                                messageManager.getMessageCountBySenderAndOperation(user.getId(), FIELD.FILE.getFIELD());

                        if (messageCount == 0) {
                            user.setMessageSize("");  // Оновлюємо messageSize
                        } else {
                            user.setMessageSize("" + messageCount);
                        }
                        break;  // Вихід після оновлення користувача
                    }
                }
                // Оновлюємо адаптер для відображення змін у списку користувачів
                operable.updateAdapter();
            } else if (FIELD.HANDSHAKE.getFIELD().equals(operation)) {
                operable.addHandshake(envelope); // Обробка отримання   ключа
            } else if (FIELD.KEY_EXCHANGE.getFIELD().equals(operation)) {
                operable.addAESKey(envelope.getSenderId(), message); // Обробка  отримання ключа у повідомленні
            } else if (FIELD.STATUS_MESSAGE.getFIELD().equals(operation)) {
                saveMessage.put(envelope.getMessageId(), envelope); // Обробка  статусу повідомлення
                messageManager.setMessage(envelope, envelope.getTime());
//                Envelope saveEnvelope = messageManager.getMessageById(envelope.getMessageId());
//                if (saveEnvelope != null) {
//                    if ("ready".equals(envelope.getMessageStatus())) {
////                        if("ready".equals(saveEnvelope.getMessageStatus())){
////                            messageManager.setMessage(envelope, envelope.getTime());
////                        }else {
////                            saveEnvelope.setMessageStatus(envelope.getMessageStatus());
////                            messageManager.setMessage(saveEnvelope, saveEnvelope.getTime());
////                            Log.e("IOService", "Operation JSON message: " + envelope.toJson());
////                        }
//                        saveEnvelope.setMessageStatus(envelope.getMessageStatus());
//                        messageManager.setMessage(saveEnvelope, saveEnvelope.getTime());
//                    }
//                } else {
//                    saveMessage.put(envelope.getMessageId(), envelope); // Обробка  статусу повідомлення
//                    messageManager.setMessage(envelope, envelope.getTime());
//                }
            } else if (FIELD.GET_AVATAR.getFIELD().equals(operation)) {
                operable.giveAvatar(envelope.getSenderId());  // Обробка запиту на отримання зображення аккаунту
            } else if (FIELD.AVATAR.getFIELD().equals(operation)) {
                operable.getAvatar(envelope); // Обробка запрасованих зображення контакту
            } else if (FIELD.AVATAR_ORG.getFIELD().equals(operation)) {
                operable.getAvatarORG(envelope); // Обробка запрасованих зображення контакту
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод який формує звіт отримання повідомлення
     *
     * @param envelope отримане повідомлення
     */
    public void setMessageStatus(Envelope envelope) {
        String messageJson = new Envelope.Builder().
                setSenderId(envelope.getReceiverId()).
                setReceiverId(envelope.getSenderId()).
                setOperation("messageStatus").
                setMessageStatus("delivered_to_user").
                setMessageId(envelope.getMessageId()).
                build().
                toJson("senderId", "receiverId", "operation", "messageStatus", "messageId").
                toString();
        operable.setMessage(messageJson);
    }

    public String getTime() {
        Date currentDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Формат дати і часу
        return formatter.format(currentDate);
    }

    /**
     * Інтерфейс для взаємодії з іншими компонентами, такими як UI та адаптери.
     * Використовується для додавання повідомлень, хендшейків (RSA ключів), обміну AES-ключами та оновлення адаптерів та інше.
     */
    public interface Operable {
        void setMessage(String message);

        void addMessage(String message);

        void giveAvatar(String sender);

        void getAvatar(Envelope envelope);

        void getAvatarORG(Envelope envelope);

        void addAESKey(String sender, String receivedMessage);

        void addHandshake(Envelope envelope);

        void updateAdapter();
    }
}
