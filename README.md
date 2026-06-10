# QRIS Notification Bridge

Android notification listener untuk menangkap notifikasi pembayaran, membaca nominal/sender, membacakan nominal lewat Text-to-Speech, dan meneruskan data ke webhook server pribadi.

## Fitur Utama

- Memantau notifikasi dari aplikasi pembayaran/e-wallet/bank yang dipilih.
- Filter package, keyword positif, dan keyword negatif.
- Routing profile untuk mengirim notifikasi tertentu ke webhook tertentu.
- Payload webhook simple/extended/custom tetap dikontrol dari konfigurasi target.
- Retry otomatis untuk webhook pending.
- Speaker TTS dengan template, volume, repeat, bahasa, kecepatan, pitch, dan mode per profile.
- Riwayat notifikasi dan status delivery lokal.

## Setup Lokal

1. Buka folder project ini di Android Studio.
2. Sync Gradle.
3. Jalankan ke device Android fisik untuk hasil notification listener paling akurat.
4. Di app, aktifkan izin akses notifikasi.
5. Tambahkan webhook target server.
6. Matikan battery optimization untuk pemantauan 24 jam.

## Catatan Operasional

- Format payload webhook tidak diubah oleh update reliability/TTS/UX.
- Data sensitif seperti secret webhook dan log pembayaran tidak diikutkan ke backup Android.
- Database memakai migration non-destruktif untuk menjaga data lokal saat app update.

## Testing

Jalankan unit test:

```powershell
.\gradlew.bat test
```

Test utama mencakup parser nominal/sender, kompatibilitas payload, dan policy TTS.
