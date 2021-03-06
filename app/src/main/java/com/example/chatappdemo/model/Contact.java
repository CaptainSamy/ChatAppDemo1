package com.example.chatappdemo.model;

public class Contact {
    private String Contact, imgAnhBia, imgAnhDD, name, status, gioiTinh, phone, onlineStatus, typingTo, uid;
    private boolean isBlocked = false;

    public Contact() {
    }

    public Contact(String contact, String imgAnhBia, String imgAnhDD, String name, String status, String gioiTinh, String phone, String onlineStatus, String typingTo, String uid, boolean isBlocked) {
        Contact = contact;
        this.imgAnhBia = imgAnhBia;
        this.imgAnhDD = imgAnhDD;
        this.name = name;
        this.status = status;
        this.gioiTinh = gioiTinh;
        this.phone = phone;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.uid = uid;
        this.isBlocked = isBlocked;
    }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        Contact = contact;
    }

    public String getImgAnhBia() {
        return imgAnhBia;
    }

    public void setImgAnhBia(String imgAnhBia) {
        this.imgAnhBia = imgAnhBia;
    }

    public String getImgAnhDD() {
        return imgAnhDD;
    }

    public void setImgAnhDD(String imgAnhDD) {
        this.imgAnhDD = imgAnhDD;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getTypingTo() {
        return typingTo;
    }

    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
