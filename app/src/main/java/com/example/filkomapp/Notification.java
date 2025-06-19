package com.example.filkomapp;

public class Notification {
    private String id;
    private String title;
    private String message;
    private long timestamp;
    private String reportId;
    private String userId;
    private boolean isRead;


    public Notification() {}

    public Notification(String id, String title, String message, String reportId, String userId, long timestamp, boolean isRead) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.reportId = reportId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
