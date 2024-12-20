package com.example.cube.chat;

import android.net.Uri;
import android.util.Log;

import com.example.cube.chat.message.Message;
import com.example.cube.control.FIELD;
import com.example.cube.control.Side;
import com.example.cube.encryption.Encryption;
import com.example.cube.socket.Envelope;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class OperationMSG {
    OperableMSG operableMSG;

    /**
     * Конструктор класу, що приймає об'єкт, який реалізує інтерфейс {@link OperableMSG}.
     * Цей об'єкт використовується для додавання повідомлень та оновлення адаптера UI.
     *
     * @param operableMSG Об'єкт, що реалізує інтерфейс {@link OperableMSG}.
     */
    public OperationMSG(OperableMSG operableMSG) {
        this.operableMSG = operableMSG;
    }

    /**
     * Обробляє отримані повідомлення та виконують відповідні операції в залежності від типу повідомлення.
     * Різні типи операцій (повідомлення, обмін ключами, хендшейк) обробляються відповідно.
     *
     * @param data Повідомлення у форматі JSON, яке потрібно обробити.
     */
    public void onReceived(String senderKey,String data) {
        try {
            JSONObject object = new JSONObject(data);
            Envelope envelope = new Envelope(object);
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());

            // Обробляємо дані від Activity, наприклад, оновлюємо UI
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                if (envelope.getFileUrl() == null) {
                    operableMSG.readMessage(new Message(rMessage, Side.Receiver,messageID));
                } else {
                    Message message = new Message(rMessage, Uri.parse(envelope.getFileUrl()), Side.Receiver,messageID);
                    message.setHas(envelope.getFileHash());
                    operableMSG.readMessageFile(message);
                }
                //operableMSG.addMessage(messageID, envelope.getMessage());
            } else if (operation.equals(FIELD.HANDSHAKE.getFIELD())) {
                JSONObject jsonObject = new JSONObject(envelope.getMessage());
                // чому getString("publicKey"); тому що відправник вказує в повідомлення метрику як publicKey
                // тим вказує що це його публічний кул так ми його забираємо
                String rPublicKey = jsonObject.getString(FIELD.PUBLIC_KEY.getFIELD());

                operableMSG.addReceiverPublicKey(rPublicKey);
            } else if (operation.equals(FIELD.KEY_EXCHANGE.getFIELD())) {
                JSONObject jsonObject = new JSONObject(envelope.getMessage());
                String aesKey = jsonObject.getString(FIELD.AES_KEY.getFIELD());
                operableMSG.addReceiverKey(aesKey);
            } else if (operation.equals(FIELD.STATUS_MESSAGE.getFIELD())) {
                // Обробка статусних повідомлень
                String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
                operableMSG.addNotifier(messageID, status);
                //Toast.makeText(this, messageID, Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Log.e("OperationMSG", "Помилка під час отримання JSON: " + e);


        } catch (Exception e) {
            Log.e("OperationMSG", "Помилка під час отримання даних : " + e);
        }
    }
    public void onSend(String senderId,String receiverId,String message,String messageId,String receiverKey){
        try {
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            Envelope envelope = new Envelope(senderId, receiverId, FIELD.MESSAGE.getFIELD(), rMessage, messageId);
            //реалізація шифрування повідомлення
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        }catch (Exception e){

        }

    }
    public void onSendFile(String senderId,String receiverId,String message,String url,String has,String receiverKey,String messageId){
        try {
            String urls = "http://192.168.1.237/api/files/download/" + new File(url).getName(); // Змініть IP на ваш
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            String rURL = Encryption.AES.encrypt(urls, receiverKey);
            String rHAS = Encryption.AES.encrypt(has, receiverKey);
            Envelope envelope = new Envelope(senderId, receiverId, "file", rMessage, rURL, rHAS, messageId);
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        }catch (Exception e){

        }
    }
    /**
     * Інтерфейс для взаємодії з іншими компонентами, такими як UI та адаптери.
     * Використовується для додавання повідомлень, хендшейків, обміну AES-ключами та оновлення адаптерів.
     */
    public interface OperableMSG {
        void readMessage(Message message);
        void readMessageFile(Message message);

        void addReceiverPublicKey(String rPublicKey) throws Exception;

        void addReceiverKey(String receiverKey) throws Exception;

        void addNotifier(String messageID, String status);
        void sendDataBackToActivity(String message);
    }
}
