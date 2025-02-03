package org.example.chat;

import org.example.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Manager {
    public static void main(String[] args) {
        List<UserData> userData = new ArrayList<>();
        userData.add(new UserData("H652882306", "", "Vlad", "0"));
        userData.add(new UserData("H652882301", "", "Sergiy", "0"));
        userData.add(new UserData("H652882304", "", "Stas", "0"));
        ChatManager chatManager = new ChatManager(userData, "H652882307");
        chatManager.startMS();
        String testmessage = "This is a photo of just one young family killed by a Russian missile attack " +
                "on Kryvyi Rih. Tonight, there was another terrorist attack – and again three dead, this time in #Odesa. " +
                "All the missiles fired by #Russia were produced in the spring of 2023. In all of them, " +
                "without… https://t.co/InqQMPl3xr https://t.co/ByK8CN1dyA";
        /**
         * Основний метод для управління чатом. Забезпечує введення даних користувачем
         * і виконання відповідних операцій, таких як виклик або відправлення повідомлення.
         */
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введіть текст (для виходу введіть 'exit'):");

        // Читаємо рядки у циклі
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine(); // Отримуємо введений рядок
            if ("exit".equalsIgnoreCase(input)) { // Перевіряємо на вихід
                System.out.println("Програму завершено.");
                break;
            }
            if ("call".equalsIgnoreCase(input)) {
                int i = 0;
                for (UserData users : userData) {
                    System.out.println((i++) + " " + users.getId());
                }
                Integer userNumeber = scanner.nextInt(); // Отримуємо введений рядок
                chatManager.callReceiver(userNumeber);

            }
            if ("key".equalsIgnoreCase(input)) {
                int i = 0;
                for (UserData users : userData) {
                    System.out.println((i++) + " " + users.getId());
                }
                Integer userNumeber = scanner.nextInt(); // Отримуємо введений рядок
                chatManager.keyNewGenerate(userNumeber);

            }
            if ("start".equalsIgnoreCase(input)) {
                for (int i = 0; i < 20; i++) {
                    try {
                        chatManager.sendMessage(testmessage);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                chatManager.sendMessage(input);
            }
        }

        scanner.close(); // Закриваємо Scanner
    }
}
