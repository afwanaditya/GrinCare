package com.grincare.util;

import com.grincare.model.GraphEdge;
import com.grincare.repository.GraphRepository;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class KeywordGraph {

    /**
     * Menganalisis keluhan pasien secara offline.
     * Menggunakan representasi Graf berupa Edge List (Daftar Sisi) yang sangat sederhana.
     */
    public static String analisisOffline(String keluhan) {
        if (keluhan == null || keluhan.trim().isEmpty()) {
            return "Pemeriksaan Umum";
        }

        // 1. Pecah keluhan pasien menjadi kata-kata (token)
        String cleanKeluhan = keluhan.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleanKeluhan.split("\\s+");

        // 2. Muat semua relasi sisi graf (edges) dari file XML
        GraphRepository repo = new GraphRepository();
        List<GraphEdge> edges = repo.getSemuaEdge();

        // 3. Akumulasikan bobot untuk setiap kategori yang cocok
        Map<String, Integer> skorKategori = new HashMap<>();

        for (String kata : tokens) {
            kata = kata.trim();
            if (kata.isEmpty()) continue;

            // Cari apakah ada sisi graf (edge) yang asalnya (source) cocok dengan kata keluhan
            for (GraphEdge edge : edges) {
                if (edge.getSource().toLowerCase().trim().equals(kata)) {
                    String kategoriTarget = edge.getTarget();
                    int bobot = edge.getWeight();
                    
                    // Tambahkan bobot ke skor kategori target tersebut
                    skorKategori.put(kategoriTarget, skorKategori.getOrDefault(kategoriTarget, 0) + bobot);
                }
            }
        }

        // 4. Tentukan kategori dengan skor akumulasi bobot tertinggi
        String kategoriTerbaik = "Pemeriksaan Umum";
        int skorTertinggi = -1;

        for (Map.Entry<String, Integer> entry : skorKategori.entrySet()) {
            if (entry.getValue() > skorTertinggi) {
                skorTertinggi = entry.getValue();
                kategoriTerbaik = entry.getKey();
            }
        }

        return kategoriTerbaik;
    }
}
