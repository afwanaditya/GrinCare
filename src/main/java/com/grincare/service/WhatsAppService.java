package com.grincare.service;

import com.grincare.util.ConfigHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WhatsAppService {

    private static final String ENDPOINT = "https://api.fonnte.com/send";

    /**
     * Normalisasi nomor ke format 628xx.
     * Handles: 08xx → 628xx, +628xx → 628xx, 628xx → tetap, 8xx → 628xx
     */
    public String normalisasiNomor(String nomor) {
        if (nomor == null || nomor.trim().isEmpty()) return "";
        nomor = nomor.trim().replaceAll("[\\s\\-]", "");
        if (nomor.startsWith("+62")) {
            return nomor.substring(1);          // +628xx → 628xx
        } else if (nomor.startsWith("62")) {
            return nomor;                       // sudah 628xx
        } else if (nomor.startsWith("0")) {
            return "62" + nomor.substring(1);   // 08xx → 628xx
        } else {
            return "62" + nomor;                // 8xx → 628xx
        }
    }

    /**
     * Kirim tiket antrian ke WhatsApp pasien.
     * Return false langsung jika noWhatsApp kosong (WA opsional).
     */
    public boolean kirimTiketAntrian(String noWhatsApp, String ticketId,
                                     String noAntrian, String kategoriLayanan) {
        if (noWhatsApp == null || noWhatsApp.trim().isEmpty()) return false;

        String pesan = "Nomor Antrian: " + noAntrian + "\n"
                     + "Layanan: "       + kategoriLayanan + "\n"
                     + "Estimasi Waktu Tunggu: 15-20 menit";
        return kirimPesan(noWhatsApp, pesan);
    }

    /**
     * Kirim notifikasi panggil — dipakai modul Kelola Antrian (Lala) saat admin klik "Panggil".
     */
    public boolean kirimNotifikasiPanggil(String noWhatsApp, String noAntrian) {
        if (noWhatsApp == null || noWhatsApp.trim().isEmpty()) return false;

        String pesan = "Nomor antrian Anda " + noAntrian
                     + " sedang dipanggil, silakan menuju ke ruang pemeriksaan.";
        return kirimPesan(noWhatsApp, pesan);
    }

    private boolean kirimPesan(String noWhatsApp, String pesan) {
        String token = ConfigHelper.getFonnteToken();
        if (token == null || token.isEmpty()) {
            System.err.println("[WhatsAppService] Fonnte token tidak ditemukan di config.properties");
            return false;
        }

        String nomorNormal = normalisasiNomor(noWhatsApp);
        if (nomorNormal.isEmpty()) return false;

        try {
            String body = "{\"target\":\"" + nomorNormal + "\","
                        + "\"message\":\"" + escapeJson(pesan) + "\"}";

            URL url = new URL(ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                System.out.println("[WhatsAppService] Terkirim ke " + nomorNormal
                        + " | " + bacaStream(conn.getInputStream()).trim());
                return true;
            } else {
                System.err.println("[WhatsAppService] HTTP " + status
                        + ": " + bacaStream(conn.getErrorStream()));
                return false;
            }

        } catch (Exception e) {
            System.err.println("[WhatsAppService] Error: " + e.getMessage());
            return false;
        }
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String bacaStream(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
