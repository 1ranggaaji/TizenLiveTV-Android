# 📺 Samsung Tizen Live TV (Android TV & Google TV)

Aplikasi **Live TV Standalone & TV Launcher** modern untuk Android TV / Google TV yang dikembangkan dan dievolusikan dari arsitektur [googlesamples/androidtv-sample-inputs](https://github.com/googlesamples/androidtv-sample-inputs), dipadukan dengan desain antarmuka elegan, responsif, dan premium khas **Samsung Tizen OS (One UI TV / Smart Hub)**.

---

## 🌟 Mengapa Proyek Ini Dikembangkan?

Repositori asli Google (`androidtv-sample-inputs`) adalah contoh *TV Input Framework (TIF)* yang hanya berperan sebagai penyedia data di balik layar untuk aplikasi Google *Live Channels*. Sayangnya, Google telah menghentikan dan menyembunyikan aplikasi *Live Channels* pada sebagian besar perangkat Android TV 11+ dan Google TV.

**Tizen Live TV** mengatasi masalah tersebut dengan mengubahnya menjadi **Aplikasi TV Utama yang Lengkap**:
1. Berfungsi sebagai **Player Live TV Mandiri** yang langsung memutar siaran secara *fullscreen* saat aplikasi dibuka atau saat TV dihidupkan (*auto boot*).
2. Dapat dijadikan sebagai **Aplikasi Default / Home Launcher TV**, sehingga tombol *Home* atau tombol *TV* di remote langsung mengarah ke siaran televisi ini.
3. Dilengkapi antarmuka pengguna **Samsung Tizen OS**, lengkap dengan *Smart Hub Floating Bar*, *Now & Next Info Banner*, *Fullscreen EPG Guide Grid*, serta *Direct Numeric Remote OSD*.

---

## 💎 Fitur Antarmuka (Samsung Tizen OS UI)

| Komponen | Penjelasan & Pengalaman Tizen OS |
|---|---|
| **Samsung Smart Hub Floating Bar** | Panel kaca gelap (*frosted glass*) yang meluncur mulus dari bawah layar tanpa menghentikan pemutaran video saat menekan tombol `OK` / `Enter`. Berisi deretan kartu saluran horizontal dengan animasi *scale focus*, progres durasi, dan menu aksi cepat. |
| **Now & Next Info Banner** | Banner status mengambang yang muncul saat berpindah saluran (`CH+` / `CH-` atau D-Pad) maupun saat menekan tombol `INFO`. Menampilkan nomor saluran besar, logo, judul acara saat ini, *progress bar live*, pratinjau acara berikutnya, serta badge kualitas (`4K UHD`, `HDR10`, `Dolby 5.1`, `LIVE`). Auto-dismiss setelah 4,5 detik. |
| **Panduan TV Lengkap (EPG Grid)** | Tampilan jadwal program televisi komprehensif dengan tab kategori (*Semua, Berita, Olahraga, Film, Musik, Anak*), daftar saluran di sebelah kiri, dan timeline horizontal program acara beserta sinopsis dan tombol "Tonton Sekarang". |
| **Direct Numeric Remote OSD** | Indikator nomor saluran mengambang di pojok kanan atas saat menekan tombol angka `0-9` di remote TV (misal: `[ 0 0 2 ]` menuju Bloomberg News). |
| **Pengubah Rasio Aspek (Aspect Ratio)** | Dukungan siklus rasio instan: **Fit (16:9 Asli)**, **Fill / Stretch (Penuh Layar)**, dan **Zoom (Menghilangkan Garis Hitam)**. |
| **Impor Playlist IPTV (M3U / M3U8)** | Pengguna dapat menambahkan URL playlist IPTV siaran lokal Indonesia atau siaran internasional favorit ke dalam daftar saluran. |

---

## 🎮 Kontrol Remote TV

Aplikasi ini dioptimalkan 100% untuk remote kontrol televisi (D-Pad & IR/Bluetooth TV Remote):

| Tombol Remote TV | Aksi |
|---|---|
| **D-Pad Atas (`UP`)** | Pindah ke Saluran Berikutnya (Mode TV) / Navigasi Atas |
| **D-Pad Bawah (`DOWN`)** | Pindah ke Saluran Sebelumnya (Mode TV) / Navigasi Bawah |
| **D-Pad Tengah (`OK` / `ENTER`)** | Membuka **Samsung Smart Hub Floating Bar** / Pilih Saluran |
| **D-Pad Kiri (`LEFT`)** | Membuka **Panduan TV (EPG Grid)** |
| **D-Pad Kanan (`RIGHT`)** | Mengganti Rasio Layar secara cepat (*Fit / Fill / Zoom*) |
| **Channel Up / Down (`CH+` / `CH-`)** | Pindah Saluran secara berurutan + menampilkan Info Banner |
| **Guide / EPG (`GUIDE` / `RED`)** | Membuka / menutup Panduan Acara TV (EPG) |
| **Info (`INFO`)** | Memunculkan Info Banner *Now & Next* saluran aktif |
| **Tombol Angka (`0` - `9`)** | Mengetik nomor saluran langsung (otomatis pindah setelah 1.2 detik) |
| **Back (`KEMBALI`)** | Menutup menu overlay yang aktif / Dialog konfirmasi keluar |

---

## 🚀 Panduan Menjadikan Aplikasi Default di TV Anda

### Cara 1: Menjadikan Home Launcher Default (Melalui Pengaturan TV)
1. Buka menu **Settings (Pengaturan)** di Android TV / Google TV Anda.
2. Masuk ke **Apps (Aplikasi)** -> **Special app access (Akses aplikasi khusus)** -> **Default apps (Aplikasi default)**.
3. Pilih **Home app (Aplikasi beranda)**.
4. Pilih **Samsung Live TV**.
5. *Selesai!* Sekarang setiap kali tombol *Home* di remote ditekan, TV akan langsung membuka siaran Live TV ini.

### Cara 2: Menjadikan Default via Perintah ADB (Sangat Cepat)
Hubungkan laptop/PC Anda ke Android TV melalui ADB via Wi-Fi atau USB, lalu jalankan:
```bash
adb shell cmd package set-home-activity com.tizen.livetv/.ui.MainActivity
```

### Cara 3: Auto-Launch saat TV Menyala (Power On / Boot)
Aplikasi ini sudah dilengkapi dengan `BootReceiver` yang mendengarkan sinyal `android.intent.action.BOOT_COMPLETED` dan `QUICKBOOT_POWERON`. 
- Saat TV dinyalakan dari kondisi mati/standby, siaran televisi langsung diputar secara otomatis di layar penuh tanpa perlu menekan remote.
- Fitur ini dapat diaktifkan atau dinonaktifkan kapan saja melalui menu **Pengaturan** di dalam aplikasi.

---

## 🛠️ Kompatibilitas Sumber Siaran (Streaming Formats)

Mengadopsi mesin **Jetpack Media3 (ExoPlayer)** terbaru:
- **HLS (.m3u8)**: Siaran langsung TV interaktif, adaptive bitrate streaming, low-latency.
- **MPEG-DASH (.mpd)**: Format streaming adaptif standar industri (seperti pada sample Tears of Steel).
- **MP4 / Progressive**: Tayangan lokal & sample Google Cloud Storage.
- **TIF (TV Input Framework)**: Kompatibel dengan `TvContractCompat` Android TV jika sistem TV Anda menggunakan input provider standar.

---

## 📦 Cara Build dan Install ke Android TV

### Prasyarat:
- Android Studio Hedgehog / Iguana / Jellyfish (atau JDK 17).
- Android SDK Platform 34 (Android 14) atau minimum SDK 21 (Lollipop ke atas).

### Langkah Build APK:
1. Buka terminal di folder proyek ini:
   ```bash
   cd /Users/ranggaaji/Documents/TizenLiveTV
   ```
2. Jalankan perintah kompilasi Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
3. File APK hasil kompilasi akan berada di:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Langkah Pemasangan ke TV melalui ADB:
1. Aktifkan **Developer Options** dan **USB/Network Debugging** di Android TV Anda.
2. Sambungkan ADB ke IP Android TV Anda:
   ```bash
   adb connect 192.168.1.xxx:5555
   ```
3. Pasang APK ke TV:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
4. Buka aplikasi di TV:
   ```bash
   adb shell am start -n com.tizen.livetv/.ui.MainActivity
   ```

---

## 📂 Struktur Direktori Proyek

```
TizenLiveTV/
├── app/
│   ├── build.gradle.kts                   # Konfigurasi Gradle modul app (Media3, Glide, TV Provider)
│   ├── src/main/
│   │   ├── AndroidManifest.xml            # Manifest dengan HOME launcher, LEANBACK, BOOT_COMPLETED, TIF
│   │   ├── java/com/tizen/livetv/
│   │   │   ├── LiveTvApplication.kt       # Inisialisasi aplikasi
│   │   │   ├── data/
│   │   │   │   ├── ChannelRepository.kt   # Pengelola database saluran, cache, TvProvider sync
│   │   │   │   ├── SampleChannels.kt      # Saluran bawaan (Google samples + public live streams)
│   │   │   │   └── M3uParser.kt           # Parser playlist IPTV (.m3u / .m3u8)
│   │   │   ├── model/
│   │   │   │   ├── Channel.kt             # Model data saluran TV
│   │   │   │   ├── Program.kt             # Model data acara EPG
│   │   │   │   └── ChannelGenre.kt        # Kategori siaran (Berita, Olahraga, Film, dll)
│   │   │   ├── player/
│   │   │   │   └── LiveTvPlayerManager.kt # Engine pemutar ExoPlayer/Media3 (HLS, DASH, Aspect Ratio)
│   │   │   ├── receiver/
│   │   │   │   └── BootReceiver.kt        # Auto-boot handler saat TV dinyalakan
│   │   │   ├── tif/
│   │   │   │   └── RichTvInputService.kt  # Android TV Input Framework service
│   │   │   └── ui/
│   │   │       ├── MainActivity.kt        # Controller utama TV, D-pad dispatcher & UI manager
│   │   │       ├── adapter/               # Adapter untuk Smart Hub, EPG, Category pills
│   │   │       └── dialog/                # Dialog pengaturan & import IPTV M3U
│   │   └── res/
│   │       ├── drawable/                  # Komponen visual Samsung Tizen (glass cards, badges, glows)
│   │       ├── layout/                    # Layout UI Smart Hub, Info Banner, EPG Grid, Numeric OSD
│   │       └── values/                    # Warna neon Tizen, string multi-bahasa, tema Leanback
├── build.gradle.kts                       # Root build file
├── settings.gradle.kts                    # Root settings file
└── README.md                              # Dokumentasi lengkap
```
