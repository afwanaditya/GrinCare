package com.grincare.service;

import com.grincare.model.Admin;
import com.grincare.repository.AdminRepository;

import java.security.MessageDigest;

public class AuthService {

    private final AdminRepository repo = new AdminRepository();

    public boolean login(String username, String password) {
        Admin admin = repo.cariAdmin(username);
        if (admin == null) return false;
        return hashPassword(password).equals(admin.getPasswordHash());
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Gagal hash password", e);
        }
    }
}
