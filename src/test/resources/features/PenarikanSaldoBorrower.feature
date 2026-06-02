Feature: Penarikan Saldo Borrower
  Sebagai Borrower
  Saya ingin melakukan penarikan dari saldo tersedia
  Agar saya dapat mencairkan saldo tersebut

  Scenario: [PSB-01] Penolakan penarikan dana apabila saldo tersedia < 100k
    Given Borrower ID "01" dengan terverifikasi (KYC = true)
    When Borrower mengajukan penarikan dana < 100k
    Then Sistem akan menolak penarikan borrower dengan pesan error "Nominal penarikan minimal harus 100000"

  Scenario: [PSB-02] Penolakan penarikan dengan Borrower tidak terverifikasi
    Given Borrower ID "01" dengan terverifikasi (KYC = false)
    When Borrower mengajukan penarikan dana > 100k
    Then Sistem akan menolak penarikan borrower dengan pesan error "Borrower tidak terverifikasi (KYC = false)"

  Scenario: [PSB-03] Pengajuan penarikan dana berhasil dengan data valid
    Given Borrower ID "01" dengan terverifikasi (KYC = true)
    When Borrower mengajukan penarikan dana > 100k
    Then Sistem akan mengurangi saldo borrower dengan jumlah penarikan yang di input

  Scenario: [PSB-04] Penolakan penarikan jika nominal > saldo tersedia
    Given Borrower ID "01" dengan terverifikasi (KYC = true)
    And Saldo borrower tersedia hanya 300k
    When Borrower mengajukan penarikan dana 1 juta
    Then Sistem akan menolak penarikan borrower karena saldo tidak cukup
