package com.example.fileexchange;

import android.os.Build;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.util.logging.Logger;

/**
 * Клас для керування HTTP-запитами, який дозволяє завантажувати файли за вказаною URL-адресою.
 * Ре
 */
public class FileDownloader {

    private static final Logger LOGGER = Logger.getLogger(FileDownloader.class.getName());
    String fileName = "";

    /**
     * Завантажує файл з вказаного URL і зберігає його у вказану директорію.
     *
     * @param fileURL URL-адреса файлу для завантаження.
     * @param DIR     Шлях до директорії, куди зберігати файл.
     * @return Шлях до завантаженого файлу або null у разі помилки.
     */
    public String download(String fileURL, String DIR) {
        HttpURLConnection httpConn = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            // Створення HTTP-з'єднання з URL
            URL url = new URL(fileURL);
            httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // Перевірка коду відповіді сервера
            if (responseCode == HttpURLConnection.HTTP_OK) {

                // Отримання імені файлу з заголовка Content-Disposition або з URL
                String disposition = httpConn.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 9, disposition.length() - 1);
                    }
                } else {
                    // Якщо ім'я не вказано в заголовку, отримуємо його з URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.indexOf("?"));
                }

                // Декодування імені файлу для правильної обробки спеціальних символів
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
                }

                // Перевірка довжини імені файлу і скорочення, якщо перевищує 255 символів
                if (fileName.length() > 255) {
                    fileName = fileName.substring(0, 255);
                }

                // Відкриття потоку для читання даних з сервера
                inputStream = new BufferedInputStream(httpConn.getInputStream());
                // Відкриття потоку для запису файлу на диск
                outputStream = new FileOutputStream(DIR + fileName);

                byte[] buffer = new byte[4096];
                int bytesRead;

                // Читання даних по частинах і запис у файл
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // Переконання в тому, що всі дані записано
                outputStream.flush();
                LOGGER.info("Файл завантажено: " + fileName);
            } else {
                throw new IOException("Server returned HTTP response code: " + responseCode);
            }
        } catch (IOException e) {
            LOGGER.severe("Помилка при завантаженні файлу: " + e.getMessage());
            return null; // У разі помилки повертаємо null
        } finally {
            // Закриття потоків після завершення роботи
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                LOGGER.severe("Помилка під час закриття одного з потоків: " + e.getMessage());
            }
            // Роз'єднання HTTP-з'єднання
            if (httpConn != null) httpConn.disconnect();
            LOGGER.info("Зʼєднання розірвано");
        }

        // Повернення шляху до збереженого файлу
        return DIR + fileName;
    }


}
