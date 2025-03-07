package com.example.cube.chat;

import android.net.Uri;
import android.util.Log;

import com.example.folder.FileData;
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

            // Обробляємо дані від Activity, наприклад, оновлюємо UI
            if (operation.equals(FIELD.MESSAGE.getFIELD())) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                if (envelope.getFileUrl() == null) {
                    Message message=new Message(rMessage, Side.Receiver, messageID);
                    message.setTimestamp(envelope.getTime());
                    operableMSG.readMessage(message);
                    returnAboutDeliver(message);
                }
            }else if (operation.equals(FIELD.FILE.getFIELD())) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                String fileUrl = Encryption.AES.decrypt(envelope.getFileUrl(), senderKey);
                String fileHash = Encryption.AES.decrypt(envelope.getFileHash(), senderKey);
                FileData fileData = new FileData().convertFilePreview(fileUrl, fileHash);

                Message message = new Message(rMessage, Uri.parse(envelope.getFileUrl()), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Receiver, messageID);
                message.setUrl(Uri.parse(fileUrl));
                message.setHas(fileHash);
                message.setFileName(fileUrl);
                message.setFileSize("100mb");
                message.setTimestamp(envelope.getTime());
                message.setTypeFile(FIELD.FILE.getFIELD());
                message.setDataCreate("11.11.11 12.12.12");
                operableMSG.readMessageFile(message);
                returnAboutDeliver(message);
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

    public void onSend(String senderId, String receiverId, String message, String messageId, String receiverKey,String time) {
        try {
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            Envelope envelope = new Envelope(senderId, receiverId, FIELD.MESSAGE.getFIELD(), rMessage, messageId,time);
            //реалізація шифрування повідомлення
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        } catch (Exception e) {
            Log.e("OperationMSG", "Помилка під час відправки виникла помилка : " + e);
        }
    }

    public void onSendFile(String senderId, String receiverId, String message, String url, String has, String receiverKey, String messageId,String time) {
        try {
            String filename = new File(url).getName();
            Log.e("FileEncryption", " filename " + filename);
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            String rURL = Encryption.AES.encrypt(url, receiverKey);
            Log.e("FileEncryption", " rURL " + rURL);
            String rHAS = Encryption.AES.encrypt(has, receiverKey);
            String operation;
            operation = FIELD.FILE.getFIELD();
            Envelope envelope = new Envelope(senderId, receiverId, operation, rMessage, rURL, rHAS, messageId,time);
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        } catch (Exception e) {

        }
    }

    /**
     * Метод сповіщення сервер о отриманні повідомлення
     *
     * @param message повідомлення яке прийшло
     *                отримуємо такі данні для відправки сповіщення:
     *                >    @envelope.getSenderId() Id відправника
     *                >    @envelope.getReceiverId() Id отримувача тоб то нащ
     *                >    @envelope.getMessageId() Id повідомлення з яким воно прийшло
     *                Дане повідомлення відправляється тільки до Сервісу  далі він не відправляється
     */
    public void returnAboutDeliver(Message message) {

        String messageJson = new Envelope.Builder().
                setSenderId(message.getReceiverId()).
                setReceiverId(message.getSenderId()).
                setOperation("messageStatus").
                setMessageStatus("delivered_to_user").
                setMessageId(message.getMessageId()).
                build().
                toJson("senderId", "receiverId", "operation", "messageStatus", "messageId").
                toString();
        Log.e("IOService", "Return About Deliver" + messageJson);
        operableMSG.sendDataBackToActivity(messageJson);
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
