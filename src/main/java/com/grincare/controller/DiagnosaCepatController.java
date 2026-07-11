package com.grincare.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.grincare.service.DiagnosaTreeService;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DiagnosaCepatController implements Initializable {

    @FXML
    private Label lblPertanyaan;

    @FXML
    private Button btnPilihan1;

    @FXML
    private Button btnPilihan2;

    @FXML
    private Label lblHasil;

    @FXML
    private Button btnUlang;

    private DiagnosaTreeService treeService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        treeService = new DiagnosaTreeService();

        tampilkanPertanyaan();

        lblHasil.setText("");
        btnUlang.setVisible(false);

    }

    /**
     * Menampilkan pertanyaan dan pilihan jawaban
     */
    private void tampilkanPertanyaan() {

        if (treeService.selesai()) {

            tampilkanHasil();

            return;

        }

        lblPertanyaan.setText(treeService.getPertanyaan());

        List<String> pilihan = treeService.getPilihan();

        if (pilihan.size() >= 2) {

            btnPilihan1.setText(pilihan.get(0));
            btnPilihan2.setText(pilihan.get(1));

        }

    }

    /**
     * Event tombol pilihan pertama
     */
    @FXML
    private void pilihPertama() {

        treeService.pilihJawaban(btnPilihan1.getText());

        tampilkanPertanyaan();

    }

    /**
     * Event tombol pilihan kedua
     */
    @FXML
    private void pilihKedua() {

        treeService.pilihJawaban(btnPilihan2.getText());

        tampilkanPertanyaan();

    }

    /**
     * Menampilkan hasil diagnosa
     */
private void tampilkanHasil() {

    String hasil = treeService.getHasilDiagnosa();

    System.out.println("HASIL = " + hasil);

    lblPertanyaan.setText("Diagnosa Sementara");

    lblHasil.setText(
            "Kemungkinan penyakit :\n\n"
                    + hasil
                    + "\n\nSilakan lakukan pemeriksaan lebih lanjut di dokter gigi."
    );

    btnPilihan1.setVisible(false);
    btnPilihan2.setVisible(false);

    btnPilihan1.setManaged(false);
    btnPilihan2.setManaged(false);

    btnUlang.setVisible(true);

}

    /**
     * Mengulang diagnosa
     */
    @FXML
    private void ulangDiagnosa() {

        treeService.reset();

        btnPilihan1.setDisable(false);
        btnPilihan2.setDisable(false);

        lblHasil.setText("");

        btnUlang.setVisible(false);

        tampilkanPertanyaan();

    }

    /**
     * Kirim hasil diagnosa kembali ke parent controller tanpa membuka Ambil Antrian.
     */
    @FXML
    private void ambilAntrian() {

        String hasil = treeService.getHasilDiagnosa();
        String kategori = mapHasilKeKategori(hasil);

        if (parentController != null) {
            parentController.setHasilDiagnosa(kategori);
            Stage stage = (Stage) btnUlang.getScene().getWindow();
            stage.close();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informasi");
        alert.setHeaderText(null);
        alert.setContentText(
                "Fitur ini nantinya akan diarahkan ke menu Ambil Antrian."
        );
        alert.showAndWait();

    }

    private String mapHasilKeKategori(String hasilDiagnosa) {
        if (hasilDiagnosa == null) {
            return "Pemeriksaan Umum";
        }

        switch (hasilDiagnosa.trim()) {
            case "Karang Gigi":
                return "Scaling";
            case "Karies Ringan":
            case "Gingivitis":
                return "Tambal Gigi";
            case "Gigi Sensitif":
                return "Konsultasi";
            case "Pemeriksaan Umum":
                return "Pemeriksaan Umum";
            default:
                return "Pemeriksaan Umum";
        }
    }

    private KonsultasiController parentController;

    public void setParentController(KonsultasiController controller) {
        this.parentController = controller;
    }

}