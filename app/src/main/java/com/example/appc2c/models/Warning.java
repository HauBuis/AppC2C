package com.example.appc2c.models;

import java.io.Serializable;

public class Warning implements Serializable {
    private String id;
    private String message;
    private long timestamp;
    private String reason;
    public Warning() {}  // Required for Firebase

    public Warning(String id, String message, long timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }
    public String getReason() {

        return reason;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
