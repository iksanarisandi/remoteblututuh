# Remote TV Bluetooth

Aplikasi Android untuk mengontrol TV B860H via Bluetooth

## Fitur

- ✅ Koneksi Bluetooth ke TV B860H
- ✅ Tombol OK (Enter)
- ✅ Tombol Play/Pause
- ✅ Tombol OK + Play/Pause BERSAMAAN ⭐
- ✅ Tombol Volume Up/Down (Testing)
- ✅ Auto-scan device B860H

## Download APK

APK otomatis di-build setiap kali ada update. Download dari:
[GitHub Actions](../../actions)

## Cara Install

1. Download APK dari GitHub Actions (Artifacts)
2. Transfer ke HP Android
3. Install APK (enable "Install from unknown sources")
4. Pair TV B860H dengan HP
5. Buka aplikasi dan gunakan!

## Build dari Source

Jika ingin build sendiri:

```bash
# Clone repository
git clone https://github.com/iksanarisandi/remoteblututuh.git
cd remoteblututuh

# Build dengan Gradle
./gradlew assembleDebug

# APK akan ada di: app/build/outputs/apk/debug/app-debug.apk
```

## Permissions

- BLUETOOTH
- BLUETOOTH_ADMIN
- BLUETOOTH_CONNECT (Android 12+)
- BLUETOOTH_SCAN (Android 12+)
- ACCESS_FINE_LOCATION

## Catatan

- Pastikan TV B860H sudah di-pair dengan HP
- Enable Bluetooth sebelum menggunakan aplikasi
- Gunakan tombol Volume Up untuk testing koneksi

## License

MIT License
