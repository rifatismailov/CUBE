package example;

import java.io.File;

public class RenameFiles {
    public static void main(String[] args) {
        // Вказуємо шлях до директорії, де містяться файли
        File directory = new File("shrift/static/");

        // Перевіряємо, чи це дійсно директорія
        if (directory.isDirectory()) {
            // Отримуємо список файлів у директорії
            File[] files = directory.listFiles();

            // Перевіряємо, чи є файли в директорії
            if (files != null) {
                for (File file : files) {
                    // Перевіряємо, чи є файл
                    if (file.isFile()) {
                        String fileName = file.getName();
                        System.out.println("Файл " + fileName);



                            // Створюємо нову назву файлу: змінюємо всю назву на малу літеру
                            String newFileName = fileName.toLowerCase();
                            newFileName = newFileName.replaceAll("-", "_");
                            File newFile = new File(directory, newFileName);

                            // Перейменовуємо файл
                            boolean renamed = file.renameTo(newFile);
                            if (renamed) {
                                System.out.println("Файл " + fileName + " перейменовано на " + newFileName);
                            } else {
                                System.out.println("Не вдалося перейменувати файл " + fileName);
                            }

                    }
                }
            }
        } else {
            System.out.println("Вказаний шлях не є директорією.");
        }
    }
}
