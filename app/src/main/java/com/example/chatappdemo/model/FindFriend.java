package com.example.chatappdemo.model;

public class FindFriend {
    private String  imgAnhDD, name;

    public FindFriend() {}

    public FindFriend(String imgAnhDD, String name) {
        this.imgAnhDD = imgAnhDD;
        this.name = name;
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
}
