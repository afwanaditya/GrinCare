package com.grincare.service;

import com.grincare.util.ConfigHelper;
import com.grincare.util.HttpHelper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiService {

    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private static final int CONNECT_TIMEOUT = 10_000;
    private static final int READ_TIMEOUT    = 60_000;

    private static final String SYSTEM_PROMPT =
        "Kamu adalah Ginny, asisten AI klinik gigi GrinCare. Tugasmu HANYA memberikan edukasi " +
        "awal singkat tentang keluhan gigi pasien dan merekomendasikan kategori layanan yang tepat.\\n\\n" +
        "BATASAN KETAT:\\n" +
        "1. Hanya menjawab pertanyaan terkait kesehatan dan layanan gigi.\\n" +
        "2. TIDAK BOLEH memberikan diagnosis medis spesifik.\\n" +
        "3. TIDAK BOLEH meresepkan atau menyarankan obat tertentu.\\n" +
        "4. TIDAK BOLEH menentukan tingkat urgensi tindakan medis.\\n\\n" +
        "KATEGORI LAYANAN YANG TERSEDIA (pilih TEPAT SATU jika dalam domain):\\n" +
        "- \\\"Pemeriksaan Umum\\\": sakit gigi, gigi berlubang, gusi bengkak, gigi sensitif, keluhan belum jelas.\\n" +
        "- \\\"Scaling\\\": karang gigi, tartar, bau mulut karena karang.\\n" +
        "- \\\"Kontrol\\\": pasien behel/kawat gigi, follow-up pasca tindakan, kontrol rutin.\\n" +
        "- \\\"Konsultasi Estetika\\\": whitening, veneer, keluhan estetika gigi.\\n\\n" +
        "FORMAT WAJIB - balas HANYA dengan JSON, tanpa teks lain:\\n" +
        "Jika pertanyaan DALAM domain klinik gigi:\\n" +
        "{\\\"dalamDomain\\\": true, \\\"edukasi\\\": \\\"<penjelasan 2-3 kalimat>\\\", \\\"kategoriLayanan\\\": \\\"<nama kategori>\\\"}\\n\\n" +
        "Jika pertanyaan DI LUAR domain klinik gigi (politik, hiburan, resep masakan, topik umum, dll):\\n" +
        "{\\\"dalamDomain\\\": false, \\\"edukasi\\\": \\\"Maaf, saya hanya dapat membantu pertanyaan terkait layanan dan kesehatan gigi.\\\", \\\"kategoriLayanan\\\": null}";

    public static class GeminiResult {
        private final boolean dalamDomain;
        private final String  edukasi;
        private final String  kategoriLayanan;

        public GeminiResult(boolean dalamDomain, String edukasi, String kategoriLayanan) {
            this.dalamDomain     = dalamDomain;
            this.edukasi         = edukasi;
            this.kategoriLayanan = kategoriLayanan;
        }

        public boolean isDalamDomain()      { return dalamDomain; }
        public String  getEdukasi()         { return edukasi; }
        public String  getKategoriLayanan() { return kategoriLayanan; }
    }

    public GeminiResult analisisKeluhan(String keluhanPasien) {
        String apiKey = ConfigHelper.getGeminiApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("[GeminiService] API key tidak ditemukan di config.properties");
            return null;
        }

        try {
            String body     = buildRequestBody(keluhanPasien);
            String response = kirimRequest(apiKey, body);
            if (response == null || response.isEmpty()) return null;

            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("[GeminiService] Error: " + e.getMessage());
            return null;
        }
    }

    private String buildRequestBody(String keluhan) {
        return "{"
            + "\"systemInstruction\":{\"parts\":[{\"text\":\"" + SYSTEM_PROMPT + "\"}]},"
            + "\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + HttpHelper.escapeJson(keluhan) + "\"}]}]"
            + "}";
    }

    private String kirimRequest(String apiKey, String body) throws Exception {
        URL url = new URL(ENDPOINT + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoOutput(true);

        try {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String response = HttpHelper.bacaStream(is);

            if (status < 200 || status >= 300) {
                System.err.println("[GeminiService] HTTP " + status + ": " + response);
                return null;
            }
            return response;
        } finally {
            conn.disconnect();
        }
    }

    private GeminiResult parseResponse(String json) {
        String text = ekstrakTextField(json);
        if (text == null || text.isEmpty()) return null;

        if (text.contains("```")) {
            int start = text.indexOf('\n') + 1;
            int end   = text.lastIndexOf("```");
            if (end > start) text = text.substring(start, end).trim();
        }

        boolean dalamDomain = ekstrakBooleanJson(text, "dalamDomain");

        String edukasi = ekstrakFieldJson(text, "edukasi");
        if (edukasi == null) {
            System.err.println("[GeminiService] Gagal parse field edukasi: " + text);
            return null;
        }

        String kategori = null;
        if (dalamDomain) {
            kategori = ekstrakFieldJson(text, "kategoriLayanan");
            if (kategori == null) {
                System.err.println("[GeminiService] dalamDomain=true tapi kategoriLayanan tidak ada: " + text);
            }
        }

        return new GeminiResult(dalamDomain, edukasi, kategori);
    }

    private String ekstrakTextField(String json) {
        Pattern p = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return unescapeJson(m.group(1));
    }

    private String ekstrakFieldJson(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return unescapeJson(m.group(1));
    }

    private boolean ekstrakBooleanJson(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        return m.find() && "true".equals(m.group(1));
    }

    private String unescapeJson(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }
}
