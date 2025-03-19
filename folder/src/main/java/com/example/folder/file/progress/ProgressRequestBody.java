package com.example.folder.file.progress;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Source;
import okio.Okio;

/**
 * Клас ProgressRequestBody використовується для відстеження прогресу завантаження файлу.
 * Він розширює RequestBody з OkHttp і дозволяє надсилати файли з можливістю моніторингу процесу завантаження.
 */
public class ProgressRequestBody extends RequestBody {

    private final File file;
    private final String contentType;
    private final UploadCallbacks listener;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    /**
     * Конструктор класу.
     *
     * @param file        Файл, який потрібно завантажити.
     * @param contentType MIME-тип файлу (наприклад, "image/png", "application/pdf").
     * @param listener    Об'єкт для отримання оновлень про прогрес завантаження.
     */
    public ProgressRequestBody(File file, String contentType, UploadCallbacks listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }

    /**
     * Повертає MIME-тип контенту.
     *
     * @return MediaType відповідно до переданого типу контенту.
     */
    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    /**
     * Повертає розмір файлу, що завантажується.
     *
     * @return Довжина файлу в байтах.
     * @throws IOException У разі проблем з отриманням довжини файлу.
     */
    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    /**
     * Записує вміст файлу у вихідний потік та відстежує прогрес завантаження.
     *
     * @param sink Потік для запису даних.
     * @throws IOException У разі помилки вводу/виводу.
     */
    @Override
    public void writeTo(BufferedSink sink) {
        try {
            Source source = Okio.source(file);
            long totalBytesRead = 0;
            long read;

            long fileSize = contentLength();
            Buffer buffer = new Buffer();

            while ((read = source.read(buffer, DEFAULT_BUFFER_SIZE)) != -1) {
                totalBytesRead += read;
                sink.write(buffer, read);

                // Оновлення прогресу завантаження
                int progress = (int) ((totalBytesRead * 100) / fileSize);
                listener.onProgressUpdate(progress);
            }

            // Завершення завантаження
            listener.onFinish();
        } catch (Exception e) {
            listener.onError(e.getMessage());
        }
    }

    /**
     * Інтерфейс для відстеження прогресу завантаження файлу.
     */
    public interface UploadCallbacks {
        /**
         * Викликається при оновленні прогресу завантаження.
         *
         * @param percentage Відсоток завантаження.
         * @throws InterruptedException У разі переривання процесу.
         */
        void onProgressUpdate(int percentage) throws InterruptedException;

        /**
         * Викликається у разі помилки під час завантаження файлу.
         */
        void onError(String e);

        /**
         * Викликається після успішного завершення завантаження файлу.
         */
        void onFinish();
    }
}
