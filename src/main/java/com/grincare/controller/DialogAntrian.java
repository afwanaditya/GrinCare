package com.grincare.controller;

import com.grincare.model.Antrian;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DialogAntrian {

    public interface KonfirmasiCallback {
        void onKonfirmasi(String nama, String noWA);
    }

    public static void tampilkanDialogDataPasien(Stage owner, String kategori, KonfirmasiCallback callback) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Data Pasien");
        dialog.setResizable(false);

        VBox root = new VBox(20);
        root.setPrefWidth(440);
        root.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 28 28 24 28;"
        );

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(4);
        Label title = new Label("Data Pasien");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label subtitle = new Label("Untuk layanan: " + kategori);
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #2563eb;");
        headerText.getChildren().addAll(title, subtitle);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Button btnClose = new Button("✕");
        btnClose.setStyle(
            "-fx-background-color: transparent; -fx-border-color: transparent;" +
            "-fx-text-fill: #94a3b8; -fx-font-size: 16px; -fx-cursor: hand; -fx-padding: 0 4 0 4;"
        );
        btnClose.setOnAction(e -> dialog.close());
        header.getChildren().addAll(headerText, headerSpacer, btnClose);

        // Separator
        Region sep1 = new Region();
        sep1.setMaxWidth(Double.MAX_VALUE);
        sep1.setStyle("-fx-background-color: #e2e8f0; -fx-pref-height: 1; -fx-max-height: 1;");

        // Form
        VBox form = new VBox(14);

        VBox namaGroup = new VBox(6);
        Label labelNama = new Label("👤  Nama Lengkap");
        labelNama.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField namaField = new TextField();
        namaField.setPromptText("Masukkan nama lengkap...");
        namaField.setStyle(
            "-fx-background-color: #f8fafc; -fx-background-radius: 8;" +
            "-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-border-width: 1;" +
            "-fx-padding: 10 14 10 14; -fx-font-size: 13px;"
        );
        namaGroup.getChildren().addAll(labelNama, namaField);

        VBox waGroup = new VBox(6);
        Label labelWA = new Label("📞  Nomor WhatsApp (opsional)");
        labelWA.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField waField = new TextField();
        waField.setPromptText("Contoh: 08123456789");
        waField.setStyle(
            "-fx-background-color: #f8fafc; -fx-background-radius: 8;" +
            "-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-border-width: 1;" +
            "-fx-padding: 10 14 10 14; -fx-font-size: 13px;"
        );
        Label hintWA = new Label("Tiket akan dikirim ke WhatsApp Anda jika diisi.");
        hintWA.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        waGroup.getChildren().addAll(labelWA, waField, hintWA);

        Label labelError = new Label("");
        labelError.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");

        form.getChildren().addAll(namaGroup, waGroup, labelError);

        // Separator 2
        Region sep2 = new Region();
        sep2.setMaxWidth(Double.MAX_VALUE);
        sep2.setStyle("-fx-background-color: #e2e8f0; -fx-pref-height: 1; -fx-max-height: 1;");

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnBatal = new Button("Batal");
        btnBatal.setStyle(
            "-fx-background-color: white; -fx-border-color: #e2e8f0;" +
            "-fx-border-radius: 8; -fx-border-width: 1; -fx-background-radius: 8;" +
            "-fx-text-fill: #64748b; -fx-font-size: 13px;" +
            "-fx-padding: 10 20 10 20; -fx-cursor: hand;"
        );
        btnBatal.setOnAction(e -> dialog.close());

        Button btnKonfirmasi = new Button("Konfirmasi & Ambil Antrian");
        btnKonfirmasi.setStyle(
            "-fx-background-color: #2563eb; -fx-text-fill: white;" +
            "-fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-padding: 10 20 10 20; -fx-cursor: hand;"
        );
        btnKonfirmasi.setOnAction(e -> {
            String nama = namaField.getText().trim();
            if (nama.isEmpty()) {
                labelError.setText("Nama tidak boleh kosong.");
                namaField.requestFocus();
                return;
            }
            dialog.close();
            callback.onKonfirmasi(nama, waField.getText().trim());
        });

        footer.getChildren().addAll(btnBatal, btnKonfirmasi);

        root.getChildren().addAll(header, sep1, form, sep2, footer);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void tampilkanDialogKonfirmasi(Stage owner, Antrian antrian, Runnable onSelesai) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Konfirmasi Antrian");
        dialog.setResizable(false);

        // TKT-yyyyMMdd-NNN → T-yyyy-NNN
        String ticketDisplay = antrian.getTicketId();
        try {
            String[] parts = antrian.getTicketId().split("-");
            if (parts.length >= 3) {
                ticketDisplay = "T-" + parts[1].substring(0, 4) + "-" + parts[2];
            }
        } catch (Exception ignored) {}

        final boolean[] selesaiDipanggil = {false};
        Runnable selesaiSekali = () -> {
            if (!selesaiDipanggil[0]) {
                selesaiDipanggil[0] = true;
                onSelesai.run();
            }
        };

        VBox root = new VBox(14);
        root.setPrefWidth(440);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 28 28 24 28;"
        );

        // Ikon hijau bulat
        StackPane iconCircle = new StackPane();
        iconCircle.setMaxWidth(72);
        iconCircle.setStyle(
            "-fx-background-color: #dcfce7; -fx-background-radius: 40;" +
            "-fx-min-width: 72; -fx-min-height: 72;" +
            "-fx-pref-width: 72; -fx-pref-height: 72;"
        );
        Label iconLabel = new Label("🎫");
        iconLabel.setStyle("-fx-font-size: 30px;");
        iconCircle.getChildren().add(iconLabel);

        // Ticket ID
        Label labelTicketId = new Label(ticketDisplay);
        labelTicketId.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // Nomor antrian besar
        Label labelNoAntrian = new Label(antrian.getNoAntrian());
        labelNoAntrian.setStyle(
            "-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #2563eb;"
        );

        // Detail rows
        VBox details = new VBox(8);
        details.setMaxWidth(Double.MAX_VALUE);
        details.setStyle(
            "-fx-background-color: #f8fafc; -fx-background-radius: 12;" +
            "-fx-padding: 16 18 16 18;"
        );
        String noWADisplay = (antrian.getNoWhatsApp() == null || antrian.getNoWhatsApp().isEmpty())
            ? "—" : antrian.getNoWhatsApp();
        details.getChildren().addAll(
            buatDetailRow("👤  Nama", antrian.getNama(), false, "#1e293b"),
            buatDetailRow("📞  WhatsApp", noWADisplay, false, "#1e293b"),
            buatDetailRow("🦷  Layanan", antrian.getKategoriLayanan(), true, "#1e293b"),
            buatDetailRow("⏱  Estimasi", "15 Menit", false, "#15803d")
        );

        // Catatan WA
        boolean adaNomorWA = antrian.getNoWhatsApp() != null && !antrian.getNoWhatsApp().isEmpty();
        Label waNote = new Label("📲  Tiket telah dikirim ke WhatsApp Anda");
        waNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        waNote.setVisible(adaNomorWA);
        waNote.setManaged(adaNomorWA);

        // Progress bar countdown
        ProgressBar progressBar = new ProgressBar(1.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(6);
        progressBar.setStyle("-fx-accent: #2563eb;");

        Label labelCountdown = new Label("Menutup otomatis dalam 30 detik...");
        labelCountdown.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        // Tombol pasien berikutnya
        Button btnBerikutnya = new Button("Pasien Berikutnya →");
        btnBerikutnya.setMaxWidth(Double.MAX_VALUE);
        btnBerikutnya.setStyle(
            "-fx-background-color: #2563eb; -fx-text-fill: white;" +
            "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;" +
            "-fx-padding: 13 0 13 0; -fx-cursor: hand;"
        );

        root.getChildren().addAll(
            iconCircle, labelTicketId, labelNoAntrian,
            details, waNote, progressBar, labelCountdown, btnBerikutnya
        );

        Scene scene = new Scene(root);
        dialog.setScene(scene);

        // Tampilkan dialog dulu (non-blocking), lalu mulai countdown
        dialog.show();

        // Countdown 30 detik — dijalankan SETELAH dialog.show()
        final int[] sisa = {30};
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            sisa[0]--;
            labelCountdown.setText("Menutup otomatis dalam " + sisa[0] + " detik...");
            progressBar.setProgress((double) sisa[0] / 30.0);
            if (sisa[0] <= 0) {
                dialog.close();
            }
        }));
        timeline.setCycleCount(30);

        // setOnHidden menangkap semua cara dialog ditutup (tombol, timer, maupun X)
        dialog.setOnHidden(e -> {
            timeline.stop();
            selesaiSekali.run();
        });

        btnBerikutnya.setOnAction(e -> dialog.close());

        timeline.play();
    }

    private static HBox buatDetailRow(String label, String value, boolean bold, String valueColor) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-min-width: 120;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label val = new Label(value);
        String weightPart = bold ? " -fx-font-weight: bold;" : "";
        val.setStyle("-fx-font-size: 13px; -fx-text-fill: " + valueColor + ";" + weightPart);
        row.getChildren().addAll(lbl, spacer, val);
        return row;
    }
}
