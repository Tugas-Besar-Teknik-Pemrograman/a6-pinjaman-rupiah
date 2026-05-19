Feature: Login Pengguna
  Sebagai pengguna terdaftar
  Saya ingin bisa masuk ke dalam sistem
  Agar saya bisa menggunakan fitur aplikasi sesuai role saya

  Scenario: [LG-01] Login berhasil sebagai Borrower
    Given Pengguna dengan email "Faqihborrower@gmail.com" dan password "Faqih123" sudah terdaftar sebagai role 1
    When Pengguna mencoba login dengan email "Faqihborrower@gmail.com" dan password "Faqih123"
    Then Sistem akan mengizinkan akses
    And Sistem mengenali pengguna sebagai "Borrower"

  Scenario: [LG-02] Login berhasil sebagai Lender
    Given Pengguna dengan email "Faqihlender@gmail.com" dan password "Lender123" sudah terdaftar sebagai role 2
    When Pengguna mencoba login dengan email "Faqihlender@gmail.com" dan password "Lender123"
    Then Sistem akan mengizinkan akses
    And Sistem mengenali pengguna sebagai "Lender"

  Scenario: [LG-03] Login gagal karena password salah
    Given Pengguna dengan email "bima@gmail.com" dan password "Bima123" sudah terdaftar sebagai role 1
    When Pengguna mencoba login dengan email "bima@gmail.com" dan password "SalahPassword"
    Then Sistem akan menolak login dengan pesan "Password salah"

  Scenario: [LG-04] Login gagal karena email belum terdaftar
    Given Sistem belum memiliki pengguna dengan email "anonim@gmail.com" untuk login
    When Pengguna mencoba login dengan email "anonim@gmail.com" dan password "Bebas123"
    Then Sistem akan menolak login dengan pesan "Email tidak ditemukan"