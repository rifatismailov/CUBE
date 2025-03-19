package com.example.folder.file.progress;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Клас ProgressResponseBody використовується для відстеження прогресу завантаження файлу.
 * Він розширює ResponseBody з OkHttp і дозволяє отримувати оновлення про процес завантаження.
 */
public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final DownloadCallbacks listener;
    private BufferedSource bufferedSource;

    /**
     * Конструктор класу.
     *
     * @param responseBody Вхідний об'єкт ResponseBody, що містить отримані дані.
     * @param listener     Об'єкт для отримання оновлень про прогрес завантаження.
     */
    public ProgressResponseBody(ResponseBody responseBody, DownloadCallbacks listener) {
        this.responseBody = responseBody;
        this.listener = listener;
    }

    /**
     * Повертає MIME-тип отриманого контенту.
     *
     * @return MediaType відповідно до отриманого контенту.
     */
    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    /**
     * Повертає розмір файлу, що завантажується.
     *
     * @return Довжина файлу в байтах.
     */
    @Override
    public long contentLength() {
        return responseBody.contentLength();
    }

    /**
     * Отримує джерело даних з можливістю відстеження прогресу завантаження.
     *
     * @return BufferedSource обгортка навколо оригінального джерела.
     */
    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    /**
     * Обгортка для джерела, що відстежує кількість завантажених байтів.
     *
     * @param source Оригінальне джерело даних.
     * @return Source з можливістю відстеження прогресу.
     */
    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;
            long contentLength = responseBody.contentLength();

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);

                // Перевірка на 0 байт або невизначену довжину файлу
                if (contentLength <= 0) {
                    return bytesRead;
                }

                totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                // Обчислення прогресу
                int progress = (int) ((totalBytesRead * 100) / contentLength);
                listener.onProgressUpdate(progress);

                // Завершення завантаження
                if (bytesRead == -1) {
                    listener.onFinish();
                }
                return bytesRead;
            }
        };
    }

    /**
     * Інтерфейс для відстеження прогресу завантаження файлу.
     */
    public interface DownloadCallbacks {
        /**
         * Викликається при оновленні прогресу завантаження.
         *
         * @param percentage Відсоток завантаження.
         */
        void onProgressUpdate(int percentage);

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
