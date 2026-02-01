========================================
   REMOTE TV BLUETOOTH - DIRECT
========================================

APLIKASI UNTUK MENGIRIM SINYAL LANGSUNG KE DEVICE BLUETOOTH YANG SUDAH TERKONEKSI

========================================
TOMBOL YANG TERSEDIA:
========================================

1. Tombol OK (hijau) - Enter/OK
2. Tombol Play/Pause (biru ▶||) - Play/Pause
3. Tombol OK + ▶|| (oranye) - KEDUANYA BERSAMAAN ⭐

========================================
CARA MENGGUNAKAN:
========================================

1. PASTIKAN DEVICE TV SUDAH TERHUBUNG:
   - Buka Settings > Bluetooth & devices
   - Pastikan B860H V5.0 nown sudah terhubung (Paired & Connected)
   - Device harus dalam status "Connected"

2. JALANKAN APLIKASI:
   python remote_tv_direct.py

3. PANDUAN PENGGUNAAN:
   - Klik tombol "AKTIFKAN" (tombol oranye di bawah)
   - Status akan berubah menjadi AKTIF (hijau)
   - Klik tombol OK untuk mengirim Enter ke TV
   - Klik tombol ▶|| untuk mengirim Play/Pause ke TV
   - Klik tombol OK + ▶|| untuk mengirim KEDUANYA BERSAMAAN ⭐
   - Keyboard shortcuts: O=OK, P=Play, B=Keduanya

========================================
CATATAN PENTING:
========================================

✓ Device Bluetooth TV harus dalam status "Connected" sebelum menjalankan aplikasi
✓ Windows akan secara otomatis mengirim sinyal keyboard ke device Bluetooth yang aktif
✓ Tombol OK + Play/Pause akan menekan kedua tombol secara bersamaan
✓ Tidak perlu install dependensi tambahan (keyboard sudah ada)

========================================
PERBEDAAN DENGAN VERSI LAMA:
========================================

remote_tv.py:
- Hanya mengontrol keyboard PC lokal
- Tidak mengirim sinyal ke device Bluetooth

remote_tv_bluetooth.py:
- Mencoba scan device (namun Windows tidak support AVRCP commands)

remote_tv_direct.py: ⭐ GUNAKAN INI!
- Mengirim sinyal langsung ke device Bluetooth yang sudah terkoneksi
- Windows akan forward sinyal ke device aktif (B860H)
- Tombol OK + Play/Pause bersamaan sudah tersedia

========================================
TESTING:
========================================

1. Pastikan B860H sudah connected via Bluetooth
2. Jalankan: python remote_tv_direct.py
3. Klik AKTIFKAN
4. Klik tombol "OK + ▶||" (oranye)
5. Jika TV merespons, berarti sinyal terkirim dengan benar!

========================================
