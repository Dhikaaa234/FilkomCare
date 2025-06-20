package com.example.filkomapp;

public class User {
    private String id;
    private String name;
    private String email;
    private String nim;
    private String programStudi;
    private boolean isAdmin;
    private String profileUrl;
    private String profileBase64;

    public User() {}

    public User(String name, String email, boolean isAdmin) {
        this.name = name;
        this.email = email;
        this.isAdmin = isAdmin;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNim() { return nim; }
    public void setNim(String nim) { this.nim = nim; }

    public String getProgramStudi() { return programStudi; }
    public void setProgramStudi(String programStudi) { this.programStudi = programStudi; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }


    public String getProfileBase64() { return profileBase64; }
    public void setProfileBase64(String profileBase64) { this.profileBase64 = profileBase64; }
}
