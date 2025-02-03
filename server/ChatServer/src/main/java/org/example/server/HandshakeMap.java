package org.example.server;

import org.example.Envelope;
import org.example.UserData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class HandshakeMap {
    HandshakeListener listener;

    HandshakeMap(HandshakeListener listener) {
        this.listener = listener;
    }

    /**
     * Управляє процесом рукостискання між двома користувачами для обміну публічними ключами.
     *
     * @param senderId   Ідентифікатор відправника.
     * @param receiverId Ідентифікатор отримувача.
     * @param publicKey  Публічний ключ відправника.
     */
    public void handleHandshake(String senderId, String receiverId, String publicKey) {
        // Виводимо кількість поточних пар
        border(senderId, receiverId, "   ПОЧАТОК  ");
        border(senderId, receiverId, "  ХАНШЕЙК " + listener.getClientsKey().size() + "  ");
        // Якщо список порожній, додаємо нову пару
        if (listener.getClientsKey().size() == 0) {
            addKey(senderId, receiverId, publicKey);
        } else {

            // Перевірка за ID
            if (listener.getClientsKey().containsKey(senderId + ":" + receiverId)) {
                String value = listener.getClientsKey().get(senderId + ":" + receiverId);
                //Додаємо ключ  якщо він був змінений
                if (!value.equals(publicKey)) {
                    border(senderId, receiverId, " ЗМІНА КЛЮЧА");

                    addKey(senderId, receiverId, publicKey);
                    // Первіряємо чи є ключ від (receiverId) отримувача. Якщо є робимо обмін ключами.
                    // Обмін ключами проходе коли отримувачь підключився та зробив обмін ключами.
                    // Я не став змінювати логику обміну ключів якщо була зміна ключа відправити тілки отримувачу, а залишив так як є
                    // проходе стандартний обмін ключами від А до Б та навпаки від Б до А Не зважаючи на те що в Б не було зміни ключа
                    returnKey(senderId, receiverId, publicKey);
                    border(senderId, receiverId, " ЗАКІНЧЕННЯ ");
                }

            } else {
                border(senderId, receiverId, "   ДОДОВАННЯ  ");
                addKey(senderId, receiverId, publicKey);
                // первіряємо чи є ключ від (receiverId) отримувача. Якщо є робимо обмін ключами
                // обмін ключами проходе коли отримувачь підключився та зробив обмін ключами
                returnKey(senderId, receiverId, publicKey);
            }
        }
        border(senderId, receiverId, "    КІНЕЦ   ");
    }

    // перевірка чи доступний клієнт для відправки ключа
    public void checkOnline(String senderId, String receiverId, String publicKey) {
        if (listener.getOnlineUsers().containsKey(receiverId)) {
            // Якщо отримувач онлайн, надсилаємо ключ
            border(senderId, receiverId, "Отримувачь на зʼязку відправляємо з " + senderId + " до " + receiverId);
            sendKeyToUser(senderId, receiverId, publicKey);

        } else {
            // Якщо отримувач офлайн, зберігаємо ключ для подальшого використання
            border(senderId, receiverId, "Отримувачь не на зʼязку. Зберігаємо повідомлення " + senderId + " до " + receiverId);
            saveKeyToUser(senderId, receiverId, publicKey);
        }
    }


    private void border(String senderId, String receiverId, String border) {
        String borderLine = "===========================================================";
        System.out.printf("\n%s\n", borderLine);
        System.out.printf("| %-15s | %-30s | %-15s |\n", "Відправник", border, "Отримувач");
        System.out.printf("| %-15s | %-30s | %-15s |\n", senderId, "", receiverId);
        System.out.printf("%s\n", borderLine);
    }

    /**
     * Додає нову пару користувачів до списку клієнтів із зазначеним публічним ключем.
     *
     * @param senderId   Ідентифікатор відправника.
     * @param receiverId Ідентифікатор отримувача.
     * @param publicKey  Публічний ключ відправника.
     */
    private void addKey(String senderId, String receiverId, String publicKey) {
        listener.getClientsKey().put(senderId + ":" + receiverId, publicKey);
        System.out.printf("[ДОДАНО КЛЮЧ] Відправник: %s, Отримувач: %s, Ключ: %s\n", senderId, receiverId, publicKey);
    }

    private void sendKeyToUser(String senderId, String receiverId, String publicKey) {
        try {
            if (publicKey != null) {
                String handshakeMessage = "{\"publicKey\": \"" + publicKey + "\" }";
                listener.sendMessage(receiverId, new Envelope(senderId, receiverId, "handshake", handshakeMessage,"").toJson().toString());
                System.out.printf("[ВІДПРАВЛЕНО] Ключ від %s до %s: %s\n", senderId, receiverId, publicKey);
                //listener.getClientsKey().remove(receiverId + ":" + senderId);
            }
        } catch (IOException e) {
            System.err.printf("[ПОМИЛКА] Не вдалося відправити ключ від %s до %s: %s\n", senderId, receiverId, e.getMessage());
        }
    }

    private void saveKeyToUser(String senderId, String receiverId, String publicKey) {
        if (publicKey != null) {
            String handshakeMessage = "{\"publicKey\": \"" + publicKey + "\" }";
            listener.saveOfflineMessage(receiverId, new Envelope(senderId, receiverId, "handshake", handshakeMessage,"").toJson().toString());
            System.out.printf("[ЗБЕРЕЖЕНО] Ключ від %s для %s: %s\n", senderId, receiverId, publicKey);
        }
    }

    public void returnKey(String senderId, String receiverId, String publicKey) {
        border(senderId, receiverId, "Перевірка зворотного ключа");
        if (listener.getClientsKey().containsKey(receiverId + ":" + senderId)) {
            String value = listener.getClientsKey().get(receiverId + ":" + senderId);
            border(senderId, receiverId, "Обмін ключами");
            checkOnline(receiverId, senderId, value);
            checkOnline(senderId, receiverId, publicKey);
        } else {
            System.out.printf("[ІНФО] Зворотний ключ від %s для %s відсутній.\n", receiverId, senderId);
        }
    }


    // Інтерфейс
    public interface HandshakeListener {

        void sendMessage(String receiverId, String jsonMessage) throws IOException;

        Map<String, Socket> getOnlineUsers();

        Map<String, String> getClientsKey();

        void saveOfflineMessage(String receiverId, String message);
    }
}
