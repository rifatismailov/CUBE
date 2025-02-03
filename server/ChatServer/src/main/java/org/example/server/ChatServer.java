package org.example.server;

import org.example.UserData;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class ChatServer {
    // Зберігає підключених клієнтів
    private static Map<String, Socket> onlineUsers = new ConcurrentHashMap<>();
    // Зберігає непрочитані повідомлення для офлайн користувачів
    private static Map<String, List<String>> offlineMessages = new ConcurrentHashMap<>();
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    public static Map<String, String> clientsKey = new HashMap<>();

    private static boolean isServerRunning = true;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Сервер чату запущено на порті 8080...");

            // Слухач підключень
            while (isServerRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.execute(new ClientHandler(clientSocket, onlineUsers, offlineMessages, clientsKey));
                    System.out.println("Клієнт підключений, офлайн повідомлення: " + offlineMessages.size());
                } catch (IOException e) {
                    if (!isServerRunning) {
                        System.out.println("Сервер вимкнений, припиняю прийом нових підключень.");
                    } else {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    // Завершення роботи сервера та всіх потоків
    public static void stopServer() {
        isServerRunning = false;
        System.out.println("Закриваю сервер...");

        try {
            // Завершуємо всі потоки в пулі
            threadPool.shutdownNow();  // Примусове завершення всіх виконуваних завдань

            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                System.out.println("Завдання не завершились вчасно, примусово зупиняю сервер");
            }
            System.out.println("Сервер успішно зупинений");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


