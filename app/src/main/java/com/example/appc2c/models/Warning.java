package com.example.appc2c.models;

import com.google.firebase.Timestamp;
import java.io.Serializable;

public class Warning implements Serializable {
    private String id;
    private String message;
    private Timestamp timestamp; // Dùng cho Firestore
    private long timeMillis;     // Dùng cho Realtime Database
    private String reason;
    private String userId;

    public Warning() {}

    // Constructor cho Firestore
    public Warning(String id, String message, Timestamp timestamp) {
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Constructor cho Realtime Database
    public Warning(String id, String message, long timeMillis) {
        this.id = id;
        this.message = message;
        this.timeMillis = timeMillis;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public long getTimeMillis() { return timeMillis; }
    public void setTimeMillis(long timeMillis) { this.timeMillis = timeMillis; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
