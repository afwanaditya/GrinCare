# GrinCare

Sistem Manajemen Operasional Klinik Gigi Berbasis AI

GrinCare merupakan aplikasi desktop berbasis JavaFX yang dirancang untuk membantu operasional klinik gigi melalui konsultasi awal berbasis kecerdasan buatan (AI), pengelolaan antrean pasien, pengiriman tiket digital melalui WhatsApp, serta dashboard administrasi untuk mendukung pelayanan klinik yang lebih efisien.

Aplikasi ini menerapkan arsitektur **Model-View-Controller (MVC)** dengan lapisan **Service** dan **Repository**. Seluruh data disimpan secara lokal menggunakan file XML, sedangkan layanan AI memanfaatkan **Google Gemini API** dan pengiriman tiket digital menggunakan **Fonnte WhatsApp API**.

---

## Daftar Isi

- [Fitur](#fitur)
- [Teknologi yang Digunakan](#teknologi-yang-digunakan)
- [Arsitektur Sistem](#arsitektur-sistem)
- [Struktur Data](#struktur-data)
- [Persyaratan Sistem](#persyaratan-sistem)
- [Cara Menjalankan Proyek](#cara-menjalankan-proyek)
- [Alur Sistem](#alur-sistem)
- [Struktur Proyek](#struktur-proyek)
- [Tim Pengembang](#tim-pengembang)
- [Lisensi](#lisensi)

---

## Fitur

### Kiosk Pasien

- Konsultasi awal menggunakan Google Gemini AI.
- Pemilihan kategori layanan melalui Kategori Cepat.
- Rekomendasi layanan cadangan apabila AI tidak tersedia.
- Pengambilan nomor antrean secara otomatis.
- Pengiriman tiket digital melalui WhatsApp.
- Informasi antrean secara real-time.

### Dashboard Admin

- Login administrator.
- Pengelolaan data antrean.
- Pemanggilan pasien.
- Penyelesaian antrean.
- Pengelolaan kategori layanan.
- Dashboard statistik harian.
- Pengelolaan keyword AI.

---

## Teknologi yang Digunakan

| Kategori | Teknologi |
|----------|-----------|
| Bahasa Pemrograman | Java |
| Framework UI | JavaFX |
| Desain Antarmuka | FXML |
| Artificial Intelligence | Google Gemini API |
| WhatsApp Gateway | Fonnte API |
| Penyimpanan Data | XML |

---

## Arsitektur Sistem

GrinCare menerapkan pola arsitektur **MVC (Model-View-Controller)** dengan pemisahan lapisan Service dan Repository.

```text
JavaFX (FXML)
      │
 Controller
      │
   Service
      │
 Repository
      │
 XML Storage
```

---

## Struktur Data

| Struktur Data | Fungsi |
|---------------|--------|
| Queue (LinkedList) | Mengelola antrean pasien (FIFO) |
| Tree | Navigasi Kategori Cepat |
| Graph | Rekomendasi layanan cadangan |
| ArrayList | Penyimpanan sementara hasil parsing XML |

---

## Persyaratan Sistem

Sebelum menjalankan aplikasi, pastikan telah tersedia:

- Java Development Kit (JDK)
- JavaFX SDK
- IDE yang mendukung JavaFX (NetBeans, IntelliJ IDEA, atau Visual Studio Code)
- Koneksi internet
- Google Gemini API Key
- Fonnte API Token

---

## Cara Menjalankan Proyek

### 1. Clone Repository

```bash
git clone https://github.com/afwanaditya/GrinCare.git
```

### 2. Buka Proyek

Buka folder proyek menggunakan IDE yang mendukung JavaFX.

### 3. Konfigurasi API

Masukkan:

- Google Gemini API Key
- Fonnte API Token

ke dalam file konfigurasi aplikasi.

### 4. Konfigurasi JavaFX

Pastikan JavaFX SDK telah ditambahkan pada project.

### 5. Jalankan Aplikasi

Jalankan kelas `Main` sebagai **Java Application**.

---

## Alur Sistem

```text
Pasien
   │
   ▼
Konsultasi dengan Ginny AI
   │
   ├── AI tersedia
   │       │
   │       ▼
   │ Rekomendasi layanan
   │
   └── AI tidak tersedia
           │
           ▼
  Rekomendasi berbasis Graph
           │
           ▼
     Input data pasien
           │
           ▼
   Pembuatan nomor antrean
           │
           ▼
 Pengiriman tiket WhatsApp
           │
           ▼
 Dashboard Admin
```

---

## Struktur Proyek

```text
src/
├── controller/
├── model/
├── repository/
├── service/
├── util/
└── resources/
```

---

## Tim Pengembang

Team BebasKataLala

- Afwan Aditya Saputra
- Ahmad Dani Maulana
- Muhammad Pramudya Aldiansyah
- Khalisa Zahra Yulismar

---

## Lisensi

Proyek ini dikembangkan untuk keperluan akademik pada mata kuliah **Software Design** Program Studi Informatika, Universitas Islam Indonesia.
