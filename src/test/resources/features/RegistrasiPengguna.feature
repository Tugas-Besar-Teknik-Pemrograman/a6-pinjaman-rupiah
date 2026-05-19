Feature: Registrasi Pengguna
  Sebagai calon pengguna platform P2P Lending
  Saya ingin mendaftar sebuah akun baru
  Agar saya bisa mengakses layanan sebagai Borrower atau Lender

  // RP = Registrasi Pengguna
  Scenario: [RP-01] Registrasi berhasil sebagai Borrower
    Given Sistem belum memiliki pengguna dengan email "Faqihborrower@gmail.com"
    When Calon pengguna mendaftar dengan nama "Faqih1", email "Faqihborrower@gmail.com", password "borrower1234", usia 20 tahun, dan role 1
    Then Sistem berhasil membuat akun pengguna baru
    And Pengguna "Faqihborrower@gmail.com" terdaftar sebagai "Borrower"

  Scenario: [RP-02] Registrasi sukses sebagai Lender
    Given Sistem belum memiliki pengguna dengan email "Faqihlender@gmail.com"
    When Calon pengguna mendaftar dengan nama "Faqih2", email "Faqihlender@gmail.com", password "Lender123", usia 25 tahun, dan role 2
    Then Sistem berhasil membuat akun pengguna baru
    And Pengguna "Faqihlender@gmail.com" terdaftar sebagai "Lender"

  Scenario: [RP-03] Registrasi ditolak karena email sudah terdaftar
    Given Pengguna dengan email "Faqihborrower@gmail.com" sudah terdaftar di sistem
    When Calon pengguna mencoba mendaftar menggunakan email "Faqihborrower@gmail.com"
    Then Sistem akan menolak registrasi dengan pesan "Email sudah terdaftar"

  Scenario: [RP-04] Registrasi ditolak karena calon pengguna di bawah umur legal
    Given Sistem belum memiliki pengguna dengan email "Faqihborrower@gmail.com"
    When Calon pengguna mendaftar dengan nama "Faqih", email "Faqihborrower@gmail.com", password "Faqihborrower123", usia 16 tahun, dan role 1
    Then Sistem akan menolak registrasi dengan pesan "Usia minimal untuk mendaftar adalah 18 tahun"

  Scenario: [RP-05] Registrasi ditolak karena pilihan role tidak valid
    Given Sistem belum memiliki pengguna dengan email "faqihinvalid@gmail.com"
    When Calon pengguna mendaftar dengan nama "Faqih3", email "faqihinvalid@gmail.com", password "Pass123", usia 20 tahun, dan role 4
    Then Sistem akan menolak registrasi dengan pesan "Role pengguna tidak valid"