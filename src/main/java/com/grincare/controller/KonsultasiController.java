package com.grincare.controller;

import com.grincare.model.Antrian;
import com.grincare.service.AntrianQueueService;
import com.grincare.service.GeminiService;
import com.grincare.service.GeminiService.GeminiResult;

import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

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

    @FXML private Label labelTanggal;
    @FXML private Label labelJam;
    @FXML private Label labelTimestamp;

    @FXML private Label labelNomorAntrian;
    @FXML private Label labelJumlahAntrian;

    @FXML private HBox  toastBox;
    @FXML private Label labelToast;

    private static final String STYLE_BTN_AKTIF =
        "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-background-radius: 8;" +
        "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 0 12 0; -fx-cursor: hand;";
    private static final String STYLE_BTN_NONAKTIF =
        "-fx-background-color: #e2e8f0; -fx-text-fill: #94a3b8;" +
        "-fx-background-radius: 8; -fx-font-size: 13px; -fx-padding: 12 0 12 0;";

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

        refreshSidebarAntrian();
    }

    private void refreshSidebarAntrian() {
        try {
            AntrianQueueService service = new AntrianQueueService();
            labelNomorAntrian.setText(String.valueOf(service.getNomorBerikutnya()));
            labelJumlahAntrian.setText(String.valueOf(service.getJumlahAntrianAktif()));
        } catch (Exception e) {
            labelNomorAntrian.setText("—");
            labelJumlahAntrian.setText("—");
        }
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
            if (result == null) { tampilkanPesanGagal(); return; }
            tampilkanHasil(result);
        });

        task.setOnFailed(e -> { setLoading(false); tampilkanPesanGagal(); });

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
        btnLanjut.setDisable(!valid);
        btnLanjut.setStyle(valid ? STYLE_BTN_AKTIF : STYLE_BTN_NONAKTIF);
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
            btnLanjut.setDisable(true);
            btnLanjut.setStyle(STYLE_BTN_NONAKTIF);
        }
    }

    @FXML
    private void handleLanjut() {
        Stage stage = (Stage) keluhanField.getScene().getWindow();
        DialogAntrian.tampilkanDialogDataPasien(stage, hasilKategori, (nama, noWA) -> {
            AntrianQueueService service = new AntrianQueueService();
            Antrian antrian = service.ambilAntrianBaru(nama, noWA, hasilKategori);
            DialogAntrian.tampilkanDialogKonfirmasi(stage, antrian, () -> {
                tampilkanToast("✅ Antrian " + antrian.getNoAntrian() + " Diterbitkan");
                resetHalaman();
            });
        });
    }

    @FXML
    private void handleLewati() {
        Stage stage = (Stage) keluhanField.getScene().getWindow();
        DialogAntrian.tampilkanDialogDataPasien(stage, "Pemeriksaan Umum", (nama, noWA) -> {
            AntrianQueueService service = new AntrianQueueService();
            Antrian antrian = service.ambilAntrianBaru(nama, noWA, "Pemeriksaan Umum");
            DialogAntrian.tampilkanDialogKonfirmasi(stage, antrian, () -> {
                tampilkanToast("✅ Antrian " + antrian.getNoAntrian() + " Diterbitkan");
                resetHalaman();
            });
        });
    }

    private void resetHalaman() {
        keluhanField.clear();
        keluhanField.requestFocus();
        labelError.setText("");
        panelHasil.setVisible(false);
        panelHasil.setManaged(false);
        labelLoading.setVisible(false);
        labelLoading.setManaged(false);
        btnLanjut.setDisable(true);
        btnLanjut.setStyle(STYLE_BTN_NONAKTIF);
        hasilKategori = "Pemeriksaan Umum";
        refreshSidebarAntrian();
    }

    private void tampilkanToast(String message) {
        labelToast.setText(message);
        toastBox.setVisible(true);
        toastBox.setManaged(true);
        toastBox.setOpacity(1.0);
        FadeTransition fade = new FadeTransition(Duration.seconds(2), toastBox);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(1.5));
        fade.setOnFinished(e -> {
            toastBox.setVisible(false);
            toastBox.setManaged(false);
        });
        fade.play();
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
