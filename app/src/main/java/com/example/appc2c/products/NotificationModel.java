package com.example.appc2c.products;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class NotificationModel implements Serializable {
    private String title;
    private String message;
    private Timestamp timestamp;

    public NotificationModel() {}

    public NotificationModel(String title, String message, Timestamp timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
