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

        // Root: menentukan cabang utama (Sakit/Nyeri vs Tujuan Kunjungan Lain)
        root = new Node("Apakah gigi atau gusi Anda saat ini terasa sakit atau nyeri?");

        // ================= Cabang 1: Sakit / Nyeri =================
        Node sakitPenyebab = new Node(
                "Apakah rasa sakit disebabkan oleh gigi berlubang atau gusi yang bengkak/berdarah?");
        Node sakitBehel = new Node(
                "Apakah nyeri tersebut muncul di area gigi yang sedang memakai kawat gigi/behel?");

        // ================= Cabang penentu Perawatan Rutin vs Konsultasi Estetika =================
        Node tujuanKunjungan = new Node(
                "Apakah tujuan kunjungan Anda untuk perawatan rutin (bukan karena sakit)?");

        // ================= Cabang 2: Perawatan Rutin =================
        Node rutinKarang = new Node(
                "Apakah Anda ingin membersihkan karang gigi atau plak yang menumpuk?");
        Node rutinBehel = new Node(
                "Apakah Anda sedang menjalani perawatan behel/kawat gigi dan perlu kontrol berkala?");

        // ================= Cabang 3: Konsultasi Estetika =================
        Node estetika = new Node(
                "Apakah Anda tertarik dengan perawatan estetika gigi seperti whitening atau veneer?");

        // ================= Leaf: 4 kategori layanan final =================
        Node pemeriksaanUmum1 = new Node("", "Pemeriksaan Umum");
        Node kontrol1 = new Node("", "Kontrol");
        Node pemeriksaanUmum2 = new Node("", "Pemeriksaan Umum");
        Node scaling = new Node("", "Scaling");
        Node kontrol2 = new Node("", "Kontrol");
        Node pemeriksaanUmum3 = new Node("", "Pemeriksaan Umum");
        Node konsultasiEstetika = new Node("", "Konsultasi Estetika");
        Node pemeriksaanUmum4 = new Node("", "Pemeriksaan Umum");

        // Hubungan Tree

        root.tambahPilihan("Ya", sakitPenyebab);
        root.tambahPilihan("Tidak", tujuanKunjungan);

        sakitPenyebab.tambahPilihan("Ya", pemeriksaanUmum1);
        sakitPenyebab.tambahPilihan("Tidak", sakitBehel);

        sakitBehel.tambahPilihan("Ya", kontrol1);
        sakitBehel.tambahPilihan("Tidak", pemeriksaanUmum2);

        tujuanKunjungan.tambahPilihan("Ya", rutinKarang);
        tujuanKunjungan.tambahPilihan("Tidak", estetika);

        rutinKarang.tambahPilihan("Ya", scaling);
        rutinKarang.tambahPilihan("Tidak", rutinBehel);

        rutinBehel.tambahPilihan("Ya", kontrol2);
        rutinBehel.tambahPilihan("Tidak", pemeriksaanUmum3);

        estetika.tambahPilihan("Ya", konsultasiEstetika);
        estetika.tambahPilihan("Tidak", pemeriksaanUmum4);

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
    // Mengambil Hasil Rekomendasi Kategori Layanan
    // ==========================
    public String getHasilRekomendasi() {
        return currentNode.getHasil();
    }

    // ==========================
    // Mengulang Proses Tanya-Jawab
    // ==========================
    public void reset() {
        currentNode = root;
    }

}