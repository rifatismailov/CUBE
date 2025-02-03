package org.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class SimpleHttpFileServer {

    private static final int PORT = 8020;
    private static final String UPLOAD_DIR = "storage";

    public static void main(String[] args) throws IOException {
        // Створення каталогу для файлів
        Files.createDirectories(Paths.get(UPLOAD_DIR));

        // Створення сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/upload", new UploadHandler());
        server.createContext("/download", new DownloadHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("HTTP Server запущено на порту " + PORT);
    }

    // Обробка завантаження файлів на сервер
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String fileName = exchange.getRequestHeaders().getFirst("File-Name");
                if (fileName == null || fileName.isEmpty()) {
                    sendResponse(exchange, 400, "File-Name header is missing");
                    return;
                }

                // Читання вхідного потоку та запис у файл
                try (InputStream inputStream = exchange.getRequestBody();
                     OutputStream fileOutputStream = new FileOutputStream(UPLOAD_DIR + "/" + fileName)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                sendResponse(exchange, 200, "Файл успішно завантажено: " + fileName);
            } else {
                sendResponse(exchange, 405, "Метод не підтримується");
            }
        }
    }

    // Обробка завантаження файлів з сервера
    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String fileName = exchange.getRequestURI().getQuery();

            if (fileName == null || fileName.isEmpty()) {
                sendResponse(exchange, 400, "Необхідно вказати ім'я файлу у запиті");
                return;
            }

            File file = new File(UPLOAD_DIR + "/" + fileName);
            if (!file.exists()) {
                sendResponse(exchange, 404, "Файл не знайдено");
                return;
            }

            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            exchange.sendResponseHeaders(200, file.length());

            try (OutputStream outputStream = exchange.getResponseBody();
                 FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    // Метод для відправлення HTTP-відповіді
    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        exchange.sendResponseHeaders(statusCode, message.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
}
