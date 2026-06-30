package com.grincare.controller;

import com.grincare.model.Antrian;
import com.grincare.service.AntrianQueueService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AmbilAntrianController {

    @FXML private TextField namaField;
    @FXML private TextField waField;
    @FXML private Label     labelKategori;
    @FXML private Label     labelError;
    @FXML private Button    btnAmbil;

    private final AntrianQueueService service = new AntrianQueueService();

    @FXML
    public void initialize() {
        // Kategori sekarang dummy; nanti diisi dari hasil Chatbot AI
        labelKategori.setText("Pemeriksaan Umum");
        labelError.setText("");
    }

    @FXML
    private void handleAmbilAntrian() {
        String nama = namaField.getText().trim();

        if (nama.isEmpty()) {
            labelError.setText("Nama tidak boleh kosong.");
            namaField.requestFocus();
            return;
        }
        labelError.setText("");

        String noWA       = waField.getText().trim();
        String kategori   = labelKategori.getText();

        Antrian antrian = service.ambilAntrianBaru(nama, noWA, kategori);

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Antrian Berhasil Diambil");
        info.setHeaderText("Nomor Antrian: " + antrian.getNoAntrian());
        info.setContentText(
            "Ticket ID    : " + antrian.getTicketId()        + "\n" +
            "Nama         : " + antrian.getNama()            + "\n" +
            "Kategori     : " + antrian.getKategoriLayanan() + "\n" +
            "Status       : " + antrian.getStatus()          + "\n" +
            "Waktu Dibuat : " + antrian.getWaktuDibuat()
        );
        info.showAndWait();

        // Kosongkan form setelah berhasil
        namaField.clear();
        waField.clear();
    }

    @FXML
    private void handleKembali() {
        try {
            Stage stage = (Stage) namaField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/TabKategori.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
