package com.grincare.service;

import java.util.ArrayList;
import java.util.List;

public class DiagnosaTreeService {

    // ==========================
    // Inner Class Node
    // ==========================
    public static class Node {

        private String pertanyaan;
        private String hasil;
        private List<Pilihan> pilihan;

        public Node(String pertanyaan) {
            this.pertanyaan = pertanyaan;
            this.pilihan = new ArrayList<>();
        }

        public Node(String pertanyaan, String hasil) {
            this.pertanyaan = pertanyaan;
            this.hasil = hasil;
            this.pilihan = new ArrayList<>();
        }

        public void tambahPilihan(String jawaban, Node tujuan) {
            pilihan.add(new Pilihan(jawaban, tujuan));
        }

        public boolean isLeaf() {
            return hasil != null;
        }

        public String getPertanyaan() {
            return pertanyaan;
        }

        public String getHasil() {
            return hasil;
        }

        public List<Pilihan> getPilihan() {
            return pilihan;
        }
    }

    // ==========================
    // Inner Class Pilihan
    // ==========================
    public static class Pilihan {

        private String jawaban;
        private Node tujuan;

        public Pilihan(String jawaban, Node tujuan) {
            this.jawaban = jawaban;
            this.tujuan = tujuan;
        }

        public String getJawaban() {
            return jawaban;
        }

        public Node getTujuan() {
            return tujuan;
        }
    }

    // ==========================
    // Root Tree
    // ==========================
    private Node root;
    private Node currentNode;

    public DiagnosaTreeService() {
        bangunTree();
        currentNode = root;
    }

    // ==========================
    // Membangun Tree
    // ==========================
    private void bangunTree() {

        // Root
        root = new Node("Apakah gigi Anda terasa sakit?");

        // Pertanyaan
        Node lubang = new Node("Apakah terdapat lubang pada gigi?");
        Node gusi = new Node("Apakah gusi Anda berdarah?");
        Node sensitif = new Node("Apakah gigi terasa ngilu saat minum dingin?");
        Node karang = new Node("Apakah terdapat karang gigi?");

        // Hasil
        Node kariesRingan =
                new Node("", "Karies Ringan");

        Node gingivitis =
                new Node("", "Gingivitis");

        Node gigiSensitif =
                new Node("", "Gigi Sensitif");

        Node scaling =
                new Node("", "Karang Gigi");

        Node pemeriksaan =
                new Node("", "Pemeriksaan Umum");

        // Hubungan Tree

        root.tambahPilihan("Ya", lubang);
        root.tambahPilihan("Tidak", karang);

        lubang.tambahPilihan("Ya", sensitif);
        lubang.tambahPilihan("Tidak", gusi);

        sensitif.tambahPilihan("Ya", gigiSensitif);
        sensitif.tambahPilihan("Tidak", kariesRingan);

        gusi.tambahPilihan("Ya", gingivitis);
        gusi.tambahPilihan("Tidak", pemeriksaan);

        karang.tambahPilihan("Ya", scaling);
        karang.tambahPilihan("Tidak", pemeriksaan);

    }

    // ==========================
    // Mengambil Pertanyaan
    // ==========================
    public String getPertanyaan() {
        return currentNode.getPertanyaan();
    }

    // ==========================
    // Mengambil Pilihan
    // ==========================
    public List<String> getPilihan() {

        List<String> hasil = new ArrayList<>();

        for (Pilihan p : currentNode.getPilihan()) {
            hasil.add(p.getJawaban());
        }

        return hasil;

    }

    // ==========================
    // Memilih Jawaban
    // ==========================
   public void pilihJawaban(String jawaban) {

    System.out.println("Jawaban = " + jawaban);

    for (Pilihan p : currentNode.getPilihan()) {

        System.out.println("Pilihan : " + p.getJawaban());

        if (p.getJawaban().equalsIgnoreCase(jawaban)) {

            currentNode = p.getTujuan();

            System.out.println("Pindah ke : " + currentNode.getPertanyaan());
            System.out.println("Hasil : " + currentNode.getHasil());

            return;

        }

    }

}

    // ==========================
    // Apakah Sudah Sampai Hasil
    // ==========================
    public boolean selesai() {
        return currentNode.isLeaf();
    }

    // ==========================
    // Mengambil Hasil Diagnosa
    // ==========================
    public String getHasilDiagnosa() {
        return currentNode.getHasil();
    }

    // ==========================
    // Mengulang Diagnosa
    // ==========================
    public void reset() {
        currentNode = root;
    }

}