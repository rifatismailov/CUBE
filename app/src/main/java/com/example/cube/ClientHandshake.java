package com.example.cube;

public class ClientHandshake {

    private String senderPublicKey;
    private String receiverPublicKey;
    private boolean requestConfirmed;

    // Конструктор
    public ClientHandshake(String senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
        this.requestConfirmed = false;
    }

    // Метод для підтвердження з'єднання
    public void confirmRequest(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
        this.requestConfirmed = true;
    }

    // Гетери і сетери

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public void setSenderPublicKey(String senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
    }

    public boolean isRequestConfirmed() {
        return requestConfirmed;
    }

    public void setRequestConfirmed(boolean requestConfirmed) {
        this.requestConfirmed = requestConfirmed;
    }
}
