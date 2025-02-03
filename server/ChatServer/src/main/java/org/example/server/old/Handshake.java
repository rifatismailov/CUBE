package org.example.server.old;

import org.example.Envelope;
import org.example.UserData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Handshake {
    HandshakeListener listener;

    Handshake(HandshakeListener listener) {
        this.listener = listener;
    }

    /**
     * Управляє процесом рукостискання між двома користувачами для обміну публічними ключами.
     *
     * @param senderId   Ідентифікатор відправника.
     * @param receiverId Ідентифікатор отримувача.
     * @param publicKey  Публічний ключ відправника.
     */
    public  void handleHandshake(String senderId, String receiverId, String publicKey) {
        // Виводимо кількість поточних пар
        System.out.println("Handshake Start, Current Pairs: " + listener.getClientsKey().size());

        // Якщо список порожній, додаємо нову пару
        if (listener.getClientsKey().size() == 0) {
            System.out.println("New");
            addKey(senderId, receiverId, publicKey);
            System.out.println(listener.getClientsKey().isEmpty());
        } else {
            // Перебираємо всі існуючі пари
            for (UserData[] clientKey : listener.getClientsKey()) {
                System.out.println("Handshake Start, found: " + listener.getClientsKey().size());

                // Перевіряємо, чи знайдена пара збігається з наданими ідентифікаторами

                if (clientKey[0].getId().equals(senderId) && clientKey[1].getId().equals(receiverId)) {
                    //  Перевірка існуючої пари:
                    //  Умова clientKey[0].getId().equals(senderId) && clientKey[1].getId().equals(receiverId) перевіряє,
                    //  чи ідентифікатори поточного відправника (senderId) і отримувача (receiverId) відповідають парі,
                    //  яка вже зберігається в списку clientsKey.
                    //  Якщо знайдена пара, виконуємо обмін ключами
                    System.out.println("Handshake found: " + clientKey[0].getId() + " <-> " + clientKey[1].getId());
                    //  Якщо збережений ключ відправника (clientKey[0].getPublicKey()) збігається з поточним ключем (publicKey),
                    //  ключ отримувача (clientKey[1]) відправляється назад відправнику.
                    if (clientKey[0].getPublicKey().equals(publicKey)) {
                        sendKeyToUser(clientKey[1], clientKey[0]);
                        break;
                    } else {
                        //  Якщо ключ змінився, створюється новий об'єкт UserData для відправника зі зміненим ключем.
                        //  Потім викликається sendKeyToUser для передачі оновленого ключа отримувачу.
                        //clientKey[0] = new UserData(senderId, publicKey);
                        sendKeyToUser(clientKey[1], clientKey[0]);
                        //  Коли отримувач онлайн (onlineUsers.containsKey(receiverId)):
                        //  Передається оновлений ключ отримувачу (sendKeyToUser(clientKey[0], clientKey[1])).
                        //        Сценарій: Якщо обидва користувачі онлайн, обмін ключами відбувається в обох напрямках.
                        //        Коли отримувач офлайн:
                        //  Викликається метод addKey, щоб зберегти змінений ключ для майбутнього використання.
                        if (listener.getOnlineUsers().containsKey(receiverId)) {
                            sendKeyToUser(clientKey[0], clientKey[1]);
                            break;
                        }
                    }
                } else if (clientKey[0].getId().equals(receiverId) && clientKey[1].getId().equals(senderId)) {
                    // Інверсія порядку — отримувач стає відправником
                    System.out.println("Handshake found: " + clientKey[0].getId() + " <-> " + clientKey[1].getId());
                    UserData sender = clientKey[1];
                    sender.setPublicKey(publicKey);
                    UserData receiver = clientKey[0]; // Ключ отримувача може бути відсутній
                    System.out.println("Handshake found Public Key: " + clientKey[0].getPublicKey() + " <-> " + clientKey[1].getPublicKey());

                    // Виконуємо обмін ключами в обидва боки
                    sendKeyToUser(sender, receiver);
                    sendKeyToUser(receiver, sender);
                    break;

                } else {
                    // Якщо пара не знайдена, додаємо її
                    addKey(senderId, receiverId, publicKey);
                    break;
                }
            }
        }
    }


    /**
     * Додає нову пару користувачів до списку клієнтів із зазначеним публічним ключем.
     *
     * @param senderId   Ідентифікатор відправника.
     * @param receiverId Ідентифікатор отримувача.
     * @param publicKey  Публічний ключ відправника.
     */
    private void addKey(String senderId, String receiverId, String publicKey) {
            // Створюємо нових користувачів: відправника з ключем і отримувача без ключа
       // UserData sender = new UserData(senderId, publicKey);
        //UserData receiver = new UserData(receiverId, null); // Ключ отримувача може бути null
       // listener.getClientsKey().add(new UserData[]{sender, receiver});

        // Лог для підтвердження додавання
        System.out.println("Added: " + senderId + " <-> " + receiverId);
    }


    // Емуляція відправки ключа від одного користувача до іншого
    private void sendKeyToUser(@NotNull UserData target, UserData source) {
        try {
            if(target.getPublicKey()!=null) {
                String handshakeMessage = "{\"publicKey\": \"" + target.getPublicKey() + "\" }";
                listener.sendMessage(source.getId(), new Envelope(target.getId(), source.getId(), "handshake", handshakeMessage,"").toJson().toString());
                System.out.println("Відправлено ключ від " + target.getId() + " до " + source.getId() + " " + target.getPublicKey());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //serverConnection.sendData(new Envelope(target.getId(), source.getId(), "handshake", handshakeMessage).toJson().toString());
    }

    // Інтерфейс
    public interface HandshakeListener {

        void sendMessage(String receiverId, String jsonMessage) throws IOException;

        Map<String, Socket> getOnlineUsers();

        List<UserData[]> getClientsKey();
    }
}
