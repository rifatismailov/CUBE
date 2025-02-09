/**
 * Клас UserSetting представляє налаштування облікового запису користувача та підключення.
 * Він забезпечує функціональні можливості для створення налаштувань користувача, серіалізації/десеріалізації їх у/з JSON,
 * і керувати індивідуальними параметрами користувача, такими як конфігурації сервера та параметри сповіщень.
 */
package com.example.setting;

import org.json.JSONObject;

/**
 * Представляє налаштування користувача, включаючи особисту інформацію та інформацію про сервер.
 */
public class UserSetting {

    // Fields for user settings
    private String id; // Unique user identifier
    private String name; // User's first name
    private String lastName; // User's last name
    private String password; // User's password
    private String avatarImageUrl; // URL for the avatar image
    private String accountImageUrl; // URL for the account image
    private String serverIp; // Main server IP address
    private String serverPort; // Main server port
    private String fileServerIp; // File server IP address
    private String fileServerPort; // File server port
    private boolean notifications; // Notification preference

    /**
     * Конструктор для ініціалізації налаштувань користувача.
     *
     * @param id Унікальний ідентифікатор користувача
     * @param name Ім'я користувача
     * @param lastName Прізвище користувача
     * @param password Пароль користувача
     * @param serverIp IP головного сервера
     * @param serverPort Головний порт сервера
     * @param fileServerIp IP файлового сервера
     * @param fileServerPort Порт файлового сервера
     */
    public UserSetting(String id, String name, String lastName, String password,
                       String serverIp, String serverPort, String fileServerIp, String fileServerPort) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.fileServerIp = fileServerIp;
        this.fileServerPort = fileServerPort;
    }

    /**
     * Конструктор для побудови налаштувань користувача за шаблоном Builder.
     */
    private UserSetting(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.lastName = builder.lastName;
        this.password = builder.password;
        this.avatarImageUrl = builder.avatarImageUrl;
        this.accountImageUrl = builder.accountImageUrl;
        this.serverIp = builder.serverIp;
        this.serverPort = builder.serverPort;
        this.fileServerIp = builder.fileServerIp;
        this.fileServerPort = builder.fileServerPort;
        this.notifications = builder.notifications;
    }

    /**
     * Клас Builder для створення об’єктів UserSetting у гнучкий спосіб.
     */
    public static class Builder {
        private String id;
        private String name;
        private String lastName;
        private String password;
        private String avatarImageUrl;
        private String accountImageUrl;
        private String serverIp;
        private String serverPort;
        private String fileServerIp;
        private String fileServerPort;
        private boolean notifications;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setAvatarImageUrl(String avatarImageUrl) {
            this.avatarImageUrl = avatarImageUrl;
            return this;
        }

        public Builder setAccountImageUrl(String accountImageUrl) {
            this.accountImageUrl = accountImageUrl;
            return this;
        }

        public Builder setServerIp(String serverIp) {
            this.serverIp = serverIp;
            return this;
        }

        public Builder setServerPort(String serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder setFileServerIp(String fileServerIp) {
            this.fileServerIp = fileServerIp;
            return this;
        }

        public Builder setFileServerPort(String fileServerPort) {
            this.fileServerPort = fileServerPort;
            return this;
        }

        public Builder setNotifications(boolean notifications) {
            this.notifications = notifications;
            return this;
        }

        /**
         * Створює та повертає об'єкт UserSetting.
         *
         * Екземпляр @return UserSetting
         */
        public UserSetting build() {
            return new UserSetting(this);
        }
    }

    /**
     * Створює об'єкт UserSetting з JSONObject.
     *
     * @param jsonObject Об’єкт JSON, що містить дані налаштувань користувача
     */
    public UserSetting(JSONObject jsonObject) {
        try {
            this.id = jsonObject.optString("userId", "");
            this.name = jsonObject.optString("name", "");
            this.lastName = jsonObject.optString("lastName", "");
            this.password = jsonObject.optString("password", "");
            this.avatarImageUrl = jsonObject.optString("avatarImageUrl", "");
            this.accountImageUrl = jsonObject.optString("accountImageUrl", "");
            this.serverIp = jsonObject.optString("serverIp", "");
            this.serverPort = jsonObject.optString("serverPort", "");
            this.fileServerIp = jsonObject.optString("fileServerIp", "");
            this.fileServerPort = jsonObject.optString("fileServerPort", "");
            this.notifications = jsonObject.optBoolean("notifications", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Серіалізує поточний об’єкт UserSetting у JSONObject.
     *
     * @return JSONObject, що представляє налаштування користувача
     */
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", id);
            jsonObject.put("name", name);
            jsonObject.put("lastName", lastName);
            jsonObject.put("password", password);
            jsonObject.put("avatarImageUrl", avatarImageUrl);
            jsonObject.put("accountImageUrl", accountImageUrl);
            jsonObject.put("serverIp", serverIp);
            jsonObject.put("serverPort", serverPort);
            jsonObject.put("fileServerIp", fileServerIp);
            jsonObject.put("fileServerPort", fileServerPort);
            jsonObject.put("notifications", notifications);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    /**
     * Серіалізує поточний об’єкт UserSetting у JSONObject.
     *
     * @return JSONObject, що представляє налаштування користувача
     * @param fields параметри за якими ми будемо повертати Json
     */
    public JSONObject toJson(String... fields) {
        JSONObject jsonObject = new JSONObject();
        try {
            for (String field : fields) {
                switch (field) {
                    case "userId":
                        jsonObject.put("userId", id);
                        break;
                    case "name":
                        jsonObject.put("name", name);
                        break;
                    case "lastName":
                        jsonObject.put("lastName", lastName);
                        break;
                    case "password":
                        jsonObject.put("password", password);
                        break;
                    case "avatarImageUrl":
                        jsonObject.put("avatarImageUrl", avatarImageUrl);
                        break;
                    case "accountImageUrl":
                        jsonObject.put("accountImageUrl", accountImageUrl);
                        break;
                    case "serverIp":
                        jsonObject.put("serverIp", serverIp);
                        break;
                    case "serverPort":
                        jsonObject.put("serverPort", serverPort);
                        break;
                    case "fileServerIp":
                        jsonObject.put("fileServerIp", fileServerIp);
                        break;
                    case "fileServerPort":
                        jsonObject.put("fileServerPort", fileServerPort);
                        break;
                    case "notifications":
                        jsonObject.put("notifications", notifications);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    // Getter and setter methods

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatarImageUrl() {
        return avatarImageUrl;
    }

    public void setAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public String getAccountImageUrl() {
        return accountImageUrl;
    }

    public void setAccountImageUrl(String accountImageUrl) {
        this.accountImageUrl = accountImageUrl;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }

    public String getFileServerIp() {
        return fileServerIp;
    }

    public void setFileServerIp(String fileServerIp) {
        this.fileServerIp = fileServerIp;
    }

    public String getFileServerPort() {
        return fileServerPort;
    }

    public void setFileServerPort(String fileServerPort) {
        this.fileServerPort = fileServerPort;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }
}