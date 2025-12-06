# Analisis Tugas: Minggu 12

## 1. Apakah Encounter bagian dari Visit, atau Visit bagian dari Encounter?

**Jawaban:**
Sebuah **Encounter adalah bagian dari Visit**.

**Alasan (Berdasarkan Foreign Key):**
Dalam skema database, tabel `encounter` memiliki kolom *Foreign Key* bernama `visit_id` yang merujuk ke tabel `visit`.
* Hal ini menjadikan tabel `visit` sebagai Induk (*Parent*) dan `encounter` sebagai Anak (*Child*).
* Secara logika, pasien datang ke RS untuk satu "Kunjungan/Visit" (misal: dari jam 08:00 sampai 12:00), dan dalam rentang waktu tersebut, mereka bisa melakukan beberapa "Encounter" (Pendaftaran, Tensi Darah, Konsultasi Dokter, Lab).

## 2. Bagaimana Provider terhubung ke Person?

**Jawaban:**
Seorang **Provider terhubung ke Person melalui Foreign Key `person_id`**.

**Alasan:**
Dengan melakukan *reverse engineer* pada tabel `provider`, terlihat adanya kolom `person_id`.
* Tabel `provider` tidak menyimpan nama atau tanggal lahir secara langsung, melainkan hanya menyimpan ID yang merujuk ke tabel `person`.
* Konsepnya: "Person" adalah manusianya (punya Nama, Gender), sedangkan "Provider" adalah peran/jabatan yang dimainkan orang tersebut dalam sistem. Tidak semua Person adalah Provider, tapi setiap Provider pasti adalah Person.
