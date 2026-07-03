package com.grincare.model;

public class Admin {
    private String username;
    private String passwordHash;

    public Admin() {}

    public Admin(String username, String passwordHash) {
        this.username     = username;
        this.passwordHash = passwordHash;
    }

    public String getUsername()      { return username; }
    public void setUsername(String v) { this.username = v; }

    public String getPasswordHash()      { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
}
