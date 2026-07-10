package com.grincare.controller;

import com.grincare.model.Antrian;
import com.grincare.repository.AntrianRepository;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class KonfirmasiAntrianController {

    @FXML private Label labelNoAntrian;
    @FXML private Label labelLayanan;
    @FXML private Label labelTicketId;
    @FXML private Label labelWa;
    @FXML private Label labelEstimasiWaktu;
    @FXML private Label labelOrangDiDepan;
    @FXML private Label labelCountdown;
    @FXML private ProgressBar progressBar;

    private static final int COUNTDOWN_DETIK = 30;
    private Timeline timeline;
    private int sisaDetik = COUNTDOWN_DETIK;

    public void setAntrianData(Antrian antrian) {
        labelNoAntrian.setText(antrian.getNoAntrian());
        labelLayanan.setText(antrian.getKategoriLayanan());
        
        // Format T-yyyy-NNN
        String ticketDisplay = antrian.getTicketId();
        try {
            String[] parts = antrian.getTicketId().split("-");
            if (parts.length >= 3) {
                ticketDisplay = "T-" + parts[1].substring(0, 4) + "-" + parts[2];
            }
        } catch (Exception ignored) {}
        
        labelTicketId.setText(ticketDisplay);

        boolean adaNomorWA = antrian.getNoWhatsApp() != null && !antrian.getNoWhatsApp().isEmpty();
        if (adaNomorWA) {
            labelWa.setText(antrian.getNoWhatsApp());
        } else {
            labelWa.setText("—");
        }

        // Hitung estimasi waktu dan orang di depan (Sesuai SRS UC04 & Backlog)
        try {
            AntrianRepository repo = new AntrianRepository();
            List<Antrian> antrianAktif = repo.getAntrianAktif();
            long orangDiDepan = antrianAktif.stream()
                .filter(a -> a.getKategoriLayanan().equals(antrian.getKategoriLayanan()))
                .filter(a -> a.getWaktuDibuat().compareTo(antrian.getWaktuDibuat()) < 0)
                .count();
            
            long estimasiMenit = (orangDiDepan + 1) * 15; // 15 menit per orang (termasuk dirinya sendiri)
            
            if (labelOrangDiDepan != null) labelOrangDiDepan.setText(orangDiDepan + " Orang");
            if (labelEstimasiWaktu != null) labelEstimasiWaktu.setText(estimasiMenit + " Menit");
        } catch (Exception e) {
            e.printStackTrace();
            if (labelOrangDiDepan != null) labelOrangDiDepan.setText("—");
            if (labelEstimasiWaktu != null) labelEstimasiWaktu.setText("—");
        }

        mulaiCountdown();
    }

    private void mulaiCountdown() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            sisaDetik--;
            labelCountdown.setText("Menutup otomatis dalam " + sisaDetik + " detik...");
            progressBar.setProgress((double) sisaDetik / COUNTDOWN_DETIK);
            
            if (sisaDetik <= 0) {
                timeline.stop();
                handleSelesai();
            }
        }));
        timeline.setCycleCount(COUNTDOWN_DETIK);
        timeline.play();
    }

    @FXML
    private void handleSelesai() {
        if (timeline != null) {
            timeline.stop();
        }
        try {
            Stage stage = (Stage) labelNoAntrian.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
