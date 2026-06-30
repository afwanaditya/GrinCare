package com.grincare.controller;

import com.grincare.service.GeminiService;
import com.grincare.service.GeminiService.GeminiResult;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class KonsultasiController {

    @FXML private TextField keluhanField;
    @FXML private Button    btnKirim;
    @FXML private Label     labelLoading;
    @FXML private Label     labelError;
    @FXML private VBox      panelHasil;
    @FXML private TextArea  areaEdukasi;
    @FXML private Label     labelKategoriHasil;
    @FXML private Button    btnLanjut;

    // Header display
    @FXML private Label labelTanggal;
    @FXML private Label labelJam;
    @FXML private Label labelTimestamp;

    private final GeminiService geminiService = new GeminiService();
    private String hasilKategori = "Pemeriksaan Umum";

    @FXML
    public void initialize() {
        labelError.setText("");
        panelHasil.setVisible(false);
        panelHasil.setManaged(false);
        labelLoading.setVisible(false);
        labelLoading.setManaged(false);

        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        String jam = LocalTime.now().format(DateTimeFormatter.ofPattern("HH.mm"));
        labelTanggal.setText(today.format(dateFmt));
        labelJam.setText(jam);
        labelTimestamp.setText(jam);
    }

    @FXML
    private void handleKirim() {
        String keluhan = keluhanField.getText().trim();
        if (keluhan.isEmpty()) {
            labelError.setText("Keluhan tidak boleh kosong.");
            keluhanField.requestFocus();
            return;
        }
        labelError.setText("");
        setLoading(true);

        Task<GeminiResult> task = new Task<GeminiResult>() {
            @Override
            protected GeminiResult call() {
                return geminiService.analisisKeluhan(keluhan);
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);
            GeminiResult result = task.getValue();
            if (result == null) {
                tampilkanPesanGagal();
                return;
            }
            tampilkanHasil(result);
        });

        task.setOnFailed(e -> {
            setLoading(false);
            tampilkanPesanGagal();
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void tampilkanHasil(GeminiResult result) {
        areaEdukasi.setText(result.getEdukasi());
        panelHasil.setVisible(true);
        panelHasil.setManaged(true);

        boolean valid = result.isDalamDomain()
                        && result.getKategoriLayanan() != null
                        && !result.getKategoriLayanan().isEmpty();

        if (valid) {
            hasilKategori = result.getKategoriLayanan();
            labelKategoriHasil.setText("Rekomendasi layanan: " + hasilKategori);
        }
        labelKategoriHasil.setVisible(valid);
        labelKategoriHasil.setManaged(valid);
        btnLanjut.setVisible(valid);
        btnLanjut.setManaged(valid);
    }

    private void tampilkanPesanGagal() {
        hasilKategori = "Pemeriksaan Umum";
        areaEdukasi.setText("AI sedang tidak dapat diakses, sistem menggunakan mode cadangan.\n" +
                            "Silakan lanjutkan dengan kategori default, atau coba lagi nanti.");
        labelKategoriHasil.setText("Rekomendasi layanan: Pemeriksaan Umum (mode cadangan)");
        panelHasil.setVisible(true);
        panelHasil.setManaged(true);
    }

    private void setLoading(boolean loading) {
        btnKirim.setDisable(loading);
        labelLoading.setVisible(loading);
        labelLoading.setManaged(loading);
        if (loading) {
            panelHasil.setVisible(false);
            panelHasil.setManaged(false);
        }
    }

    @FXML
    private void handleLanjut() {
        try {
            Stage stage = (Stage) keluhanField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AmbilAntrian.fxml"));
            Parent root = loader.load();
            AmbilAntrianController ctrl = loader.getController();
            ctrl.setKategoriLayanan(hasilKategori);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLewati() {
        try {
            Stage stage = (Stage) keluhanField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AmbilAntrian.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleKembali() {
        try {
            Stage stage = (Stage) keluhanField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/TabKategori.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
