package com.example.cube.control;

public enum FIELD {
    USER_ID("userId"),
    SENDER_ID("senderId"),
    RECEIVER_ID("receiverId"),
    MESSAGE_ID("messageId"),
    MESSAGE("message"),
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
    STATUS_USER("status_user"),
    CONTACT_NAME("name_contact"),
    CONTACT_ID("id_contact"),
    CONTACT_PUBLIC_KEY("public_key_contact"),
    PASSWORD("password"),
    STATUS_MESSAGE("messageStatus"),
    REPLY_FROM_CHAT("REPLY_FROM_CHAT"),
    DATA_TO_CHAT("DATA_TO_CHAT"),
    DATE_FROM_USERS_ACTIVITY("DATE_FROM_USERS_ACTIVITY"),
    DATA_FROM_CHAT("DATA_FROM_CHAT");
    private final String field;

    FIELD(String field) {
        this.field = field;
    }

    public String getFIELD() {
        return field;
    }
}
