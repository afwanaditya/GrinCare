package com.grincare.util;

import com.grincare.model.GraphEdge;
import com.grincare.model.KategoriLayanan;
import com.grincare.repository.GraphRepository;
import com.grincare.repository.KategoriRepository;
import java.util.List;

public class KeywordGraph {

    /**
     * Menganalisis keluhan pasien secara offline.
     * Menggunakan representasi Graf berupa Daftar Sisi (List of Edges) yang disimpan dalam ArrayList,
     * serta Larik (Array 1D) untuk akumulasi skor kategori agar selaras dengan materi kuliah.
     */
    public static String analisisOffline(String keluhan) {
        if (keluhan == null || keluhan.trim().isEmpty()) {
            return "Pemeriksaan Umum";
        }

        // 1. Pecah keluhan pasien menjadi kata-kata (token)
        String cleanKeluhan = keluhan.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleanKeluhan.split("\\s+");

        // 2. Muat semua kategori layanan (ArrayList)
        KategoriRepository katRepo = new KategoriRepository();
        List<KategoriLayanan> semuaKategori = katRepo.getSemuaKategori();

        // 3. Buat Larik (Array biasa) untuk menyimpan akumulasi skor masing-masing kategori
        int[] skor = new int[semuaKategori.size()];

        // 4. Muat semua relasi sisi graf (edges) dari file XML
        GraphRepository graphRepo = new GraphRepository();
        List<GraphEdge> edges = graphRepo.getSemuaEdge();

        // 5. Akumulasikan bobot untuk setiap kategori yang cocok
        for (String kata : tokens) {
            kata = kata.trim();
            if (kata.isEmpty()) continue;

            // Cari apakah ada sisi graf (edge) yang asalnya (source) cocok dengan kata keluhan
            for (GraphEdge edge : edges) {
                if (edge.getSource().toLowerCase().trim().equals(kata)) {
                    String kategoriTarget = edge.getTarget();
                    int bobot = edge.getWeight();
                    
                    // Cari indeks kategori target tersebut di dalam daftar semuaKategori
                    for (int i = 0; i < semuaKategori.size(); i++) {
                        if (semuaKategori.get(i).getNama().equalsIgnoreCase(kategoriTarget)) {
                            skor[i] += bobot; // Tambahkan bobot ke larik skor
                            break;
                        }
                    }
                }
            }
        }

        // 6. Cari kategori dengan skor tertinggi menggunakan Linear Search pada Larik
        String kategoriTerbaik = "Pemeriksaan Umum";
        int skorTertinggi = 0;

        for (int i = 0; i < semuaKategori.size(); i++) {
            if (skor[i] > skorTertinggi) {
                skorTertinggi = skor[i];
                kategoriTerbaik = semuaKategori.get(i).getNama();
            }
        }

        return kategoriTerbaik;
    }
}

