package com.grincare.service;

import com.grincare.util.ConfigHelper;
import com.grincare.util.HttpHelper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WhatsAppService {

    private static final String ENDPOINT        = "https://api.fonnte.com/send";
    private static final int    CONNECT_TIMEOUT = 10_000;
    private static final int    READ_TIMEOUT    = 15_000;

    public String normalisasiNomor(String nomor) {
        if (nomor == null || nomor.trim().isEmpty()) return "";
        nomor = nomor.trim().replaceAll("[\\s\\-]", "");
        if (nomor.startsWith("+62")) {
            return nomor.substring(1);
        } else if (nomor.startsWith("62")) {
            return nomor;
        } else if (nomor.startsWith("0")) {
            return "62" + nomor.substring(1);
        } else {
            return "62" + nomor;
        }
    }

    public boolean kirimTiketAntrian(String noWhatsApp, String ticketId,
                                     String noAntrian, String kategoriLayanan) {
        if (noWhatsApp == null || noWhatsApp.trim().isEmpty()) return false;

        String pesan = "Nomor Antrian: " + noAntrian + "\n"
                     + "Layanan: "       + kategoriLayanan + "\n"
                     + "Estimasi Waktu Tunggu: 15-20 menit";
        return kirimPesan(noWhatsApp, pesan);
    }

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

        HttpURLConnection conn = null;
        try {
            String body = "{\"target\":\"" + nomorNormal + "\","
                        + "\"message\":\"" + HttpHelper.escapeJson(pesan) + "\"}";

            URL url = new URL(ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            int status = conn.getResponseCode();
            if (status >= 200 && status < 300) {
                InputStream is = conn.getInputStream();
                System.out.println("[WhatsAppService] Terkirim ke " + nomorNormal
                        + " | " + HttpHelper.bacaStream(is).trim());
                return true;
            } else {
                System.err.println("[WhatsAppService] HTTP " + status
                        + ": " + HttpHelper.bacaStream(conn.getErrorStream()));
                return false;
            }

        } catch (Exception e) {
            System.err.println("[WhatsAppService] Error: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
