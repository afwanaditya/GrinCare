package com.grincare.controller;

import com.grincare.model.RekapHarian;
import com.grincare.repository.RekapRepository;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Comparator;
import java.util.List;

public class TabStatistikController {
    @FXML private LineChart<String, Number> chartTrenSelesai;
    @FXML private CategoryAxis xAxisTren;
    @FXML private NumberAxis yAxisTren;

    @FXML private PieChart pieChartKategori;

    @FXML private Label labelSelesaiHariIni;
    @FXML private Label labelMenungguHariIni;
    @FXML private Label labelSelesaiTotal;

    private final RekapRepository repo = new RekapRepository();

    private String snapshotTrenTerakhir = null;
    private String snapshotKategoriTerakhir = null;

    @FXML
    public void initialize() {
        chartTrenSelesai.setAnimated(false);
        pieChartKategori.setAnimated(false);

        muatTrenSelesai();
        muatKategoriHariIni();

        Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            muatTrenSelesai();
            muatKategoriHariIni();
        }));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    // Line Chart
    private void muatTrenSelesai() {
        if (chartTrenSelesai == null) return;

        List<RekapHarian> semuaRekap = repo.getSemuaRekap();
        semuaRekap.sort(Comparator.comparing(RekapHarian::getTanggal));

        // Hitung total selesai sepanjang waktu
        int totalSelesaiAllTime = 0;
        for (RekapHarian r : semuaRekap) {
            totalSelesaiAllTime += r.getJumlahSelesai();
        }
        if (labelSelesaiTotal != null) {
            labelSelesaiTotal.setText(String.valueOf(totalSelesaiAllTime));
        }

        StringBuilder sb = new StringBuilder();
        for (RekapHarian r : semuaRekap) {
            sb.append(r.getTanggal()).append('=').append(r.getJumlahSelesai()).append(';');
        }
        String snapshot = sb.toString();
        if (snapshot.equals(snapshotTrenTerakhir)) return; // data belum berubah, jangan render ulang
        snapshotTrenTerakhir = snapshot;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Pasien Selesai");
        for (RekapHarian r : semuaRekap) {
            series.getData().add(new XYChart.Data<>(r.getTanggal(), r.getJumlahSelesai()));
        }
        chartTrenSelesai.getData().clear();
        chartTrenSelesai.getData().add(series);
    }

    // Pie Chart
    private void muatKategoriHariIni() {
        if (pieChartKategori == null) return;

        RekapHarian hariIni = repo.getRekapHariIni();
        int umum     = hariIni.getJumlahPemeriksaanUmum();
        int scaling  = hariIni.getJumlahScaling();
        int kontrol  = hariIni.getJumlahKontrol();
        int estetika = hariIni.getJumlahKonsultasiEstetika();

        if (labelSelesaiHariIni != null) {
            labelSelesaiHariIni.setText(String.valueOf(hariIni.getJumlahSelesai()));
        }
        if (labelMenungguHariIni != null) {
            labelMenungguHariIni.setText(String.valueOf(hariIni.getJumlahMenunggu()));
        }

        String snapshot = umum + "," + scaling + "," + kontrol + "," + estetika;
        if (snapshot.equals(snapshotKategoriTerakhir)) return; // data belum berubah, jangan render ulang
        snapshotKategoriTerakhir = snapshot;

        int total = umum + scaling + kontrol + estetika;
        ObservableList<PieChart.Data> slices = FXCollections.observableArrayList(
            new PieChart.Data(labelSlice(RekapHarian.KATEGORI_PEMERIKSAAN_UMUM, umum, total), umum),
            new PieChart.Data(labelSlice(RekapHarian.KATEGORI_SCALING, scaling, total), scaling),
            new PieChart.Data(labelSlice(RekapHarian.KATEGORI_KONTROL, kontrol, total), kontrol),
            new PieChart.Data(labelSlice(RekapHarian.KATEGORI_KONSULTASI_ESTETIKA, estetika, total), estetika)
        );
        pieChartKategori.setData(slices);
    }

    // Format label tiap slice: "Nama Kategori: jumlah (persen%)"
    private String labelSlice(String nama, int jumlah, int total) {
        double persen = (total == 0) ? 0 : (jumlah * 100.0 / total);
        return String.format("%s: %d (%.1f%%)", nama, jumlah, persen);
    }
}
