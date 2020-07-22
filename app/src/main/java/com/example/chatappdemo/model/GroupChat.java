package com.example.chatappdemo.model;

public class GroupChat {
    String message, sender, timestamp, type, nameFile;

    public GroupChat() {
    }

    public GroupChat(String message, String sender, String timestamp, String type, String nameFile) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
        this.type = type;
        this.nameFile = nameFile;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }
}
