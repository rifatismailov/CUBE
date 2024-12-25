package com.example.folder.file.progress;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Source;
import okio.Okio;

public class ProgressRequestBody extends RequestBody {

    private File file;
    private String contentType;
    private UploadCallbacks listener;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage) throws InterruptedException;
        void onError();
        void onFinish();
    }

    public ProgressRequestBody(File file, String contentType, UploadCallbacks listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Source source;
        try {
            source = Okio.source(file);
            long totalBytesRead = 0;
            long read;

            long fileSize = contentLength();
            Buffer buffer = new Buffer();
            while ((read = source.read(buffer, DEFAULT_BUFFER_SIZE)) != -1) {
                totalBytesRead += read;
                sink.write(buffer, read);

                // Оновлюємо прогрес
                int progress = (int) ((totalBytesRead * 100) / fileSize);
                listener.onProgressUpdate(progress);
            }
        } catch (Exception e) {
            listener.onError();
        }
    }
}
