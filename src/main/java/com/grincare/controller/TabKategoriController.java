package com.grincare.controller;

import com.grincare.model.KategoriLayanan;
import com.grincare.repository.KategoriRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;
import java.util.UUID;

public class TabKategoriController {

    @FXML private TableView<KategoriLayanan> tabelKategori;
    @FXML private TableColumn<KategoriLayanan, String> colNama;
    @FXML private TableColumn<KategoriLayanan, String> colDeskripsi;
    @FXML private TableColumn<KategoriLayanan, Void> colAksi;

    private final KategoriRepository repo = new KategoriRepository();
    private final ObservableList<KategoriLayanan> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        setupKolomAksi();
        muatData();
    }

    private void muatData() {
        data.setAll(repo.getSemuaKategori());
        tabelKategori.setItems(data);
    }

    private void setupKolomAksi() {
        colAksi.setCellFactory(col -> new TableCell<KategoriLayanan, Void>() {
            private final Button btnEdit  = new Button("Edit");
            private final Button btnHapus = new Button("Hapus");

            {
                btnEdit.setStyle("-fx-font-size: 11px;");
                btnHapus.setStyle("-fx-font-size: 11px; -fx-text-fill: #cc0000;");

                btnEdit.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    handleEdit(getTableView().getItems().get(idx));
                });

                btnHapus.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) return;
                    handleHapus(getTableView().getItems().get(idx));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(6, btnEdit, btnHapus);
                    box.setStyle("-fx-alignment: CENTER_LEFT;");
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    private void handleNavKonsultasi() {
        try {
            Stage stage = (Stage) tabelKategori.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Konsultasi.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNavAmbilAntrian() {
        try {
            Stage stage = (Stage) tabelKategori.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/AmbilAntrian.fxml"));
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambah() {
        Optional<KategoriLayanan> hasil = tampilkanDialog("Tambah Kategori", null);
        hasil.ifPresent(k -> {
            k.setId(UUID.randomUUID().toString());
            repo.tambahKategori(k);
            muatData();
        });
    }

    private void handleEdit(KategoriLayanan kategori) {
        Optional<KategoriLayanan> hasil = tampilkanDialog("Edit Kategori", kategori);
        hasil.ifPresent(k -> {
            repo.updateKategori(kategori.getId(), k);
            muatData();
        });
    }

    private void handleHapus(KategoriLayanan kategori) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus kategori \"" + kategori.getNama() + "\"?");
        konfirmasi.setContentText("Data yang dihapus tidak dapat dikembalikan.");
        konfirmasi.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                repo.hapusKategori(kategori.getId());
                muatData();
            }
        });
    }

    private Optional<KategoriLayanan> tampilkanDialog(String judul, KategoriLayanan existing) {
        Dialog<KategoriLayanan> dialog = new Dialog<>();
        dialog.setTitle(judul);
        dialog.setHeaderText(null);

        ButtonType simpanType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(simpanType, ButtonType.CANCEL);

        TextField namaField = new TextField();
        namaField.setPromptText("Nama kategori");
        namaField.setPrefWidth(300);

        TextField deskripsiField = new TextField();
        deskripsiField.setPromptText("Deskripsi singkat");
        deskripsiField.setPrefWidth(300);

        if (existing != null) {
            namaField.setText(existing.getNama());
            deskripsiField.setText(existing.getDeskripsi());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 16, 16, 16));
        grid.add(new Label("Nama:"), 0, 0);
        grid.add(namaField, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1);
        grid.add(deskripsiField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Node simpanBtn = dialog.getDialogPane().lookupButton(simpanType);
        simpanBtn.setDisable(namaField.getText().trim().isEmpty());
        namaField.textProperty().addListener((obs, old, val) ->
                simpanBtn.setDisable(val.trim().isEmpty()));

        dialog.setResultConverter(bt -> {
            if (bt == simpanType) {
                KategoriLayanan k = new KategoriLayanan();
                k.setId(existing != null ? existing.getId() : "");
                k.setNama(namaField.getText().trim());
                k.setDeskripsi(deskripsiField.getText().trim());
                return k;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
