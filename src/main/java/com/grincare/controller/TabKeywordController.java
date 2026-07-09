package com.grincare.controller;

import com.grincare.model.GraphEdge;
import com.grincare.model.KategoriLayanan;
import com.grincare.repository.GraphRepository;
import com.grincare.repository.KategoriRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.Optional;
import java.util.UUID;

public class TabKeywordController {

    @FXML private TableView<GraphEdge> tabelEdge;
    @FXML private TableColumn<GraphEdge, String> colSource;
    @FXML private TableColumn<GraphEdge, String> colTarget;
    @FXML private TableColumn<GraphEdge, Integer> colWeight;
    @FXML private TableColumn<GraphEdge, Void> colAksi;

    private final GraphRepository repo = new GraphRepository();
    private final ObservableList<GraphEdge> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSource.setCellValueFactory(new PropertyValueFactory<>("source"));
        colTarget.setCellValueFactory(new PropertyValueFactory<>("target"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        
        setupKolomAksi();
        muatData();
    }

    private void muatData() {
        data.setAll(repo.getSemuaEdge());
        tabelEdge.setItems(data);
    }

    private void setupKolomAksi() {
        colAksi.setCellFactory(col -> new TableCell<GraphEdge, Void>() {
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
    private void handleTambah() {
        Optional<GraphEdge> hasil = tampilkanDialog("Hubungkan Kata Kunci", null);
        hasil.ifPresent(e -> {
            e.setId(UUID.randomUUID().toString());
            repo.tambahEdge(e);
            muatData();
        });
    }

    private void handleEdit(GraphEdge edge) {
        Optional<GraphEdge> hasil = tampilkanDialog("Edit Hubungan Kata Kunci", edge);
        hasil.ifPresent(e -> {
            repo.updateEdge(edge.getId(), e);
            muatData();
        });
    }

    private void handleHapus(GraphEdge edge) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus hubungan \"" + edge.getSource() + " → " + edge.getTarget() + "\"?");
        konfirmasi.setContentText("Data yang dihapus tidak dapat dikembalikan.");
        konfirmasi.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                repo.hapusEdge(edge.getId());
                muatData();
            }
        });
    }

    private Optional<GraphEdge> tampilkanDialog(String judul, GraphEdge existing) {
        Dialog<GraphEdge> dialog = new Dialog<>();
        dialog.setTitle(judul);
        dialog.setHeaderText(null);

        ButtonType simpanType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(simpanType, ButtonType.CANCEL);

        TextField sourceField = new TextField();
        sourceField.setPromptText("Kata kunci asal (misal: nyeri, tartar)");
        sourceField.setPrefWidth(300);

        ComboBox<String> targetCombo = new ComboBox<>();
        targetCombo.setPromptText("Pilih simpul tujuan (kategori)");
        targetCombo.setPrefWidth(300);

        // Muat daftar kategori dari KategoriRepository
        KategoriRepository katRepo = new KategoriRepository();
        for (KategoriLayanan kat : katRepo.getSemuaKategori()) {
            targetCombo.getItems().add(kat.getNama());
        }

        TextField weightField = new TextField();
        weightField.setPromptText("Bobot hubungan (1-100)");
        weightField.setPrefWidth(300);

        if (existing != null) {
            sourceField.setText(existing.getSource());
            targetCombo.setValue(existing.getTarget());
            weightField.setText(String.valueOf(existing.getWeight()));
        } else {
            weightField.setText("5"); // default weight
            if (!targetCombo.getItems().isEmpty()) {
                targetCombo.getSelectionModel().selectFirst();
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16, 16, 16, 16));
        grid.add(new Label("Kata Kunci Asal:"), 0, 0);
        grid.add(sourceField, 1, 0);
        grid.add(new Label("Tujuan (Target):"), 0, 1);
        grid.add(targetCombo, 1, 1);
        grid.add(new Label("Bobot (1-100):"), 0, 2);
        grid.add(weightField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        Node simpanBtn = dialog.getDialogPane().lookupButton(simpanType);
        
        // Form validation listener
        Runnable validasiForm = () -> {
            String src = sourceField.getText().trim();
            String tgt = targetCombo.getValue() != null ? targetCombo.getValue().trim() : "";
            String wStr = weightField.getText().trim();
            
            boolean valid = !src.isEmpty() && !tgt.isEmpty();
            if (valid) {
                try {
                    int w = Integer.parseInt(wStr);
                    valid = w >= 1 && w <= 100;
                } catch (NumberFormatException e) {
                    valid = false;
                }
            }
            simpanBtn.setDisable(!valid);
        };

        sourceField.textProperty().addListener((obs, old, val) -> validasiForm.run());
        targetCombo.valueProperty().addListener((obs, old, val) -> validasiForm.run());
        weightField.textProperty().addListener((obs, old, val) -> validasiForm.run());

        // Jalankan validasi awal
        validasiForm.run();

        dialog.setResultConverter(bt -> {
            if (bt == simpanType) {
                GraphEdge edge = new GraphEdge();
                edge.setId(existing != null ? existing.getId() : "");
                edge.setSource(sourceField.getText().trim());
                edge.setTarget(targetCombo.getValue() != null ? targetCombo.getValue().trim() : "");
                int weight = 5;
                try {
                    weight = Integer.parseInt(weightField.getText().trim());
                } catch (NumberFormatException ignored) {}
                edge.setWeight(weight);
                return edge;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
