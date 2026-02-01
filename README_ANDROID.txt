========================================
   APLIKASI ANDROID REMOTE TV BLUETOOTH
========================================

Aplikasi Android untuk mengontrol TV B860H via Bluetooth

========================================
FITUR:
========================================

✓ Koneksi Bluetooth ke TV B860H
✓ Tombol OK (Enter)
✓ Tombol Play/Pause
✓ Tombol OK + Play/Pause BERSAMAAN ⭐
✓ Tombol Volume Up (Testing)
✓ Tombol Volume Down
✓ Auto-scan device B860H

========================================
CARA BUILD & INSTALL:
========================================

REQUIREMENTS:
- Android Studio (download di: https://developer.android.com/studio)
- HP Android dengan Bluetooth
- TV B860H dengan Bluetooth

LANGKAH-LANGKAH:

1. Buka Android Studio

2. Buat Project Baru:
   - Select "New Project"
   - Pilih "Empty Activity"
   - Name: Remote TV
   - Package name: com.example.remotetv
   - Language: Kotlin
   - Minimum SDK: API 23 (Android 6.0)

3. Replace File yang Ada dengan File di Folder ini:
   
   A. MainActivity.kt
      → Buka: app/src/main/java/com/example/remotetv/MainActivity.kt
      → Copy semua isi dari file MainActivity.kt yang saya buat
      → Paste dan Save
   
   B. AndroidManifest.xml
      → Buka: app/src/main/AndroidManifest.xml
      → Copy semua isi dari file AndroidManifest.xml yang saya buat
      → Paste dan Save
   
   C. build.gradle (Module: app)
      → Buka: app/build.gradle
      → Copy semua isi dari file build.gradle yang saya buat
      → Paste dan Save
   
   D. settings.gradle
      → Buka: settings.gradle
      → Copy semua isi dari file settings.gradle yang saya buat
      → Paste dan Save
   
   E. gradle.properties
      → Buka: gradle.properties
      → Tambahkan baris dari file gradle.properties yang saya buat
      → Save

4. Sync Project:
   - Klik "Sync Now" di pojok kanan atas
   - Tunggu sampai Gradle sync selesai

5. Connect HP:
   - Enable USB Debugging di HP (Developer Options)
   - Connect HP ke PC via USB
   - Pastikan HP terdeteksi di Android Studio

6. Build & Install:
   - Klik tombol "Run" (ikon hijau ▶) di Android Studio
   - Atau tekan Shift + F10
   - Tunggu proses build dan install ke HP

========================================
CARA MENGGUNAKAN:
========================================

1. Pair TV B860H dengan HP:
   - Buka Settings > Bluetooth di HP
   - Scan dan cari "B860H" atau device TV Anda
   - Pair device

2. Buka Aplikasi Remote TV:
   - Status akan menampilkan: "Status: Tidak terkoneksi"

3. Scan Device:
   - Klik tombol "🔍 Scan Device"
   - Aplikasi akan mencari device B860H yang sudah di-pair
   - Jika ditemukan, akan muncul: "Device ditemukan: B860H V5.0"

4. Connect:
   - Klik tombol "Hubungkan ke B860H"
   - Tunggu sampai status berubah jadi: "✓ Terkoneksi ke B860H"

5. Gunakan Tombol:
   - **OK**: Mengirim sinyal Enter ke TV
   - **▶||**: Mengirim sinyal Play/Pause ke TV
   - **OK+▶||**: Mengirim KEDUANYA BERSAMAAN ke TV ⭐
   - **VOL +**: Volume Up (untuk testing koneksi)
   - **VOL -**: Volume Down

========================================
CATATAN PENTING:
========================================

✓ Pastikan Bluetooth di HP sudah aktif
✓ TV B860H harus sudah di-pair dengan HP sebelumnya
✓ Jika koneksi gagal, coba:
  - Restart Bluetooth di HP
  - Unpair dan pair ulang B860H
  - Pastikan TV dalam mode pairing
✓ Aplikasi menggunakan SPP (Serial Port Profile) untuk komunikasi
✓ Perintah dikirim sebagai byte array via Bluetooth RFCOMM

========================================
PERMISSIONS:
========================================

Aplikasi memerlukan permissions:
- BLUETOOTH
- BLUETOOTH_ADMIN
- BLUETOOTH_CONNECT (Android 12+)
- BLUETOOTH_SCAN (Android 12+)
- ACCESS_FINE_LOCATION (untuk scan Bluetooth)

Semua permissions otomatis diminta saat pertama kali aplikasi dibuka.

========================================
TROUBLESHOOTING:
========================================

Problem: Device tidak ditemukan
Solusi: Pastikan B860H sudah di-pair di Bluetooth settings HP

Problem: Gagal connect
Solusi: 
- Restart Bluetooth
- Matikan dan nyalakan TV
- Coba restart HP

Problem: Tombol tidak berfungsi
Solusi: Pastikan TV mendukung kontrol via Bluetooth HID/SPP

========================================
INFO TAMBAHAN:
========================================

Aplikasi ini menggunakan:
- Kotlin
- Android SDK minimum: API 23 (Android 6.0)
- Target SDK: API 34 (Android 14)
- Bluetooth RFCOMM untuk komunikasi
- Serial Port Profile (UUID: 00001101-0000-1000-8000-00805F9B34FB)

========================================
