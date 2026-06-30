package com.grincare.model;

public class Antrian {
    private String ticketId;
    private String noAntrian;
    private String nama;
    private String noWhatsApp;
    private String kategoriLayanan;
    private String status;       // MENUNGGU | DIPANGGIL | SELESAI
    private String waktuDibuat;
    private String statusKirimWA; // TERKIRIM | GAGAL | TIDAK_ADA

    public Antrian() {}

    public Antrian(String ticketId, String noAntrian, String nama, String noWhatsApp,
                   String kategoriLayanan, String status, String waktuDibuat, String statusKirimWA) {
        this.ticketId = ticketId;
        this.noAntrian = noAntrian;
        this.nama = nama;
        this.noWhatsApp = noWhatsApp;
        this.kategoriLayanan = kategoriLayanan;
        this.status = status;
        this.waktuDibuat = waktuDibuat;
        this.statusKirimWA = statusKirimWA;
    }

    public String getTicketId()      { return ticketId; }
    public void setTicketId(String v) { this.ticketId = v; }

    public String getNoAntrian()      { return noAntrian; }
    public void setNoAntrian(String v) { this.noAntrian = v; }

    public String getNama()      { return nama; }
    public void setNama(String v) { this.nama = v; }

    public String getNoWhatsApp()      { return noWhatsApp; }
    public void setNoWhatsApp(String v) { this.noWhatsApp = v; }

    public String getKategoriLayanan()      { return kategoriLayanan; }
    public void setKategoriLayanan(String v) { this.kategoriLayanan = v; }

    public String getStatus()      { return status; }
    public void setStatus(String v) { this.status = v; }

    public String getWaktuDibuat()      { return waktuDibuat; }
    public void setWaktuDibuat(String v) { this.waktuDibuat = v; }

    public String getStatusKirimWA()      { return statusKirimWA; }
    public void setStatusKirimWA(String v) { this.statusKirimWA = v; }
}
