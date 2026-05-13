Feature: Penarikan Saldo Lender
  Sebagai Lender
  Saya ingin melakukan penarikan dari saldo tersedia
  Agar saya dapat mencairkan saldo tersebut

  Scenario: [PS-01] Penolakan penarikan dana apabila saldo tersedia < 100k
    Given Lender ID "01" dengan terverifikasi (KYC = true)
    When Lender mengajukan penarikan dana < 100k
    Then Sistem akan menolak dengan pesan error

  Scenario: [PS-02] Penolakan penarikan dengan Lender tidak terverifikasi
    Given Lender ID "01" dengan terverifikasi (KYC = false)
    When Lender mengajukan penarikan dana > 100k
    Then Sistem akan menolak penarikan dana dengan pesan error

  Scenario: [PS-03] Pengajuan penarikan dana berhasil dengan data valid
    Given Lender ID "01" dengan terverifikasi (KYC = true)
    When Lender mengajukan penarikan dana > 100k
    Then Sistem akan mengurangi saldo tersedia dengan jumlah penarikan yang di input

  Scenario: [PS-04] Penolakan penarikan jika nominal > saldo tersedia
    Given Lender ID "01" dengan terverifikasi (KYC = true)
    And Saldo tersedia hanya 300k
    When Lender mengajukan penarikan dana 1 juta
    Then Sistem akan menolak karena saldo tidak cukup