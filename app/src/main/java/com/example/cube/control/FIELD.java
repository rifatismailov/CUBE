package com.example.cube.control;

public enum FIELD {
    USER_ID("userId"),
    SENDER_ID("senderId"),
    RECEIVER_ID("receiverId"),
    MESSAGE("message"),
    OPERATION("operation"),
    HANDSHAKE("handshake"),
    PUBLIC_KEY("publicKey"),
    PRIVATE_KEY("privateKey"),
    RECEIVER_PUBLIC_KEY("receiverPublicKey"),
    AES_KEY("aes_key"),
    KEY_EXCHANGE("keyExchange"),
    NAME("name"),
    LAST_NAME("lastName"),
    STATUS("status"),
    CONTACT_NAME("name_contact"),
    CONTACT_ID("id_contact"),
    CONTACT_PUBLIC_KEY("public_key_contact"),
    PASSWORD("password");
    private final String field;

    FIELD(String field) {
        this.field = field;
    }

    public String getFIELD() {
        return field;
    }
}
