package com.example.folder.file;

import android.content.Context;

import java.io.File;

public class FilePathBuilder {

    private File directory;
    private String fileName;

    public FilePathBuilder() {}

    private FilePathBuilder(File directory) {
        this.directory = directory;
    }

    // Створює екземпляр білдера з основним каталогом
    public static FilePathBuilder withDirectory(File baseDir) {
        return new FilePathBuilder(baseDir);
    }

    // Встановлює ім'я файлу
    public FilePathBuilder setFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("Ім'я файлу не може бути порожнім");
        }
        this.fileName = fileName;
        return this;
    }

    // Перевіряє існування каталогу та створює його за потреби
    private void ensureDirectoryExists() {
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Не вдалося створити каталог: " + directory.getAbsolutePath());
        }
    }

    // Генерує повний шлях до файлу
    public String build() {
        ensureDirectoryExists();
        return new File(directory, fileName).getAbsolutePath();
    }

    // Повертає об'єкт файлу
    public File newFile() {
        ensureDirectoryExists();
        return new File(directory, fileName);
    }

    // Метод для створення каталогу на основі контексту
    public static File getDirectory(Context context, String folderName) {
        if (context == null) {
            throw new IllegalArgumentException("Контекст не може бути null");
        }
        return new File(context.getExternalFilesDir(null), folderName);
    }
}
