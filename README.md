# Penyelesaian Puzzle Rush Hour Menggunakan Algoritma Pathfinding - Tugas Kecil 3 IF2211 Strategi Algoritma
<img src="demo.gif" alt="demo animation" width="350">

## Deskripsi Singkat

Proyek ini merupakan implementasi penyelesaian puzzle **Rush Hour** menggunakan algoritma pathfinding seperti Uniform Cost Search (UCS), Greedy Best-First Search (GBFS), dan A* (A-Star). Program menerima konfigurasi papan Rush Hour, kemudian mencari solusi minimum untuk memindahkan mobil merah keluar dari papan. Antarmuka pengguna berbasis JavaFX memudahkan visualisasi proses pencarian solusi.

## Persyaratan Sistem
Sebelum menjalankan program, pastikan Anda telah melakukan instalasi Java Development Kit (JDK) untuk menjalankan program berbasis Java ini. Anda dapat mengunduh JDK melalui [pranala](https://www.oracle.com/in/java/technologies/downloads/#java23) ini.

Selain itu, pastikan Maven telah terinstal untuk mengelola dependensi JavaFX. Anda dapat mengunduh Maven melalui [pranala](https://maven.apache.org/download.cgi) ini.

## Instalasi / Memulai
Silakan clone repositori ini dengan menjalankan perintah di bawah pada terminal.
```sh
git clone https://github.com/karolyangqian/Tucil3_13523077_13523093.git
cd Tucil3_13523077_13523093
```

### Menjalankan Program

#### Dengan Maven (Direkomendasikan untuk JavaFX)
1. Pastikan Anda sudah berada di direktori proyek.
2. Jalankan perintah berikut untuk menjalankan aplikasi dengan JavaFX:
    ```sh
    mvn clean javafx:run -f src/rushhour/pom.xml
    ```
    Maven akan otomatis mengunduh dependensi yang diperlukan dan menjalankan aplikasi.
### Cara Menggunakan Aplikasi GUI

1. Setelah aplikasi berjalan, Anda dapat mengonfigurasi papan Rush Hour dengan dua cara:
    - **Upload file .txt**: Klik tombol **Upload File** untuk mengunggah file konfigurasi papan dalam format `.txt`.
    - **Input manual**: Masukkan konfigurasi papan secara manual melalui input teks di sisi kiri aplikasi.
2. Setelah konfigurasi selesai, klik tombol **Apply** untuk menerapkan konfigurasi papan.
3. Pilih algoritma pencarian yang ingin digunakan (UCS, GBFS, atau A*) pada menu dropdown di bawah tombol **Apply**.
4. Jika memilih algoritma yang memerlukan heuristik (GBFS atau A*), pilih heuristik yang diinginkan pada menu dropdown di bawahnya.
5. Klik tombol **Solve** untuk memulai proses pencarian solusi.
6. Untuk melihat animasi pergerakan mobil pada papan, klik tombol **Play**.



## Laporan
[Google Docs Laporan](https://docs.google.com/document/d/1t3PP67LsU6CJNsBP9RpmAtOvd8sleLSr8JUu9fH3mqk/edit?usp=sharing)

## Authors
|     NIM    |                  Nama                  |
| :--------: | :------------------------------------: |
| 13523077   | Albertus Christian Poandy              |
| 13523093   | Karol Yangqian Poetracahya             |