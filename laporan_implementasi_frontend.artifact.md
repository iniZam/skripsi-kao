# Laporan Implementasi Frontend Android - Penerjemah Bahasa Kao

Dokumen ini merangkum seluruh fitur dan perubahan teknis yang telah diimplementasikan pada aplikasi Android "Penerjemah Bahasa Kao" berdasarkan referensi desain HTML yang diberikan.

## 1. Fitur Utama yang Diimplementasikan

### 🔄 Penerjemahan Dua Arah (Swap)
Aplikasi mendukung penerjemahan dari **Bahasa Indonesia ke Bahasa Daerah** dan sebaliknya (**Bahasa Daerah ke Bahasa Indonesia**). Terdapat tombol interaktif `⇄` untuk menukar arah bahasa secara instan.

### 🔡 Pilihan Bahasa Daerah (Dropdown)
Tersedia menu pilihan bahasa yang dinamis menggunakan *Material 3 Exposed Dropdown Menu*:
- Bahasa Pagu
- Bahasa Modole
- Bahasa Towiliko

### 🎤 Pengenalan Suara (Speech-to-Text)
Integrasi dengan library `SpeechRecognizer` Android yang memungkinkan pengguna untuk memasukkan kalimat melalui suara (saat ini dioptimalkan untuk Bahasa Indonesia).

### 🔊 Pembacaan Teks (Text-to-Speech)
Terdapat tombol speaker `🔊` yang akan membacakan hasil terjemahan menggunakan mesin suara Android (`TextToSpeech`).

### 🌐 Koneksi Backend Real-time
Integrasi dengan API Python (FastAPI/Uvicorn) menggunakan library **Retrofit**. Aplikasi dikonfigurasi untuk terhubung ke server lokal melalui IP emulator `10.0.2.2`.

---

## 2. Detail Teknis (Tech Stack)

| Komponen | Teknologi |
| :--- | :--- |
| **Bahasa Pemrograman** | Kotlin |
| **UI Framework** | Jetpack Compose (Modern Declarative UI) |
| **Networking** | Retrofit 2 & OkHttp 3 |
| **Arsitektur** | MVVM (Model-View-ViewModel) |
| **Data Parsing** | GSON Converter |

---

## 3. Struktur File Penting

1.  **[MainActivity.kt](file:///D:/skripsi%20anjeli/frontend/app/src/main/java/com/example/frontend_indo_kao/MainActivity.kt)**: Mengatur tampilan utama, menangani izin (permission) mikrofon, dan interaksi UI.
2.  **[TranslationViewModel.kt](file:///D:/skripsi%20anjeli/frontend/app/src/main/java/com/example/frontend_indo_kao/ui/TranslationViewModel.kt)**: Mengelola logika bisnis, status aplikasi (isTranslating, isListening), serta integrasi Speech-to-Text & Text-to-Speech.
3.  **[TranslationApi.kt](file:///D:/skripsi%20anjeli/frontend/app/src/main/java/com/example/frontend_indo_kao/network/TranslationApi.kt)**: Definisi endpoint API dan konfigurasi jaringan (timeout 20 detik, logging, dan retry logic).
4.  **[network_security_config.xml](file:///D:/skripsi%20anjeli/frontend/app/src/main/res/xml/network_security_config.xml)**: Konfigurasi keamanan untuk mengizinkan trafik HTTP lokal tanpa SSL.

---

## 4. Cara Menjalankan Aplikasi & Koneksi

Agar aplikasi Android dapat berkomunikasi dengan backend di Windows, pastikan:

1.  **Sisi Server**: Jalankan backend dengan host `0.0.0.0` agar bisa menerima koneksi dari emulator:
    ```bash
    uvicorn server:app --host 0.0.0.0 --port 8000 --reload
    ```
2.  **Firewall**: Pastikan Windows Firewall mengizinkan koneksi masuk pada port `8000`.
3.  **Endpoint**: Backend harus menyediakan endpoint `POST /api/translate` yang menerima JSON:
    ```json
    {
      "teks": "kalimat asli",
      "bahasa_tujuan": "pagu/modole/towiliko/indonesia"
    }
    ```

---
**Status Proyek:** Selesai (Frontend Siap Digunakan)
