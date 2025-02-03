package old;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Transport;

public class SocketIOServerApp {
    public static void main(String[] args) {
        // Налаштування сервера
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0"); // Слухати всі доступні IP
        config.setOrigin("*");
        config.setPort(8080);

        // Увімкнути підтримку WebSocket
        config.setTransports(Transport.WEBSOCKET, Transport.POLLING);

        // Створення сервера
        SocketIOServer server = new SocketIOServer(config);

        // Обробка підключення
        server.addConnectListener(client -> {
            System.out.println("Клієнт підключився: " + client.getSessionId());
        });

        // Обробка відключення
        server.addDisconnectListener(client -> {
            System.out.println("Клієнт відключився: " + client.getSessionId());
        });

        // Обробка події "register"
        server.addEventListener("register", String.class, (client, data, ackSender) -> {
            System.out.println("Клієнт зареєструвався як: " + data);
        });

        // Обробка події "message"
        server.addEventListener("message", String.class, (client, data, ackSender) -> {
            System.out.println("Отримано повідомлення: " + data);
            // Відправка повідомлення назад клієнту
            client.sendEvent("message", "Сервер отримав: " + data);
        });

        // Запуск сервера
        server.start();
        System.out.println("Сервер запущено на порту: 8080");

        // Зупинка сервера при завершенні програми
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
