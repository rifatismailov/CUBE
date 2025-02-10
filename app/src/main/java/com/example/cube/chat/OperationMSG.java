package com.example.cube.chat;

import android.net.Uri;
import android.util.Log;

import com.example.cube.chat.message.FileData;
import com.example.cube.chat.message.Message;
import com.example.cube.control.FIELD;
import com.example.cube.control.Side;
import com.example.cube.encryption.Encryption;
import com.example.web_socket_service.socket.Envelope;

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
    public void onReceived(String senderKey, String data) {
        try {
            JSONObject object = new JSONObject(data);
            Envelope envelope = new Envelope(object);
            String operation = envelope.toJson().getString(FIELD.OPERATION.getFIELD());
            String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());
            Log.e("Listener", "operation " + operation);

            // Обробляємо дані від Activity, наприклад, оновлюємо UI
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                if (envelope.getFileUrl() == null) {
                    operableMSG.readMessage(new Message(rMessage, Side.Receiver, messageID));
                }
                //operableMSG.addMessage(messageID, envelope.getMessage());
            } else if (operation.equals(FIELD.IMAGE.getFIELD())) {
            } else if (operation.equals(FIELD.FILE.getFIELD())) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                String fileUrl = Encryption.AES.decrypt(envelope.getFileUrl(), senderKey);
                String fileHash = Encryption.AES.decrypt(envelope.getFileHash(), senderKey);
                FileData fileData = new FileData().convertFilePreview(fileUrl, fileHash);

                Message message = new Message(rMessage, Uri.parse(envelope.getFileUrl()), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Receiver, messageID);
                //Log.e("Listener", "rMessage "+rMessage);
                message.setUrl(Uri.parse(fileUrl));
                message.setHas(fileHash);
                message.setFileName(fileUrl);
                message.setFileSize("100mb");
                message.setTypeFile(FIELD.FILE.getFIELD());
                message.setDataCreate("11.11.11 12.12.12");
                operableMSG.readMessageFile(message);

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

    public void onSend(String senderId, String receiverId, String message, String messageId, String receiverKey) {
        try {
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            Envelope envelope = new Envelope(senderId, receiverId, FIELD.MESSAGE.getFIELD(), rMessage, messageId);
            //реалізація шифрування повідомлення
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        } catch (Exception e) {

        }

    }

    public void onSendFile(String senderId, String receiverId, String message, String url, String has, String receiverKey, String messageId) {
        try {
            String filename = new File(url).getName();
            Log.e("FileEncryption", " filename " + filename);
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            String rURL = Encryption.AES.encrypt(url, receiverKey);
            Log.e("FileEncryption", " rURL " + rURL);

            String rHAS = Encryption.AES.encrypt(has, receiverKey);
            String operation;
//            if (url.endsWith(".jpg") || url.endsWith(".png")) {
//                operation = FIELD.IMAGE.getFIELD();
//            } else {
//                operation = FIELD.FILE.getFIELD();
//            }
            //після шифрування нам не відомо який формат файлу
            operation = FIELD.FILE.getFIELD();
            Envelope envelope = new Envelope(senderId, receiverId, operation, rMessage, rURL, rHAS, messageId);
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        } catch (Exception e) {

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
