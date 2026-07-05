package com.grincare.model;

public class RekapHarian {
    public static final String KATEGORI_PEMERIKSAAN_UMUM   = "Pemeriksaan Umum";
    public static final String KATEGORI_SCALING             = "Scaling";
    public static final String KATEGORI_KONTROL             = "Kontrol";
    public static final String KATEGORI_KONSULTASI_ESTETIKA = "Konsultasi Estetika";

    private String tanggal;
    private int jumlahPemeriksaanUmum;
    private int jumlahScaling;
    private int jumlahKontrol;
    private int jumlahKonsultasiEstetika;
    private int jumlahSelesai;
    private int jumlahMenunggu;

    public RekapHarian() {}
    public RekapHarian(String tanggal, int jumlahPemeriksaanUmum, int jumlahScaling,
                       int jumlahKontrol, int jumlahKonsultasiEstetika,
                       int jumlahSelesai, int jumlahMenunggu) {
        this.tanggal                  = tanggal;
        this.jumlahPemeriksaanUmum    = jumlahPemeriksaanUmum;
        this.jumlahScaling            = jumlahScaling;
        this.jumlahKontrol            = jumlahKontrol;
        this.jumlahKonsultasiEstetika = jumlahKonsultasiEstetika;
        this.jumlahSelesai            = jumlahSelesai;
        this.jumlahMenunggu           = jumlahMenunggu;
    }

    public String getTanggal()                          { return tanggal; }
    public void   setTanggal(String v)                  { this.tanggal = v; }

    public int  getJumlahPemeriksaanUmum()              { return jumlahPemeriksaanUmum; }
    public void setJumlahPemeriksaanUmum(int v)         { this.jumlahPemeriksaanUmum = v; }

    public int  getJumlahScaling()                      { return jumlahScaling; }
    public void setJumlahScaling(int v)                 { this.jumlahScaling = v; }

    public int  getJumlahKontrol()                      { return jumlahKontrol; }
    public void setJumlahKontrol(int v)                 { this.jumlahKontrol = v; }

    public int  getJumlahKonsultasiEstetika()           { return jumlahKonsultasiEstetika; }
    public void setJumlahKonsultasiEstetika(int v)      { this.jumlahKonsultasiEstetika = v; }

    public int  getJumlahSelesai()                      { return jumlahSelesai; }
    public void setJumlahSelesai(int v)                 { this.jumlahSelesai = v; }

    public int  getJumlahMenunggu()                     { return jumlahMenunggu; }
    public void setJumlahMenunggu(int v)                { this.jumlahMenunggu = v; }  
}
