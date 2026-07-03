package com.grincare.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardAdminController {

    @FXML private Label  labelNamaAdmin;

    @FXML private Button btnTabAntrian;
    @FXML private Button btnTabKategori;
    @FXML private Button btnTabStatistik;
    @FXML private Button btnTabKeyword;

    @FXML private VBox panelAntrian;
    @FXML private VBox panelKategori;
    @FXML private VBox panelStatistik;
    @FXML private VBox panelKeyword;

    private static final String STYLE_TAB_AKTIF =
        "-fx-background-color: #2563eb; -fx-text-fill: white;" +
        "-fx-font-size: 13px; -fx-font-weight: bold;" +
        "-fx-padding: 14 24 14 24; -fx-cursor: hand;" +
        "-fx-background-radius: 0; -fx-border-color: transparent;";

    private static final String STYLE_TAB_NONAKTIF =
        "-fx-background-color: transparent; -fx-text-fill: #64748b;" +
        "-fx-font-size: 13px;" +
        "-fx-padding: 14 24 14 24; -fx-cursor: hand;" +
        "-fx-background-radius: 0; -fx-border-color: transparent;";

    public void setNamaAdmin(String username) {
        labelNamaAdmin.setText("👤  " + username);
    }

    @FXML
    private void handleTabAntrian() {
        aktifkanTab(btnTabAntrian, panelAntrian);
    }

    @FXML
    private void handleTabKategori() {
        aktifkanTab(btnTabKategori, panelKategori);
    }

    @FXML
    private void handleTabStatistik() {
        aktifkanTab(btnTabStatistik, panelStatistik);
    }

    @FXML
    private void handleTabKeyword() {
        aktifkanTab(btnTabKeyword, panelKeyword);
    }

    private void aktifkanTab(Button tabAktif, VBox panelAktif) {
        // Reset semua tab ke style non-aktif
        btnTabAntrian.setStyle(STYLE_TAB_NONAKTIF);
        btnTabKategori.setStyle(STYLE_TAB_NONAKTIF);
        btnTabStatistik.setStyle(STYLE_TAB_NONAKTIF);
        btnTabKeyword.setStyle(STYLE_TAB_NONAKTIF);

        // Sembunyikan semua panel
        setPanel(panelAntrian,   false);
        setPanel(panelKategori,  false);
        setPanel(panelStatistik, false);
        setPanel(panelKeyword,   false);

        // Aktifkan tab dan panel yang dipilih
        tabAktif.setStyle(STYLE_TAB_AKTIF);
        setPanel(panelAktif, true);
    }

    private void setPanel(VBox panel, boolean tampil) {
        panel.setVisible(tampil);
        panel.setManaged(tampil);
    }

    @FXML
    private void handleLogout() {
        try {
            // Hapus referensi username sebelum navigasi
            labelNamaAdmin.setText("");

            Stage stage = (Stage) labelNamaAdmin.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Konsultasi.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
