# 🎯 CARA DOWNLOAD APK

APK sedang di-build otomatis oleh GitHub Actions! Berikut cara mendapatkannya:

## 📥 Langkah-langkah Download:

### 1. Buka GitHub Actions
1. Kunjungi: https://github.com/iksanarisandi/remoteblututuh/actions
2. Tunggu sampai build selesai (status hijau ✓ dengan centang)
3. Biasanya butuh 3-5 menit

### 2. Download APK
Setelah build selesai:

1. Scroll ke bawah di halaman Actions
2. Cari bagian **"Artifacts"**
3. Anda akan melihat 2 file:
   - **app-debug** (APK Debug - lebih kecil, bisa langsung install)
   - **app-release** (APK Release - lebih besar, optimized)

4. Klik pada salah satu (pilih **app-debug** untuk pemakaian biasa)
5. Klik tombol **Download**

### 3. Install ke HP

#### Opsi A: Transfer via USB
1. Connect HP ke PC dengan USB
2. Copy file APK ke HP
3. Buka file APK di HP
4. Klik "Install" (mungkin perlu enable "Unknown sources")

#### Opsi B: Download langsung di HP
1. Buka browser di HP
2. Kunjungi: https://github.com/iksanarisandi/remoteblututuh/actions
3. Download APK langsung dari sana
4. Install

### 4. Pair dengan TV
1. Buka **Settings > Bluetooth** di HP
2. Scan device
3. Cari **B860H** atau device TV Anda
4. Pair device

### 5. Gunakan Aplikasi
1. Buka aplikasi **Remote TV**
2. Klik **🔍 Scan Device**
3. Klik **Hubungkan ke B860H**
4. Gunakan tombol:
   - **OK** = Enter
   - **▶||** = Play/Pause
   - **OK + ▶||** = Keduanya bersamaan ⭐
   - **VOL +** = Volume Up (test)
   - **VOL -** = Volume Down

## 🔧 Troubleshooting

### Build belum selesai?
- Tunggu beberapa menit
- Refresh halaman Actions
- Cek status di tab "Summary"

### APK tidak bisa install?
- Pastikan "Install from unknown sources" aktif
- Coba APK debug dulu (app-debug)
- Pastikan Android versi 6.0 ke atas

### Device tidak ketemu?
- Pastikan B860H sudah di-pair di Bluetooth settings
- Restart Bluetooth di HP
- Coba unpair dan pair ulang

### Tombol tidak berfungsi?
- Pastikan status sudah "Terkoneksi"
- Coba tombol VOL + dulu untuk testing
- Restart HP dan TV

## 📱 Info Aplikasi

- **Nama**: Remote TV
- **Package**: com.example.remotetv
- **Min SDK**: Android 6.0 (API 23)
- **Target SDK**: Android 14 (API 34)
- **Ukuran**: ~2-3 MB

## 🔐 Permissions

Aplikasi membutuhkan:
- ✓ Bluetooth
- ✓ Bluetooth Admin
- ✓ Bluetooth Connect (Android 12+)
- ✓ Bluetooth Scan (Android 12+)
- ✓ Location (untuk scan Bluetooth)

Semua permissions aman dan hanya digunakan untuk koneksi ke TV.

## 📞 Support

Jika ada masalah:
1. Cek Issues di GitHub: https://github.com/iksanarisandi/remoteblututuh/issues
2. Atau buat Issue baru untuk melaporkan masalah

---

**Selamat menggunakan! 🎉**
