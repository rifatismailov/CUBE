package example;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class ConsoleApp {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        // Статичне поле, яке не буде підніматися
        String staticField = "Статичне поле: Завантаження...";
        AtomicReference<String> dynamicMessage = new AtomicReference<>("Статичне поле: Поступово оновлюється...");

        // Початкове відображення
        System.out.println(staticField);
        System.out.println(); // Відступ для динамічної частини

        // Створюємо окремий потік для обробки вводу користувача
        Thread inputThread = new Thread(() -> {
            while (true) {
                // Зчитуємо введений текст користувача
                System.out.print("Введіть текст: ");
                String userInput = scanner.nextLine();

                // Оновлюємо динамічне повідомлення на основі вводу
                dynamicMessage.set("Введено: " + userInput);

                // Оновлюємо консоль
                updateConsole(staticField, dynamicMessage.get());
            }
        });

        inputThread.start();

        // Симулюємо зміни у динамічному полі (з прогресом або іншою інформацією)
        for (int i = 0; i <= 100; i++) {
            // Оновлюємо консоль
            updateConsole(staticField, dynamicMessage.get());

            // Затримка для симуляції оновлення
            Thread.sleep(500);
        }
    }

    // Метод для оновлення консольного виведення
    private static void updateConsole(String staticField, String dynamicMessage) {
        // Використовуємо ANSI-код для очищення екрану і переміщення курсора на початок
        System.out.print("\033[H\033[2J");
        System.out.flush();

        // Виводимо статичне поле
        System.out.println(staticField);

        // Виводимо динамічне повідомлення
        System.out.println(dynamicMessage);
    }
}
