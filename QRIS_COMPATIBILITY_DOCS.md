# 📋 Panduan Kompatibilitas & Integrasi QRIS Listener

Aplikasi ini dirancang sebagai jembatan otomatisasi untuk menangani pembayaran QRIS secara real-time. Dokumen ini dibuat untuk membantu Anda memahami cara kerja aplikasi, menentukan kompatibilitas dengan layanan QRIS Anda, serta memberikan panduan integrasi ke sistem backend Anda.

---

## 🚀 1. Cara Kerja Utama Aplikasi

Sistem ini **tidak terhubung langsung ke API internal Bank atau API E-Wallet** (sehingga tidak membutuhkan kredensial sensitif atau izin developer merchant khusus). 

Cara kerja aplikasi adalah **Notification-Based Gateway**:
1. **Menerima Pembayaran**: Pelanggan memindai QRIS Anda dan menyelesaikan pembayaran.
2. **Notifikasi Masuk**: Aplikasi merchant Anda (seperti GoBiz, Shopee Partner, DANA Bisnis, OVO Merchant, Livin' Merchant, BCA Mobile, dll.) menampilkan push notifikasi di perangkat Android Anda (contoh: *"Anda menerima pembayaran Rp15.000 dari BUDI UTOMO"*).
3. **Intersepsi Notifikasi**: Layanan latar belakang aplikasi ini (`NotificationListenerService`) mendeteksi notifikasi tersebut secara real-time.
4. **Parsing Data Pintar**: Pembaca teks pintar (`PaymentParser`) otomatis memproses teks notifikasi untuk mencari:
   - **Nominal Uang** (contoh: `15000`)
   - **Nama Pengirim** (contoh: `BUDI UTOMO`)
   - **Aplikasi Asal / Merchant** (contoh: `GoPay` atau `ShopeePay`)
5. **Kirim Webhook**: Aplikasi menyusun data tersebut ke dalam format JSON/Query String dan mengirimkannya ke server backend Anda (Webhook URL) secara otomatis.
6. **TTS (Text-to-Speech)**: Aplikasi juga dapat membacakan nama pengirim dan nominalnya secara langsung lewat speaker (misal: *"Pembayaran lima belas ribu rupiah dari Budi Utomo berhasil"*).

---

## 🔍 2. Apakah Layanan QRIS Anda Kompatibel? (Checklist Kompatibilitas)

Aplikasi ini **100% Kompatibel** dengan layanan QRIS apa pun, asalkan memenuhi persyaratan sederhana berikut:

### 👍 SYARAT UTAMA:
- [ ] **Menerima Push Notifikasi di HP:** Aplikasi merchant QRIS Anda dapat menampilkan notifikasi push di layar HP Android ketika ada uang masuk.
- [ ] **Mencantumkan Nominal:** Isi teks notifikasi tersebut harus mencantumkan nominal uang yang diterima (misal: "Pembayaran sebesar Rp15.000 sukses"). If the notification only says "Ada transaksi baru" without the amount, the parser cannot extract the details unless configured via specific formats.

### 📱 Daftar Aplikasi yang Sudah Terbiasa Digunakan:
* **Gojek / GoBiz / GoPay Merchant**
* **Shopee Partner / ShopeePay**
* **DANA Bisnis / Kelola DANA**
* **OVO Merchant**
* **GrabMerchant**
* **BCA Mobile / Merchant BCA**
* **Livin' Merchant / Bank Mandiri**
* **QRIS.id / Netzme / PosPay / LinkAja**
* **Aplikasi Bank daerah lainnya** yang memunculkan notifikasi mutasi/dana masuk.

---

## ⚙️ 3. Pola Pembacaan Data (Parsing Patterns)

Mesin pembaca (`PaymentParser.kt`) dilatih untuk mengenali format penulisan umum di Indonesia:

### A. Deteksi Nominal Uang
Aplikasi secara otomatis mendeteksi pola angka rupiah seperti:
* `Rp 15.000` atau `Rp.150.000`
* `IDR 50,000` atau `IDR 2.500.00`
* Angka murni berakhiran mata uang: `25000 rupiah` atau `15000 rp`
* Angka mandiri bernilai di atas 1000 tanpa simbol rupiah jika berada di dalam teks transaksi.

### B. Deteksi Nama Pengirim
Aplikasi mendeteksi nama pengirim asli dengan mencari kata kunci penghubung seperti:
* `... dari [NAMA PENGIRIM]`
* `... oleh [NAMA PENGIRIM]`
* `... pengirim: [NAMA PENGIRIM]`
* `... transfer masuk dari [NAMA PENGIRIM]`
* *Sistem secara cerdas menyaring kata noise (seperti "rekening", "bank", "gopay", "saldo") sehingga hanya menghasilkan nama pengirim sesungguhnya.*

---

## 💻 4. Format Pengiriman Webhook (Format Data ke Server Anda)

Secara default, jika Anda menetapkan **Webhook Target** di aplikasi, server Anda akan mendapatkan data bertipe `POST` dengan format JSON seperti berikut:

### Contoh Payload JSON Sent to Your Server
```json
{
  "event": "qris_payment",
  "id": 142,
  "app_name": "GoPay",
  "package_name": "com.gojek.app",
  "title": "Pembayaran Berhasil",
  "text": "Anda menerima pembayaran sebesar Rp150.000 dari AHMAD BUDI",
  "big_text": "Menjumlahkan saldo QRIS sebesar Rp150.000 dari GoPay.",
  "amount": 150000,
  "sender": "AHMAD BUDI",
  "currency": "IDR",
  "timestamp": "2026-05-22T17:01:23+07:00",
  "device_id": "8fa19e34c9108b",
  "dedupe_key": "test-payload-key"
}
```

### Variabel Kustom yang Dapat Digunakan
Jika Anda ingin me-map format webhook Anda sendiri (Custom Template), Anda dapat menyusunnya langsung melalui aplikasi menggunakan variabel dinamis berikut:
* `{id}` : ID Log Notifikasi unik di lokal.
* `{app_name}` : Nama aplikasi e-wallet pengirim notifikasi (misal: GoPay, ShopeePay).
* `{package_name}` : Package name Android (misal: `com.gojek.app`).
* `{title}` : Judul notifikasi.
* `{text}` : Isi teks notifikasi.
* `{big_text}` : Isi teks panjang notifikasi jika ada.
* `{amount}` : Angka nominal saja (contoh: `15000`).
* `{sender}` : Nama pengirim yang terdeteksi (contoh: `AHMAD BUDI`).
* `{timestamp}` : Waktu terjadinya transaksi.
* `{device_id}` : ID unik ponsel Android pengirim webhook.
* `{dedupe_key}` : Kunci enkripsi unik untuk mencegah transaksi ganda di sisi backend.

---

## 🛠️ 5. Cara Pengujian untuk Memastikan Kompatibilitas

Anda bisa langsung mengetes kecocokan aplikasi ini di ponsel Anda tanpa perlu menunggu pelanggan membayar asli:

1. **Unduh & Pasang APK Debug**:
   * Instal aplikasi di HP android merchant Anda.
2. **Aktifkan Izin Akses Notifikasi (Notification Listener Permission)**:
   * Saat pertama kali masuk, klik tombol izin dan aktifkan akses untuk aplikasi ini.
3. **Konfigurasikan Webhook**:
   * Masuk ke tab **Webhook Config**.
   * Masukkan URL endpoint server Anda (atau gunakan tool gratis seperti `webhook.site` untuk uji coba) dan masukkan Secret token jika dibutuhkan.
4. **Gunakan Fitur "Kirim Ulang" di Detak Log**:
   * Di Dashboard atau Riwayat, pilih salah satu baris log pengujian, lalu klik **"Kirim Ulang"** untuk mengetes respons Webhook ke server tujuan Anda.
5. **Coba Transaksi Riil**:
   * Lakukan pembayaran QRIS nominal kecil (misalnya Rp1.000) ke QRIS Anda.
   * Pastikan HP Anda menerima notifikasi pembayaran.
   * Cek di aplikasi apakah nominal dan nama pengirim terdeteksi dengan benar dan langsung dikirim ke Webhook Anda.

---

## 🛡️ 6. Ketangguhan Pengiriman (Deduplikasi & Auto-Retry)

* **Anti Transaksi Ganda (Deduplication):** Aplikasi menggunakan algoritma MD5 hashing untuk mendinginkan notifikasi serupa dalam window 30 detik untuk mencegah pemicuan ganda (double-triggering) jika notifikasi muncul berulang di system drawer.
* **Auto-Retry & WorkManager:** Jika koneksi server Anda down atau offline saat webhook dikirim, aplikasi mengantrekan request tersebut ke latar belakang menggunakan Android `WorkManager` yang akan mencobanya lagi secara otomatis setelah kondisi internet pulih.
