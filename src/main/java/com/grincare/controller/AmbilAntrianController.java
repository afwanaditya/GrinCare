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
        labelKategori.setText("Pemeriksaan Umum");
        labelError.setText("");
    }

    /** Dipanggil oleh KonsultasiController setelah load FXML untuk meneruskan hasil AI. */
    public void setKategoriLayanan(String kategori) {
        if (kategori != null && !kategori.isEmpty()) {
            labelKategori.setText(kategori);
        }
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

        try {
            Stage stage = (Stage) namaField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/KonfirmasiAntrian.fxml"));
            Parent root = loader.load();
            KonfirmasiAntrianController controller = loader.getController();
            controller.setAntrianData(antrian);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKembali() {
        try {
            Stage stage = (Stage) namaField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
