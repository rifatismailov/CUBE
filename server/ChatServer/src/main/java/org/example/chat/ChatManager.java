package org.example.chat;

import encryption.Encryption;
import encryption.KeyGenerator;
import org.example.Envelope;
import org.example.UserData;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Клас `ChatManager` відповідає за керування чатом, отримання повідомлень, їх обробку та
 * взаємодію із сервером через об'єкт `ServerConnection`.
 */

public class ChatManager implements ServerConnection.ConnectionListener {
    private ServerConnection serverConnection;
    private String userId;
    private String receiverId;  // ID отримувача
    private static int numMessage = 0;  // Лічильник повідомлень
    private List<UserData> userList;  // Список користувачів
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private PublicKey receiverPublicKey;
    static HashMap<Integer, Envelope> saveMessage = new HashMap<>();  // Збережені повідомлення
    private final ExecutorService executor = Executors.newSingleThreadExecutor();  // Виконання в окремому потоці
    private UserData user;  // Поточний користувач
    final String MESSAGE = "message";
    final String IMAGE = "image";
    final String FILE = "file";
    final String OPERATION = "operation";
    final String HANDSHAKE = "handshake";
    final String PUBLICKEY = "publicKey";
    final String PRIVATEKEY = "privateKey";
    final String KEY_EXCHANGE = "keyExchange";

    /**
     * Конструктор класу `ChatManager`.
     *
     * @param userList список користувачів для управління.
     * @param userId   ID поточного користувача.
     */
    public ChatManager(List<UserData> userList, String userId) {
        this.userList = userList;
        this.userId = userId;
    }

    public void startMS() {
        if (!userId.isEmpty()) {
            serverConnection = new ServerConnection(this, "localhost", 8080);
            serverConnection.connectToServer();
            serverConnection.setUserId(userId);
        }
    }

    public void keyNewGenerate(Integer userNumeber) {
        user = userList.get(userNumeber);
        System.out.println("Ви вибрали: " + user.getId()); // Виводимо введений текст
        receiverId = user.getId();
        serverConnection.setReceiverId(user.getId());
        if (user.getPublicKey().isEmpty() && user.getPrivateKey().isEmpty() && user.getSenderKey().isEmpty()) {
            //generate PublicKey
            KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA();
            keyGenerator.key();
            user.setPublicKey(keyGenerator.getPublicKey());
            user.setPrivateKey(keyGenerator.getPrivateKey());
            String key = KeyGenerator.AES.generateKey(16);
            System.out.println("Генерація для [" + receiverId + "]: " + key);

            user.setSenderKey(key);
        }

        //send to server PublicKey
        sendKey(userId, receiverId, HANDSHAKE, "publicKey", user.getPublicKey());


        //або якщо handshake вже був то дивимось чи не було повідомлень з боку віддаленого користувача з ким проводиться повідомлення
        onMessageSaved();

    }

    public void callReceiver(Integer userNumeber) {
        user = userList.get(userNumeber);
        System.out.println("Ви вибрали: " + user.getId()); // Виводимо введений текст
        receiverId = user.getId();
        serverConnection.setReceiverId(user.getId());
        if (user.getPublicKey().isEmpty()) {
            //generate PublicKey
            KeyGenerator.RSA keyGenerator = new KeyGenerator.RSA();
            keyGenerator.key();
            user.setPublicKey(keyGenerator.getPublicKey());
            user.setPrivateKey(keyGenerator.getPrivateKey());
            String key = KeyGenerator.AES.generateKey(16);
            System.out.println("Генерація для [" + receiverId + "]: " + key);

            user.setSenderKey(key);
            //send to server PublicKey

            sendKey(userId, receiverId, HANDSHAKE, "publicKey", user.getPublicKey());

        } else {
            //або якщо handshake вже був то дивимось чи не було повідомлень з боку віддаленого користувача з ким проводиться повідомлення
            onMessageSaved();
        }
    }

    public void sendMessage(String input) {
        if (user != null) {
            if (!input.isEmpty()) {
                if (user.getReceiverPublicKey() != null && user.getReceiverKey() != null) {
                    try {
                        System.out.println("Відправка шифрованого повідолмення за допомогою ключа: " + user.getReceiverKey());
                        String message = Encryption.AES.encrypt(input, user.getReceiverKey());
                        serverConnection.sendData(new Envelope(userId, receiverId, MESSAGE, message,UUID.randomUUID().toString()).toJson().toString());
                    } catch (Exception e) {
                        System.out.println("під час шифрування виникла помилка: " + e);
                    }

                }
            }
        }
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onMessageReceived(String message) {
        try {
            JSONObject object = new JSONObject(message);
            Envelope envelope = new Envelope(object);
            String sender = envelope.toJson().getString("senderId");
            String operation = envelope.toJson().getString(OPERATION);
            String receivedMessage = envelope.toJson().getString(MESSAGE);
            if (MESSAGE.equals(operation)) {

                //String decryptedMessage = RSAEncryption.decrypt(receivedMessage, privateKey);
                String rMessage = Encryption.AES.decrypt(receivedMessage, user.getSenderKey());
                System.out.println("Отримано повідомлення від [" + sender + "]: " + rMessage);

            } else if (IMAGE.equals(operation)) {

                String rMessage = Encryption.AES.decrypt(receivedMessage, user.getSenderKey());
                String fileUrl = Encryption.AES.decrypt(envelope.toJson().getString("fileUrl"), user.getSenderKey());
                String fileHash = Encryption.AES.decrypt(envelope.toJson().getString("fileHash"), user.getSenderKey());
                System.out.println("Отримано повідомлення від [" + sender + "]: " + rMessage);
                System.out.println("[fileUrl]: " + fileUrl);
                System.out.println("[fileHash]: " + fileHash);


            } else if (FILE.equals(operation)) {

                String rMessage = Encryption.AES.decrypt(receivedMessage, user.getSenderKey());
                String fileUrl = Encryption.AES.decrypt(envelope.toJson().getString("fileUrl"), user.getSenderKey());
                String fileHash = Encryption.AES.decrypt(envelope.toJson().getString("fileHash"), user.getSenderKey());
                System.out.println("Отримано повідомлення від [" + sender + "]: " + rMessage);
                System.out.println("[fileUrl]: " + fileUrl);
                System.out.println("[fileHash]: " + fileHash);

                String sMessage= Encryption.AES.encrypt(rMessage, user.getReceiverKey());
                String sFileUrl= Encryption.AES.encrypt(fileUrl, user.getReceiverKey());
                String sFileHash= Encryption.AES.encrypt(fileHash, user.getReceiverKey());
                Envelope envelope1 = new Envelope(userId,receiverId,FILE,sMessage,sFileUrl,sFileHash,UUID.randomUUID().toString());
                serverConnection.sendData(envelope1.toJson().toString());


            } else if (HANDSHAKE.equals(operation)) {
                addHandshake(envelope);
            } else if (operation.equals(KEY_EXCHANGE)) {
                addAESKey(sender, receivedMessage);
            } else if (operation.equals(FIELD.STATUS_MESSAGE.getFIELD())) {
                //operable.addMessage(message);
                String status = envelope.toJson().getString(FIELD.STATUS_MESSAGE.getFIELD());
                String messageID = envelope.toJson().getString(FIELD.MESSAGE_ID.getFIELD());
                System.out.println("Отримано STATUS_MESSAGE [" + sender + "]: " + status+" [ "+ messageID + "]" );
            }
        } catch (Exception e) {

        }

    }

    /**
     * Відправка збережених повідомлень після затримки, щоб активність чату встигла ініціалізуватися.
     */

    private void onMessageSaved() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {

            Iterator<Map.Entry<Integer, Envelope>> iterator = saveMessage.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, Envelope> entry = iterator.next();
                Envelope envelope = entry.getValue();
                if (envelope.getSenderId().equals(receiverId)) {
                    String receivedMessage = envelope.toJson().getString(MESSAGE);
                    String operation = envelope.toJson().getString(OPERATION);
                    if (OPERATION.equals(operation)) {
                        System.out.println("Збережено повідомленнявід [" + receiverId + "]: " + receivedMessage);
                    } else if (HANDSHAKE.equals(operation)) {
                        addHandshake(envelope);
                    } else if (operation.equals(KEY_EXCHANGE)) {
                        System.out.println("Збережено KEY_EXCHANGE [" + receiverId + "]: " + receivedMessage);
                        try {
                            addAESKey(receiverId, receivedMessage);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                    iterator.remove();
                }
            }
            scheduler.shutdown(); // Завершити роботу планувальника
        }, 1, TimeUnit.SECONDS); // Відкладення на 1 секунду
    }

    public void addAESKey(String sender, String receivedMessage) {

        try {
            JSONObject jsonObject = new JSONObject(receivedMessage);
            String receiverKey = jsonObject.getString(FIELD.AES_KEY.getFIELD());

            for (UserData user : userList) {
                if (user.getId().equals(sender)) {
                    PrivateKey privateKey = new KeyGenerator.RSA().decodePrivateKey(user.getPrivateKey());
                    String AES = Encryption.RSA.decrypt(receiverKey, privateKey);
                    System.out.println("Отримано AES від [" + sender + "]: " + AES);

                    user.setReceiverKey(AES);
                    break;
                }
            }
        } catch (Exception e) {

        }
    }

    // Якщо в нас пройшов ханшейк то ми відпрпвляємо ключ AES для шифрування (повідомлень)
    public void addHandshake(Envelope envelope) {
        try {
            String sender = envelope.getSenderId();
            String receivedMessage = envelope.toJson().getString(FIELD.MESSAGE.getFIELD());
            JSONObject jsonObject = new JSONObject(receivedMessage);
            String publicKey = jsonObject.getString(FIELD.PUBLIC_KEY.getFIELD());
            //Перевіряємо якщо publicKey не пустий або якщо він змінився додаємо
            if (!publicKey.isEmpty() || !publicKey.equals(publicKey)) {
                System.out.println("Отримано handshake від [" + sender + "]: " + publicKey);
                updateReceiverPublicKey(sender, publicKey);
                //Для відправки спочатку отримуємо публічний ключ отримувача для шифрування AES ключа
                PublicKey receiverPublicKey = new KeyGenerator.RSA().decodePublicKey(user.getReceiverPublicKey());
                // Шифруємо AES ключ за допомогою публічного ключа отримувача
                String AES = Encryption.RSA.encrypt(user.getSenderKey(), receiverPublicKey);
                serverConnection.sendHandshake(userId, sender, FIELD.KEY_EXCHANGE.getFIELD(), "aes_key", AES);
            }
        } catch (JSONException e) {

        } catch (Exception e) {
        }
    }

    private void updateReceiverPublicKey(String sender, String publicKey) {
        for (UserData user : userList) {
            if (user.getId().equals(sender)) {
                user.setReceiverPublicKey(publicKey);
                System.out.println("Оновлено публічний ключ для користувача: " + sender);
                break;
            }
        }
    }

    private void sendKey(String userId, String receiverId, String operation, String nameKey, String key) {
        String keyMessage = "{\"" + nameKey + "\": \"" + key + "\" }";
        serverConnection.sendData(new Envelope(userId, receiverId, operation, keyMessage,"").toJson().toString());
    }


    /**
     * Зберігає отримане повідомлення в системі.
     *
     * @param envelope дані повідомлення, яке зберігається.
     */
    @Override
    public void saveMessage(Envelope envelope) {
        saveMessage.put(numMessage, envelope);
        numMessage++;

        // Оновлення інтерфейсу користувача в окремому потоці
        executor.submit(() -> {
            for (UserData user : userList) {
                if (user.getId().equals(envelope.getSenderId())) {
                    user.setMessageSize(String.valueOf(numMessage)); // Оновлюємо messageSize
                    break;
                }
            }
        });
    }

}
