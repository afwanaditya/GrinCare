package com.grincare.controller;

import com.grincare.model.Antrian;
import com.grincare.model.RekapHarian;
import com.grincare.repository.AntrianRepository;
import com.grincare.repository.RekapRepository;
import com.grincare.service.WhatsAppService;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class TabAntrianController {
    @FXML private TableView<Antrian>           tabelAntrian;
    @FXML private TableColumn<Antrian, String> colNoAntrian;
    @FXML private TableColumn<Antrian, String> colNama;
    @FXML private TableColumn<Antrian, String> colNoWA;
    @FXML private TableColumn<Antrian, String> colKategori;
    @FXML private TableColumn<Antrian, String> colStatus;
    @FXML private TableColumn<Antrian, Void>   colAksi;
    @FXML private Label                        labelJumlahAktif;
    @FXML private HBox                         toastBox;
    @FXML private Label                        labelToast;

    private final AntrianRepository antrianRepo = new AntrianRepository();
    private final RekapRepository   rekapRepo   = new RekapRepository();
    private final WhatsAppService   waService   = new WhatsAppService();

    private final ObservableList<Antrian> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupKolom();
        loadDataAntrian();
    }

    private void setupKolom() {
        colNoAntrian.setCellValueFactory(new PropertyValueFactory<>("noAntrian"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategoriLayanan"));
        setupKolomNoWA();
        setupKolomStatus();
        setupKolomAksi();
    }

    private void setupKolomNoWA() {
        colNoWA.setCellValueFactory(cd -> {
            String wa = cd.getValue().getNoWhatsApp();
            return new SimpleStringProperty((wa == null || wa.trim().isEmpty()) ? "-" : wa);
        });
    }

    private void setupKolomStatus() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<Antrian, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add(getBadgeClass(status));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void setupKolomAksi() {
        colAksi.setCellFactory(col -> new TableCell<Antrian, Void>() {
            private final Button btnPanggil = new Button("Panggil");
            private final Button btnSelesai = new Button("Selesai");
            private final HBox   aksiBox    = new HBox(6, btnPanggil, btnSelesai);

            {
                aksiBox.setStyle("-fx-alignment: CENTER_LEFT;");
                btnPanggil.getStyleClass().add("btn-panggil");
                btnSelesai.getStyleClass().add("btn-selesai");
                btnPanggil.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        onPanggilPasien(getTableView().getItems().get(idx));
                    }
                });
                btnSelesai.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getTableView().getItems().size()) {
                        onTandaiSelesai(getTableView().getItems().get(idx));
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Antrian a         = getTableView().getItems().get(getIndex());
                    boolean menunggu  = Antrian.STATUS_MENUNGGU.equals(a.getStatus());
                    boolean dipanggil = Antrian.STATUS_DIPANGGIL.equals(a.getStatus());
                    btnPanggil.setDisable(!menunggu);
                    btnSelesai.setDisable(!dipanggil);
                    setGraphic(aksiBox);
                }
            }
        });
    }

    private void loadDataAntrian() {
        List<Antrian> aktif = antrianRepo.getAntrianAktif();
        data.setAll(aktif);
        tabelAntrian.setItems(data);
        labelJumlahAktif.setText(String.valueOf(aktif.size()));
    }

    private void onPanggilPasien(Antrian antrian) {
        antrianRepo.updateStatus(antrian.getTicketId(), Antrian.STATUS_DIPANGGIL);
        kirimNotifikasiWaBackground(antrian.getNoWhatsApp(), antrian.getNoAntrian());
        loadDataAntrian();
        tampilkanToast("Pasien " + antrian.getNoAntrian() + " dipanggil");
    }

    private void kirimNotifikasiWaBackground(String noWA, String noAntrian) {
        if (noWA == null || noWA.trim().isEmpty()) return;
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() {
                waService.kirimNotifikasiPanggil(noWA, noAntrian);
                return null;
            }
        };
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void onTandaiSelesai(Antrian antrian) {
        antrianRepo.updateStatus(antrian.getTicketId(), Antrian.STATUS_SELESAI);
        updateRekapHarian(antrian);
        loadDataAntrian();
    }

    private void updateRekapHarian(Antrian antrian) {
        try {
            RekapHarian rekap = rekapRepo.getRekapHariIni();
            rekap.setJumlahSelesai(rekap.getJumlahSelesai() + 1);
            tambahKategoriRekap(rekap, antrian.getKategoriLayanan());
            rekapRepo.simpanRekap(rekap);
        } catch (Exception e) {
            System.err.println("[TabAntrianController] Gagal update rekap harian: " + e.getMessage());
        }
    }

    private void tambahKategoriRekap(RekapHarian rekap, String kategori) {
        if (kategori == null) return;
        String k = kategori.trim();
        if (k.equalsIgnoreCase(RekapHarian.KATEGORI_PEMERIKSAAN_UMUM)) {
            rekap.setJumlahPemeriksaanUmum(rekap.getJumlahPemeriksaanUmum() + 1);
        } else if (k.equalsIgnoreCase(RekapHarian.KATEGORI_SCALING)) {
            rekap.setJumlahScaling(rekap.getJumlahScaling() + 1);
        } else if (k.equalsIgnoreCase(RekapHarian.KATEGORI_KONTROL)) {
            rekap.setJumlahKontrol(rekap.getJumlahKontrol() + 1);
        } else if (k.toLowerCase().contains("estetika")) {
            rekap.setJumlahKonsultasiEstetika(rekap.getJumlahKonsultasiEstetika() + 1);
        }
    }

    @FXML
    private void onHapusDataSelesai() {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus semua data antrian yang sudah selesai?");
        konfirmasi.setContentText("Data yang dihapus tidak dapat dikembalikan.");
        konfirmasi.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                hapusSemuaSelesai();
            }
        });
    }

    private void hapusSemuaSelesai() {
        List<Antrian> selesai = antrianRepo.getSemuaAntrian().stream()
                .filter(a -> Antrian.STATUS_SELESAI.equals(a.getStatus()))
                .collect(Collectors.toList());
        for (Antrian a : selesai) {
            antrianRepo.hapusAntrian(a.getTicketId());
        }
        loadDataAntrian();
        tampilkanToast("Dihapus " + selesai.size() + " antrian selesai");
    }

    private String getBadgeClass(String status) {
        switch (status) {
            case Antrian.STATUS_DIPANGGIL: return "badge-dipanggil";
            case Antrian.STATUS_SELESAI:   return "badge-selesai";
            default:                        return "badge-menunggu";
        }
    }

    private void tampilkanToast(String pesan) {
        labelToast.setText(pesan);
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
}
