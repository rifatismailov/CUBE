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

public class Operation {
    Operable operable;

    public Operation(Operable operable) {
        this.operable = operable;
    }

    public void onReceived(String message) {
        try {
            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            String sender = envelope.toJson().getString(FIELD.SENDER_ID.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                operable.addMessage(message);
            } else if (operation.equals(FIELD.HANDSHAKE.getFIELD())) {
                Log.e("Operation", receivedMessage);
                operable.addHandshake(envelope);
            } else if (operation.equals(FIELD.KEY_EXCHANGE.getFIELD())) {
                operable.addAESKey(sender, receivedMessage);
                Log.e("Operation", "Отримано KEY_EXCHANGE [" + sender + "]: " + receivedMessage);
            }
            // Обробка отриманих повідомлень
        } catch (JSONException e) {
            Log.e("Operation", "Помилка під час отримання JSON у методі onReceived: " + e);
        }
    }

    /**
     * відкриття збережених повідомлень після затримки, щоб активність чату встигла ініціалізуватися.
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
                        iterator.remove(); // Remove the entry only once per iteration

                    } catch (JSONException e) {
                        Log.e("Operation", "Помилка під час отримання JSON у методі openSaveMessage: " + e);
                    }
                }
            }
        }, 1000);  // Відкладення на 1 секунду
    }

    /**
     * Зберігає отримане повідомлення.
     *
     * @param envelope дані повідомлення, яке зберігається.
     */
    public int saveMessage(Envelope envelope, HashMap<Integer, Envelope> saveMessage, int numMessage, List<UserData> userList) {

        try {
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                saveMessage.put(numMessage, envelope);
                numMessage++;
                for (UserData user : userList) {
                    // Знайдіть користувача за його id та оновіть кількість повідомлень
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
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return numMessage;
    }

    public interface Operable {
        void addMessage(String message);

        void addHandshake(Envelope envelope);

        void addAESKey(String sender, String receivedMessage);

        void updateAdapter();
    }
}
