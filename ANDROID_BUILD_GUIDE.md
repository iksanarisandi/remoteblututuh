# Panduan Lengkap Build APK Android & Setup GitHub Actions

Dokumen ini berisi panduan teknis mendalam untuk membangun aplikasi Android `RemoteTV` dan menyiapkan otomatisasi CI/CD menggunakan GitHub Actions. Panduan ini dirancang agar AI Agent atau developer dapat mengikuti langkah demi langkah tanpa kegagalan.

## 1. Analisis Spesifikasi Project

Berdasarkan konfigurasi saat ini:
- **Gradle Plugin**: 8.5.2
- **Gradle Wrapper**: 8.7
- **Kotlin**: 1.9.24
- **Compile SDK**: 34 (Android 14)
- **Min SDK**: 28 (Android 9)
- **Java Version**: 17 (Wajib menggunakan JDK 17 untuk build environment)

---

## 2. Konfigurasi Signing (PENTING)

Saat ini, konfigurasi build `release` di `app/build.gradle` masih menggunakan debug key (`signingConfigs.debug`). Untuk production build yang valid, kita perlu mengubahnya agar bisa membaca Keystore dari Environment Variables (untuk CI/CD) atau file properti lokal.

### Langkah 1: Generate Keystore
Jika belum memiliki Keystore, jalankan perintah berikut di terminal (root project):

```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias key0
```
*Simpan password yang Anda buat! Anda akan membutuhkannya nanti.*

### Langkah 2: Modifikasi `app/build.gradle`
Ubah bagian `android` di `app/build.gradle` agar mendukung dynamic signing configuration.

**Cari blok `buildTypes` dan ubah menjadi:**

```groovy
    signingConfigs {
        release {
            // Cek apakah variabel env ada (untuk GitHub Actions)
            if (System.getenv("KEYSTORE_PASSWORD") != null) {
                storeFile = file(System.getenv("KEYSTORE_PATH"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            } else {
                // Fallback ke debug jika tidak ada config release (untuk local build tanpa keystore setup)
                storeFile = debug.storeFile
                storePassword = debug.storePassword
                keyAlias = debug.keyAlias
                keyPassword = debug.keyPassword
            }
        }
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            // Gunakan config release yang baru dibuat di atas
            signingConfig signingConfigs.release
        }
        debug {
            minifyEnabled false
        }
    }
```

---

## 3. Setup GitHub Secrets (Untuk CI/CD)

Agar GitHub Actions bisa mem-build dan men-sign APK secara otomatis, Anda perlu menyimpan informasi sensitif di GitHub Secrets.

### Langkah 1: Encode Keystore ke Base64
GitHub tidak bisa menyimpan file binary secara langsung dengan mudah. Kita perlu mengubah file `.jks` menjadi teks Base64.

**Di Terminal (Git Bash / Linux / Mac):**
```bash
base64 -w 0 release-key.jks > release-key.jks.base64
# Atau di PowerShell:
# [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.jks")) | Out-File release-key.jks.base64
```
Salin isi file `release-key.jks.base64`.

### Langkah 2: Tambahkan ke GitHub
Masuk ke repository GitHub -> **Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**.

Tambahkan secret berikut:

| Nama Secret | Nilai |
|-------------|-------|
| `KEYSTORE_BASE64` | Isi teks Base64 dari file `.jks` |
| `KEYSTORE_PASSWORD` | Password keystore Anda |
| `KEY_ALIAS` | Alias key (contoh: `key0`) |
| `KEY_PASSWORD` | Password key Anda |

---

## 4. Konfigurasi GitHub Actions Workflow

Buat atau update file di `.github/workflows/build-apk.yml` dengan konten berikut. Script ini menangani decode keystore dan build yang aman.

```yaml
name: Build Signed Android APK

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    name: Build Release APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      # Decode Keystore dari Secrets
      - name: Decode Keystore
        if: env.KEYSTORE_BASE64 != ''
        env:
            KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: |
          echo $KEYSTORE_BASE64 | base64 --decode > app/release-key.jks

      # Build APK Release
      # Mengirimkan path keystore dan credentials sebagai Environment Variables
      - name: Build Release APK
        env:
          KEYSTORE_PATH: ../app/release-key.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease

      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: app-release-signed
          path: app/build/outputs/apk/release/*.apk
          retention-days: 30
```

---

## 5. Troubleshooting Umum

1.  **Error `JAVA_HOME`**:
    *   Pastikan menggunakan JDK 17. Project ini dikonfigurasi dengan `JavaVersion.VERSION_17`.
    *   Cek di `File > Project Structure > SDK Location` di Android Studio.

2.  **Error `Permission denied` pada gradlew**:
    *   Di Windows local tidak masalah, tapi di CI (Linux) perlu `chmod +x gradlew` (sudah ditangani di script workflow di atas).

3.  **Lint Error**:
    *   Jika build gagal karena Lint (analisis kode statis), tambahkan baris ini di dalam blok `android {}` di `app/build.gradle`:
        ```groovy
        lintOptions {
            checkReleaseBuilds false
            abortOnError false
        }
        ```

4.  **Dependencies Conflict**:
    *   Project menggunakan `androidx.appcompat:appcompat:1.6.1`. Jika menambahkan library baru, pastikan versinya kompatibel dengan SDK 34.

## 6. Cara Build Local (Manual)

Jika ingin membuild APK release di laptop sendiri tanpa GitHub Actions:

1.  Buka terminal di Android Studio.
2.  Jalankan: `./gradlew assembleRelease`
3.  Hasil APK ada di `app/build/outputs/apk/release/`.
    *   *Catatan: Jika belum setup keystore environment variable di laptop, APK ini akan menggunakan debug keys (sesuai fallback logic di atas).*
