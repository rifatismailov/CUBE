package com.example.folder.file;

import android.content.Context;

import java.io.File;

/**
 * Клас FilePathBuilder використовується для побудови шляху до файлу в заданому каталозі.
 * Дозволяє задавати ім'я файлу, перевіряти існування каталогу та створювати файли.
 */
public class FilePathBuilder {

    private File directory;
    private String fileName;

    /**
     * Конструктор за замовчуванням.
     */
    public FilePathBuilder() {}

    /**
     * Приватний конструктор, який ініціалізує білдер з каталогом.
     *
     * @param directory Базовий каталог для збереження файлу.
     */
    private FilePathBuilder(File directory) {
        this.directory = directory;
    }

    /**
     * Створює екземпляр білдера з основним каталогом.
     *
     * @param baseDir Каталог, у якому буде створюватися файл.
     * @return Новий екземпляр FilePathBuilder.
     */
    public static FilePathBuilder withDirectory(File baseDir) {
        return new FilePathBuilder(baseDir);
    }

    /**
     * Встановлює ім'я файлу.
     *
     * @param fileName Ім'я файлу.
     * @return Поточний екземпляр FilePathBuilder.
     * @throws IllegalArgumentException Якщо ім'я файлу порожнє або null.
     */
    public FilePathBuilder setFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Ім'я файлу не може бути порожнім");
        }
        this.fileName = fileName;
        return this;
    }

    /**
     * Перевіряє існування каталогу та створює його, якщо він не існує.
     *
     * @throws RuntimeException Якщо не вдалося створити каталог.
     */
    private void ensureDirectoryExists() {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Не вдалося створити каталог: " + directory.getAbsolutePath());
        }
    }

    /**
     * Генерує повний шлях до файлу у вказаному каталозі.
     *
     * @return Абсолютний шлях до файлу.
     */
    public String build() {
        ensureDirectoryExists();
        return new File(directory, fileName).getAbsolutePath();
    }

    /**
     * Створює новий об'єкт файлу у вказаному каталозі.
     *
     * @return Об'єкт файлу.
     */
    public File newFile() {
        ensureDirectoryExists();
        return new File(directory, fileName);
    }

    /**
     * Отримує каталог для збереження файлів у внутрішньому сховищі програми.
     *
     * @param context    Контекст додатка.
     * @param folderName Назва папки для збереження файлів.
     * @return Об'єкт каталогу.
     * @throws IllegalArgumentException Якщо передано null-контекст.
     */
    public static File getDirectory(Context context, String folderName) {
        if (context == null) {
            throw new IllegalArgumentException("Контекст не може бути null");
        }
        return new File(context.getExternalFilesDir(null), folderName);
    }
}
