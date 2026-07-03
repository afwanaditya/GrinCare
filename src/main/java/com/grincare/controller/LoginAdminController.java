package com.grincare.controller;

import com.grincare.repository.AdminRepository;
import com.grincare.service.AuthService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginAdminController {

    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtPasswordVisible;
    @FXML private Button        btnTogglePassword;
    @FXML private Button        btnMasuk;
    @FXML private Label         lblError;

    private final AdminRepository adminRepo   = new AdminRepository();
    private final AuthService     authService = new AuthService();

    @FXML
    public void initialize() {
        if (!adminRepo.sudahAdaAdmin()) {
            adminRepo.buatAdminDefault();
        }
    }

    @FXML
    private void onTogglePassword() {
        if (!txtPasswordVisible.isVisible()) {
            // Tampilkan password: salin dari PasswordField ke TextField lalu tukar visibilitas
            txtPasswordVisible.setText(txtPassword.getText());
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            btnTogglePassword.setText("🙈");
            txtPasswordVisible.requestFocus();
        } else {
            // Sembunyikan lagi: salin balik ke PasswordField, PasswordField jadi aktif
            txtPassword.setText(txtPasswordVisible.getText());
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            btnTogglePassword.setText("👁");
            txtPassword.requestFocus();
        }
    }

    @FXML
    private void onMasuk() {
        String username = txtUsername.getText().trim();

        // Pastikan teks terbaru dari field yang aktif masuk ke txtPassword
        if (txtPasswordVisible.isVisible()) {
            txtPassword.setText(txtPasswordVisible.getText());
        }
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            tampilkanError("Username dan password tidak boleh kosong.");
            return;
        }

        sembunyikanError();

        if (authService.login(username, password)) {
            navigasiKeDashboard(username);
        } else {
            tampilkanError("Username atau password salah.");
        }
    }

    private void navigasiKeDashboard(String username) {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DashboardAdmin.fxml"));
            Parent root = loader.load();
            DashboardAdminController dashboard = loader.getController();
            dashboard.setNamaAdmin(username);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tampilkanError(String pesan) {
        lblError.setText(pesan);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void sembunyikanError() {
        lblError.setText("");
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    @FXML
    private void handleKembali() {
        try {
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Konsultasi.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
