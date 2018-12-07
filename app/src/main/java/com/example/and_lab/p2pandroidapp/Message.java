package com.example.and_lab.p2pandroidapp;


public class Message {
    String message;
    Boolean isSender;
    long createdAt;

    public Message(String message, boolean isSender) {
        this.message = message;
        this.isSender = isSender;
        this.createdAt = System.currentTimeMillis();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsSender() {
        return isSender;
    }

    public void setIsSender(Boolean isSender) {
        this.isSender = isSender;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}