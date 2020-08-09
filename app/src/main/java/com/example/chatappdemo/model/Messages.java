
package com.example.chatappdemo.model;

import com.google.firebase.database.PropertyName;

public class Messages {
    private String from, message, type, to, timeStamp, nameFile;
    private boolean isSeen;
    private String latitude;
    private String longitude;
    public Messages() {
    }

    public Messages(String from, String message, String type, String to, String timeStamp, String nameFile, boolean isSeen, String latitude, String longitude) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.timeStamp = timeStamp;
        this.nameFile = nameFile;
        this.isSeen = isSeen;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    @PropertyName("isSeen")
    public boolean isSeen() {
        return isSeen;
    }

    @PropertyName("isSeen")
    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
