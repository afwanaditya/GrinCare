package com.grincare.service;

import com.grincare.model.Antrian;
import com.grincare.repository.AntrianRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;

public class AntrianQueueService {

    private static final String TICKET_ID_FORMAT  = "TKT-%s-%03d";
    private static final String TICKET_PREFIX     = "TKT-";
    private static final String NO_ANTRIAN_FORMAT = "A-%02d";

    private final AntrianRepository repo;
    private final WhatsAppService   waService;
    private final Queue<Antrian>    antrianQueue;

    public AntrianQueueService() {
        this.repo         = new AntrianRepository();
        this.waService    = new WhatsAppService();
        this.antrianQueue = new LinkedList<>();
        antrianQueue.addAll(repo.getAntrianAktif());
    }

    private int hitungAntrianHariIni() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return (int) repo.getSemuaAntrian().stream()
                .filter(a -> a.getTicketId().startsWith(TICKET_PREFIX + today))
                .count();
    }

    private String generateTicketId(int nomor) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format(TICKET_ID_FORMAT, today, nomor);
    }

    private String generateNoAntrian(int nomor) {
        return String.format(NO_ANTRIAN_FORMAT, nomor);
    }

    public String generateTicketId() {
        return generateTicketId(hitungAntrianHariIni() + 1);
    }

    public String generateNoAntrian() {
        return generateNoAntrian(hitungAntrianHariIni() + 1);
    }

    public int getNomorBerikutnya() {
        return hitungAntrianHariIni() + 1;
    }

    public int getJumlahAntrianAktif() {
        return repo.getAntrianAktif().size();
    }

    public Antrian ambilAntrianBaru(String nama, String noWhatsApp, String kategoriLayanan) {
        int next = hitungAntrianHariIni() + 1;

        Antrian antrian = new Antrian();
        antrian.setTicketId(generateTicketId(next));
        antrian.setNoAntrian(generateNoAntrian(next));
        antrian.setNama(nama.trim());
        antrian.setNoWhatsApp(noWhatsApp != null ? noWhatsApp.trim() : "");
        antrian.setKategoriLayanan(kategoriLayanan);
        antrian.setStatus(Antrian.STATUS_MENUNGGU);
        antrian.setWaktuDibuat(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        antrian.setStatusKirimWA(Antrian.WA_TIDAK_ADA);

        repo.tambahAntrian(antrian);

        String noWA = antrian.getNoWhatsApp();
        if (noWA != null && !noWA.isEmpty()) {
            boolean terkirim = waService.kirimTiketAntrian(
                noWA, antrian.getTicketId(), antrian.getNoAntrian(), antrian.getKategoriLayanan());
            String statusWA = terkirim ? Antrian.WA_TERKIRIM : Antrian.WA_GAGAL;
            antrian.setStatusKirimWA(statusWA);
            repo.updateStatusKirimWA(antrian.getTicketId(), statusWA);
        }

        antrianQueue.offer(antrian);
        return antrian;
    }

    public Queue<Antrian> getAntrianQueue() {
        return antrianQueue;
    }
}
