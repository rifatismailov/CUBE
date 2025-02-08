/**
 * The UserSetting class represents user account and connection settings.
 * It provides functionality to create user settings, serialize/deserialize them to/from JSON,
 * and manage user-specific preferences such as server configurations and notification preferences.
 */
package com.example.setting;

import org.json.JSONObject;

/**
 * Represents the settings of a user, including personal and server information.
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
     * Constructor to initialize user settings.
     *
     * @param id           Unique user ID
     * @param name         User's first name
     * @param lastName     User's last name
     * @param password     User's password
     * @param serverIp     Main server IP
     * @param serverPort   Main server port
     * @param fileServerIp File server IP
     * @param fileServerPort File server port
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
     * Constructor for building user settings using the Builder pattern.
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
     * Builder class for creating UserSetting objects in a flexible manner.
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
         * Builds and returns a UserSetting object.
         *
         * @return UserSetting instance
         */
        public UserSetting build() {
            return new UserSetting(this);
        }
    }

    /**
     * Constructs a UserSetting object from a JSONObject.
     *
     * @param jsonObject JSON object containing user settings data
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
     * Serializes the current UserSetting object to a JSONObject.
     *
     * @return JSONObject representing the user settings
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