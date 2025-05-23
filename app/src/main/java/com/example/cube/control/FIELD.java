package com.example.cube.control;

public enum FIELD {
    USER_ID("userId"),
    SENDER_ID("senderId"),
    RECEIVER_ID("receiverId"),
    MESSAGE_ID("messageId"),
    MESSAGE("message"),
    SETTING("setting"),
    SAVE_MESSAGE("save_message"),
    CONTACT_STATUS("contact_status"),
    IMAGE("image"),
    FILE("file"),
    OPERATION("operation"),
    HANDSHAKE("handshake"),
    PUBLIC_KEY("publicKey"),
    PRIVATE_KEY("privateKey"),
    RECEIVER_PUBLIC_KEY("receiverPublicKey"),
    SENDER_KEY("senderKey"),
    RECEIVER_KEY("receiverKey"),
    AES_KEY("aes_key"),
    KEY_EXCHANGE("keyExchange"),
    NAME("name"),
    USER_END("endUser"),
    LAST_NAME("lastName"),
    STATUS("status"),
    STATUS_USER("status_user"),
    PASSWORD("password"),
    STATUS_MESSAGE("messageStatus"),
    REPLY_FROM_CHAT("REPLY_FROM_CHAT"),
    DATA_TO_CHAT("DATA_TO_CHAT"),
    DATE_FROM_USERS_ACTIVITY("DATE_FROM_USERS_ACTIVITY"),
    DATA_FROM_CHAT("DATA_FROM_CHAT"),
    CUBE_ID_SENDER("CUBE_ID_SENDER"),
    CUBE_IP_TO_SERVER("CUBE_IP_TO_SERVER"),
    CUBE_PORT_TO_SERVER("CUBE_PORT_TO_SERVER"),
    CUBE_ID_RECIVER("CUBE_ID_RECIVER"),
    CUBE_SEND_TO_SERVER("CUBE_SEND_TO_SERVER"),
    CUBE_SEND_TO_SETTING("CUBE_SEND_TO_SETTING"),
    CUBE_RECEIVED_MESSAGE("CUBE_RECEIVED_MESSAGE"),
    MAIN_ACTIVITY_COMMAND("MAIN_ACTIVITY_COMMAND"),
    MAIN_ACTIVITY_REGISTRATION("MAIN_ACTIVITY_REGISTRATION"),
    COMMAND("command"),
    LIFE("LIFE"),
    IP("ip"),
    PORT("port"),
    SERVER_IP("serverIp"),
    SERVER_PORT("serverPort"),
    FILE_SERVER_IP("fileServerIp"),
    FILE_SERVER_PORT("fileServerPort"),
    NOTIFICATION("notification"),
    AVATAR("AVATAR"),
    AVATAR_ORG("AVATAR_ORG"),
    GET_AVATAR("GET_AVATAR");
    private final String field;

    FIELD(String field) {
        this.field = field;
    }

    public String getFIELD() {
        return field;
    }
}
