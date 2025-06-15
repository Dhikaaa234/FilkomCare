package com.example.filkomapp;

import java.util.HashMap;
import java.util.Map;

public class Report {
    private String id;
    private String title;
    private String location;
    private String description;
    private String image;
    private String userId;
    private String userName;
    private long timestamp;
    private String status;
    private int likes;
    private Map<String, Boolean> likedBy;

    // Constructors, getters, and setters
    public Report() {}

    public Report(String title, String location, String description, String image,
                  String userId, String userName, long timestamp, String status) {
        this.title = title;
        this.location = location;
        this.description = description;
        this.image = image;
        this.userId = userId;
        this.userName = userName;
        this.timestamp = timestamp;
        this.status = status;
        this.likes = 0;
        this.likedBy = new HashMap<>();
    }

    // Getters and setters for all fields
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }
    public Map<String, Boolean> getLikedBy() { return likedBy; }
    public void setLikedBy(Map<String, Boolean> likedBy) { this.likedBy = likedBy; }
}

