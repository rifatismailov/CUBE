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
     * Class constructor that accepts an object that implements the {@link OperableMSG} interface.
     * This object is used to add messages and update the UI adapter.
     *
     * @param operableMSG An object that implements the {@link OperableMSG} interface.
     */
    public OperationMSG(OperableMSG operableMSG) {
        this.operableMSG = operableMSG;
    }

    /**
     * Processes received messages and performs appropriate operations depending on the message type.
     * Different types of operations (message, key exchange, handshake) are handled accordingly.
     *
     * @param data The JSON message to be processed.
     */
    public void onReceived(String senderKey, String data) {
        try {
            JSONObject object = new JSONObject(data);
            Envelope envelope = new Envelope(object);
            String operation = envelope.getOperation();
            String messageID = envelope.getMessageId();

            // Process data from the Activity, for example, update the UI
            if (FIELD.MESSAGE.getFIELD().equals(operation)) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                if (envelope.getFileUrl() == null) {
                    Message message = new Message(rMessage, Side.Receiver, messageID);
                    message.setTimestamp(envelope.getTime());
                    message.setMessageStatus(envelope.getMessageStatus());
                    operableMSG.readMessage(message);
                    returnAboutDeliver(message, "delivered_to_user");
                }
            } else if (FIELD.FILE.getFIELD().equals(operation)) {
                String rMessage = Encryption.AES.decrypt(envelope.getMessage(), senderKey);
                String fileUrl = Encryption.AES.decrypt(envelope.getFileUrl(), senderKey);
                String fileHash = Encryption.AES.decrypt(envelope.getFileHash(), senderKey);
                FileData fileData = new FileData().convertFilePreview(fileUrl, fileHash);

                Message message = new Message(rMessage, Uri.parse(envelope.getFileUrl()), fileData.getImageBytes(), fileData.getWidth(), fileData.getHeight(), Side.Receiver, messageID);
                message.setUrl(Uri.parse(fileUrl));
                message.setHas(fileHash);
                message.setFileName(fileUrl);
                message.setFileSize(envelope.getFileSize());
                message.setTimestamp(envelope.getTime());
                message.setTypeFile(envelope.getFiletype());
                message.setDataCreate("[d].[m].[year] [t]:[m]:[s]");
                message.setMessageStatus(envelope.getMessageStatus());
                operableMSG.readMessageFile(message);
                returnAboutDeliver(message, "delivered_to_user");
            } else if (FIELD.HANDSHAKE.getFIELD().equals(operation)) {
                JSONObject jsonObject = new JSONObject(envelope.getMessage());
                String rPublicKey = jsonObject.getString(FIELD.PUBLIC_KEY.getFIELD());
                operableMSG.addReceiverPublicKey(rPublicKey);
            } else if (FIELD.KEY_EXCHANGE.getFIELD().equals(operation)) {
                JSONObject jsonObject = new JSONObject(envelope.getMessage());
                String aesKey = jsonObject.getString(FIELD.AES_KEY.getFIELD());
                operableMSG.addReceiverKey(aesKey);
            } else if (FIELD.STATUS_MESSAGE.getFIELD().equals(operation)) {
                // Processing status messages
                String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
                operableMSG.addNotifier(messageID, status);
            }
        } catch (JSONException e) {
            Log.e("OperationMSG", "Error while receiving JSON: " + e);

        } catch (Exception e) {
            Log.e("OperationMSG", "Error while receiving data: " + e);
        }
    }

    public void onSend(String senderId, String receiverId, String message, String messageId, String receiverKey, String time) {
        try {
            String rMessage = Encryption.AES.encrypt(message, receiverKey);
            Envelope envelope = new Envelope(senderId, receiverId, FIELD.MESSAGE.getFIELD(), rMessage, messageId, time);
            //implementation of message encryption
            operableMSG.sendDataBackToActivity(envelope.toJson().toString());
        } catch (Exception e) {
            Log.e("OperationMSG", "Error while sending: " + e);
        }
    }

    public void onSendFile(Message message, String url, String receiverKey) {
        try {
            String rMessage = Encryption.AES.encrypt(message.getMessage(), receiverKey);
            String rURL = Encryption.AES.encrypt(url, receiverKey);
            String rHAS = Encryption.AES.encrypt(message.getHas(), receiverKey);
            String messageJson = new Envelope.Builder().
                    setSenderId(message.getSenderId()).
                    setReceiverId(message.getReceiverId()).
                    setOperation(FIELD.FILE.getFIELD()).
                    setMessage(rMessage).
                    setFileUrl(rURL).
                    setFiletype(message.getTypeFile()).
                    setFileSize(message.getFileSize()).
                    setFileHash(rHAS).
                    setMessageId(message.getMessageId()).
                    setTime(message.getTimestamp()).
                    build().
                    toJson("senderId", "receiverId", "operation", "message", "fileUrl", "filetype", "fileSize", "fileHash", "messageId", "timestamp").
                    toString();
            operableMSG.sendDataBackToActivity(messageJson);
        } catch (Exception e) {

        }
    }

    /**
     * Method for notifying the server about receiving a message
     *
     * @param message the message that arrived
     *                we get the following data for sending the notification:
     *                > @envelope.getSenderId() Sender ID
     *                > @envelope.getReceiverId() Recipient ID, that is, our
     *                > @envelope.getMessageId() Message ID with which it arrived
     *                This message is sent only to the Service, then it is not sent
     */
    public void returnAboutDeliver(Message message, String messageStatus) {

        String messageJson = new Envelope.Builder().
                setSenderId(message.getSenderId()).
                setReceiverId(message.getReceiverId()).
                setOperation("messageStatus").
                setMessageStatus(messageStatus).
                setMessageId(message.getMessageId()).
                build().
                toJson("senderId", "receiverId", "operation", "messageStatus", "messageId").
                toString();
        operableMSG.sendDataBackToActivity(messageJson);
    }

    /**
     * Interface for interacting with other components such as UI and adapters.
     * Used for adding messages, handshakes, exchanging AES keys, and updating adapters.
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