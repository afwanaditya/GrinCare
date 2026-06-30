package com.grincare.service;

import com.grincare.util.ConfigHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeminiService {

    private static final String ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

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

    // Inner class hasil parsing response Gemini
    public static class GeminiResult {
        private final boolean dalamDomain;
        private final String  edukasi;
        private final String  kategoriLayanan; // null jika dalamDomain == false

        public GeminiResult(boolean dalamDomain, String edukasi, String kategoriLayanan) {
            this.dalamDomain      = dalamDomain;
            this.edukasi          = edukasi;
            this.kategoriLayanan  = kategoriLayanan;
        }

        public boolean isDalamDomain()      { return dalamDomain; }
        public String  getEdukasi()         { return edukasi; }
        public String  getKategoriLayanan() { return kategoriLayanan; }
    }

    /**
     * Kirim keluhan ke Gemini, return GeminiResult atau null jika terjadi error.
     */
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
            + "\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"" + escapeJson(keluhan) + "\"}]}]"
            + "}";
    }

    private String kirimRequest(String apiKey, String body) throws Exception {
        URL url = new URL(ENDPOINT + apiKey);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(60_000);
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes("UTF-8"));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
        String response = bacaStream(is);

        if (status < 200 || status >= 300) {
            System.err.println("[GeminiService] HTTP " + status + ": " + response);
            return null;
        }
        return response;
    }

    private GeminiResult parseResponse(String json) {
        // Ekstrak nilai field "text" dari response JSON Gemini (outer layer)
        String text = ekstrakTextField(json);
        if (text == null || text.isEmpty()) return null;

        // Bersihkan markdown code block jika ada (```json ... ```)
        if (text.contains("```")) {
            int start = text.indexOf('\n') + 1;
            int end   = text.lastIndexOf("```");
            if (end > start) text = text.substring(start, end).trim();
        }

        // Parse field dalamDomain (boolean)
        boolean dalamDomain = ekstrakBooleanJson(text, "dalamDomain");

        // Parse edukasi (selalu ada)
        String edukasi = ekstrakFieldJson(text, "edukasi");
        if (edukasi == null) {
            System.err.println("[GeminiService] Gagal parse field edukasi: " + text);
            return null;
        }

        // Parse kategoriLayanan hanya jika dalamDomain == true
        String kategori = null;
        if (dalamDomain) {
            kategori = ekstrakFieldJson(text, "kategoriLayanan");
            if (kategori == null) {
                System.err.println("[GeminiService] dalamDomain=true tapi kategoriLayanan tidak ada: " + text);
            }
        }

        return new GeminiResult(dalamDomain, edukasi, kategori);
    }

    // Ambil nilai field "text" dari JSON response Gemini (outer JSON)
    private String ekstrakTextField(String json) {
        Pattern p = Pattern.compile("\"text\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return unescapeJson(m.group(1));
    }

    // Ambil nilai string dari field JSON {"field":"value"}
    private String ekstrakFieldJson(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (!m.find()) return null;
        return unescapeJson(m.group(1));
    }

    // Ambil nilai boolean dari field JSON {"field": true/false}
    private boolean ekstrakBooleanJson(String json, String field) {
        Pattern p = Pattern.compile("\"" + field + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        return m.find() && "true".equals(m.group(1));
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String s) {
        return s.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
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
