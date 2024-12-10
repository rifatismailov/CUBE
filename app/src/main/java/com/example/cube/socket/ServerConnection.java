package com.example.cube.socket;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Клас для роботи з серверним підключенням.
 * Використовується для надсилання, отримання даних та перевірки стану підключення.
 */
public class ServerConnection implements Connector.Listener {

    private Connector connector;
    private final ConnectionListener listener;
    private String userId;
    private String receiverId;


    public ServerConnection(ConnectionListener listener, String userId,String SERVER_IP, int SERVER_PORT) {
        this.listener = listener;
        this.userId = userId;
        connector = new Connector(this, SERVER_IP, SERVER_PORT);
        connector.connectToServer();
        connector.setUserId(userId);
        connector.setREGISTRATION_COMMAND("{\"userId\":\"" + this.userId + "\"}");
        connector.setPING_COMMAND(new Envelope(userId, userId, "ping", "ping", "").toJson().toString());
    }

    //Якщо кілька потоків можуть змінювати receiverId, використовуйте synchronized або додайте механізм синхронізації:
    public synchronized void setUserId(String userId) {
        this.userId = userId;
    }

    //Якщо кілька потоків можуть змінювати receiverId, використовуйте synchronized або додайте механізм синхронізації:
    public synchronized void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
        listener.setLogs("[INFO] [SetReceiverId]", "receiverId встановлено: " + receiverId);
    }

    // Метод підключення до сервера
    public void connectToServer() {

    }


    // Надсилання даних на сервер
    public synchronized void sendData(String data) {
        connector.sendData(data);
    }




    // Читання даних з сервера
    @Override
    public void onListener(String message) {
        try {
            if (message != null) {
                JSONObject object = new JSONObject(message);
                Envelope envelope = new Envelope(object);
                // listener.setLogs("[INFO] [Check receiver]", "Відправник " + receiverId);
                // якщо повідомлення від нас самих Тоб то  SenderId та ReceiverId однакові. Наприклад спеціальні команди на сервер
                if (userId.equals(envelope.getSenderId()) && listener.getReceiverId().equals(envelope.getReceiverId())) {
                    listener.onReceived(message);
                    Thread.sleep(100); // 100 мс
                    // якщо ReceiverId інший та він запушений у вікні ChatActivity то відправляємо туди інакше зберігаємо повідомлення
                } else if (listener.getReceiverId() != null && listener.getReceiverId().equals(envelope.getSenderId())) {
                    listener.onReceived(message);
                    Thread.sleep(100); // 100 мс
                } else {
                    listener.saveMessage(envelope);
                }
            }
        } catch (JSONException | InterruptedException e) {
            listener.setLogs("[ERROR] [Listen messages]", " Помилка обробки JSON під час отримання повідомлення - " + e.getMessage());

        }
    }

    @Override
    public void setLogs(String clas, String log) {
        listener.setLogs(clas, log);
    }


    public void sendHandshake(String userId, String receiverId, String operation, String nameKey, String key) {
        String keyMessage = "{\"" + nameKey + "\": \"" + key + "\" }";
        sendData(new Envelope(userId, receiverId, operation, keyMessage, "").toJson().toString());
    }

    // Інтерфейс для сповіщення про стан підключення
    public interface ConnectionListener {

        void onReceived(String message);

        void saveMessage(Envelope envelope);

        void setLogs(String clas, String log);

        String getReceiverId();
    }
}
