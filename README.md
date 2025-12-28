# ReadBoost ID

ReadBoost ID adalah aplikasi mobile berbasis Android yang dirancang untuk meningkatkan minat dan kemampuan literasi masyarakat Indonesia melalui pengalaman membaca yang lebih menarik, adaptif, dan interaktif. Aplikasi ini menggabungkan konten bacaan, gamifikasi, serta sistem CRUD untuk admin dalam mengelola konten literasi.

## 📋 Daftar Isi
- [Fitur Utama](#-fitur-utama)
- [Teknologi yang Digunakan](#-teknologi-yang-digunakan)
- [Persyaratan Sistem](#-persyaratan-sistem)
- [Instalasi](#-instalasi)
- [Cara Penggunaan](#-cara-penggunaan)
- [Struktur Proyek](#-struktur-proyek)
- [Tim Pengembang](#-tim-pengembang)

## 🚀 Fitur Utama

### Untuk Pengguna
- **Dashboard Personal**: Pantau progress membaca harian dengan target yang dapat disesuaikan
- **Artikel Literasi**: Koleksi artikel berkualitas dalam berbagai kategori (Teknologi, Sains, Psikologi, Sejarah, Motivasi, Sosial & Budaya)
- **Gamifikasi**: Sistem XP dan streak untuk memotivasi membaca rutin
- **Leaderboard**: Kompetisi dengan pembaca lain untuk meningkatkan motivasi
- **Catatan Pribadi**: Fitur untuk menyimpan catatan penting saat membaca
- **Profil & Statistik**: Lihat statistik membaca lengkap dan riwayat progress

### Untuk Admin
- **Panel Admin**: Dashboard khusus untuk mengelola aplikasi
- **Manajemen Artikel**: CRUD lengkap untuk menambah, mengedit, dan menghapus artikel

## 🛠 Teknologi yang Digunakan

### Frontend
- **Kotlin**: Bahasa pemrograman utama
- **Jetpack Compose**: UI toolkit modern untuk Android
- **Material Design 3**: Desain antarmuka yang konsisten

### Backend & Database
- **Firebase Authentication**: Sistem autentikasi pengguna
- **Firebase Firestore**: Database cloud untuk data real-time
- **Room Database**: Database lokal untuk penyimpanan offline

### Networking & Utilities
- **Retrofit**: HTTP client untuk API calls
- **OkHttp**: HTTP client yang powerful
- **Gson**: JSON serialization/deserialization
- **Coil**: Image loading library
- **DataStore**: Penyimpanan preferences

### Architecture
- **MVVM**: Model-View-ViewModel pattern
- **Repository Pattern**: Abstraksi data layer
- **Dependency Injection**: Manual DI dengan AppContainer

## 📱 Persyaratan Sistem
- **Android Studio**: Iguana atau versi lebih baru
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 15)
- **Kotlin**: 1.9.0+
- **Gradle**: 8.0+

## 🔧 Instalasi

1. **Clone Repository**
   ```bash
   git clone https://github.com/naylannajwa/AppReadBoostID.git
   cd ReadBoostID
   ```

2. **Buka di Android Studio**
   - Buka Android Studio
   - Pilih "Open an existing Android Studio project"
   - Pilih folder AppReadBoostID

3. **Konfigurasi Firebase**
   - Buat project baru di [Firebase Console](https://console.firebase.google.com/)
   - Download `google-services.json` dan letakkan di folder `app/`
   - Aktifkan Authentication dan Firestore

4. **Build dan Jalankan**
   - Sinkronkan project dengan Gradle
   - Jalankan di emulator atau device fisik

## 📖 Cara Penggunaan

### Login Aplikasi
- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `user`, password: `user123`

### Fitur Utama
1. **Home**: Lihat progress harian dan artikel terbaru
2. **Articles**: Jelajahi artikel berdasarkan kategori
3. **Leaderboard**: Lihat peringkat pembaca terbaik
4. **Notes**: Buat dan kelola catatan pribadi
5. **Profile**: Lihat statistik dan ubah pengaturan

## 🏗 Struktur Proyek

```
app/src/main/java/com/example/readboostid/
├── data/
│   ├── local/          # Database lokal (Room)
│   ├── model/          # Data models
│   ├── repository/     # Repository implementations
│   └── service/        # External services (Firebase, etc.)
├── di/                 # Dependency injection
├── domain/
│   └── repository/     # Repository interfaces
├── presentation/
│   ├── navigation/     # Navigation setup
│   ├── screens/        # UI screens
│   └── viewmodel/      # ViewModels
└── ui/theme/           # UI theming
```

## 👥 Tim Pengembang

**Tugas Besar PAB 2025 - Kelompok 4951**

1. **NAYLANNAJWA JIHANA UMMA** - NIM: 23523183
2. **ERFINA SAFITRI** - NIM: 23523192
3. **PAMELA NAJLA GHASSANI** - NIM: 23523249
4. **ANINDYA AYU NABILAH** - NIM: 23523268

---

**ReadBoost ID** - Meningkatkan Literasi Digital Indonesia 🇮🇩
