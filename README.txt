========================================
   APLIKASI REMOTE TV BLUETOOTH
========================================

APLIKASI DESKTOP DENGAN BLUETOOTH AVRCP:

Versi 1: remote_tv.py (Keyboard lokal)
Versi 2: remote_tv_bluetooth.py (Bluetooth AVRCP) ⭐ GUNAKAN INI!

========================================
APLIKASI VERSI BLUETOOTH (4 TOMBOL):
========================================

1. Tombol OK (hijau) - untuk Enter/OK
2. Tombol Play/Pause (biru) - untuk Play/Pause (▶||)
3. Tombol VOL + (oranye) - untuk Volume Up
4. Tombol VOL - (ungu) - untuk Volume Down

========================================
CARA MENGGUNAKAN (VERSI BLUETOOTH):
========================================

1. JALANKAN APLIKASI BLUETOOTH:
   python remote_tv_bluetooth.py

2. PANDUAN PENGGUNAAN:
   - Aplikasi akan otomatis scan device TV
   - Tunggu sampai device B860H terdeteksi
   - Klik tombol "AKTIFKAN" untuk menghubungkan ke TV
   - Gunakan tombol untuk mengontrol TV via Bluetooth
   - Sinyal dikirim langsung ke TV, bukan ke PC!

========================================
PERBEDAAN VERSI:
========================================

remote_tv.py:
- Mengontrol keyboard PC lokal saja
- Sinyal tidak dikirim ke device Bluetooth
- Tidak cocok untuk mengontrol TV

remote_tv_bluetooth.py: ⭐ RECOMMENDED
- Mengirim sinyal langsung ke TV via Bluetooth AVRCP
- Menggunakan Windows Media Commands
- Benar-benar mengontrol device TV yang terhubung

========================================
DEVICE YANG TERDETEKSI:
========================================

B860H V5.0 nown (dengan AVRCP Transport)
- Device ini sudah terhubung via Bluetooth
- Mendukung Audio Video Remote Control Profile
- Dapat dikontrol dengan aplikasi ini

========================================
CATATAN:
========================================

- Pastikan TV sudah dipair dengan Windows Bluetooth
- Aplikasi menggunakan Windows Shell SendKeys untuk AVRCP
- Tidak perlu install dependensi tambahan
- Gunakan remote_tv_bluetooth.py untuk kontrol TV yang benar

========================================
