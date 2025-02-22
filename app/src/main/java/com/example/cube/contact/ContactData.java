package com.example.cube.contact;

import org.json.JSONObject;
import java.io.Serializable;

/**
 * Represents contact data with associated attributes such as keys, names, and image URLs.
 * Supports serialization to and from JSON.
 */
public class ContactData implements Serializable {
    private String id;
    private String publicKey;
    private String privateKey;
    private String receiverPublicKey;
    private String senderKey;
    private String receiverKey;
    private String name;
    private String lastName;
    private String messageSize;
    private String avatarImageUrl;
    private String accountImageUrl;
    private String statusContact;
    private String message;
    private String messageType;
    private int size;
    private int progress;

    private ContactData(Builder builder) {
        this.id = builder.id;
        this.publicKey = builder.publicKey;
        this.privateKey = builder.privateKey;
        this.receiverPublicKey = builder.receiverPublicKey;
        this.senderKey = builder.senderKey;
        this.receiverKey = builder.receiverKey;
        this.name = builder.name;
        this.lastName = builder.lastName;
        this.messageSize = builder.messageSize;
        this.avatarImageUrl = builder.avatarImageUrl;
        this.accountImageUrl = builder.accountImageUrl;
        this.size = builder.size;
        this.progress = builder.progress;
    }

    // Default constructor
    public ContactData() {}

    public ContactData(String id, String name, String lastName, String messageSize) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.messageSize = messageSize;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
    }

    public String getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(String senderKey) {
        this.senderKey = senderKey;
    }

    public String getReceiverKey() {
        return receiverKey;
    }

    public void setReceiverKey(String receiverKey) {
        this.receiverKey = receiverKey;
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

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getStatusContact() {
        return statusContact;
    }

    public void setStatusContact(String statusContact) {
        this.statusContact = statusContact;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Constructs a ContactData object from a JSON representation.
     * @param jsonObject JSON object containing contact information.
     */
    public ContactData(JSONObject jsonObject) {
        try {
            this.id = jsonObject.optString("id", "No ID");
            this.publicKey = jsonObject.optString("publicKey", "");
            this.privateKey = jsonObject.optString("privateKey", "");
            this.receiverPublicKey = jsonObject.optString("receiverPublicKey", "");
            this.senderKey = jsonObject.optString("senderKey", "");
            this.receiverKey = jsonObject.optString("receiverKey", "");
            this.name = jsonObject.optString("name", "No name");
            this.lastName = jsonObject.optString("lastName", "No lastName");
            this.avatarImageUrl = jsonObject.optString("avatarImageUrl", "");
            this.accountImageUrl = jsonObject.optString("accountImageUrl", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts the current ContactData object to a JSON representation.
     * @return JSONObject containing contact information.
     */
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("publicKey", publicKey);
            jsonObject.put("privateKey", privateKey);
            jsonObject.put("receiverPublicKey", receiverPublicKey);
            jsonObject.put("senderKey", senderKey);
            jsonObject.put("receiverKey", receiverKey);
            jsonObject.put("name", name);
            jsonObject.put("lastName", lastName);
            jsonObject.put("avatarImageUrl", avatarImageUrl);
            jsonObject.put("accountImageUrl", accountImageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * Builder class for constructing ContactData objects.
     */
    public static class Builder {
        private String id;
        private String publicKey;
        private String privateKey;
        private String receiverPublicKey;
        private String senderKey;
        private String receiverKey;
        private String name;
        private String lastName;
        private String messageSize;
        private String avatarImageUrl;
        private String accountImageUrl;
        private int size;
        private int progress;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setPublicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder setReceiverPublicKey(String receiverPublicKey) {
            this.receiverPublicKey = receiverPublicKey;
            return this;
        }

        public Builder setSenderKey(String senderKey) {
            this.senderKey = senderKey;
            return this;
        }

        public Builder setReceiverKey(String receiverKey) {
            this.receiverKey = receiverKey;
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

        public Builder setMessageSize(String messageSize) {
            this.messageSize = messageSize;
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

        public Builder setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder setProgress(int progress) {
            this.progress = progress;
            return this;
        }

        public ContactData build() {
            return new ContactData(this);
        }
    }
}
