package com.grincare.service;

import com.grincare.model.Antrian;
import com.grincare.repository.AntrianRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

public class AntrianQueueService {

    private final AntrianRepository repo;
    private final WhatsAppService   waService;
    private final Queue<Antrian>    antrianQueue;

    public AntrianQueueService() {
        this.repo       = new AntrianRepository();
        this.waService  = new WhatsAppService();
        this.antrianQueue = new LinkedList<>();
        antrianQueue.addAll(repo.getAntrianAktif());
    }

    // Hitung berapa tiket yang sudah dibuat hari ini (sebagai basis nomor urut)
    private int hitungAntrianHariIni() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return (int) repo.getSemuaAntrian().stream()
                .filter(a -> a.getTicketId().startsWith("TKT-" + today))
                .count();
    }

    private String generateTicketId(int nomor) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("TKT-%s-%03d", today, nomor);
    }

    private String generateNoAntrian(int nomor) {
        return String.format("A-%02d", nomor);
    }

    // Versi publik — dipakai jika perlu cek format dari luar tanpa menyimpan
    public String generateTicketId() {
        return generateTicketId(hitungAntrianHariIni() + 1);
    }

    public String generateNoAntrian() {
        return generateNoAntrian(hitungAntrianHariIni() + 1);
    }

    /**
     * Ambil nomor antrian baru: generate ID, simpan ke XML, masukkan ke queue.
     */
    public Antrian ambilAntrianBaru(String nama, String noWhatsApp, String kategoriLayanan) {
        int next = hitungAntrianHariIni() + 1;

        Antrian antrian = new Antrian();
        antrian.setTicketId(generateTicketId(next));
        antrian.setNoAntrian(generateNoAntrian(next));
        antrian.setNama(nama.trim());
        antrian.setNoWhatsApp(noWhatsApp != null ? noWhatsApp.trim() : "");
        antrian.setKategoriLayanan(kategoriLayanan);
        antrian.setStatus("MENUNGGU");
        antrian.setWaktuDibuat(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        antrian.setStatusKirimWA("TIDAK_ADA");

        repo.tambahAntrian(antrian);

        // Kirim tiket via WhatsApp jika nomor diisi
        String noWA = antrian.getNoWhatsApp();
        if (noWA != null && !noWA.isEmpty()) {
            boolean terkirim = waService.kirimTiketAntrian(
                noWA, antrian.getTicketId(), antrian.getNoAntrian(), antrian.getKategoriLayanan());
            String statusWA = terkirim ? "TERKIRIM" : "GAGAL";
            antrian.setStatusKirimWA(statusWA);
            repo.updateStatusKirimWA(antrian.getTicketId(), statusWA);
        }

        antrianQueue.offer(antrian);
        return antrian;
    }

    // Dipakai modul Kelola Antrian nanti
    public Queue<Antrian> getAntrianQueue() {
        return antrianQueue;
    }
}
