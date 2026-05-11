Feature: Penarikan Saldo Lender
  Sebagai Lender
  Saya ingin melakukan penarikan dari saldo tersedia
  Agar saya dapat mencairkan saldo tersebut

  Scenario: [PS-01] Penolakan penarikan dana apabila saldo tersedia kurang dari samadengan 100k
    Given Loan ID "01" dengan terverifikasi (KYC = true)
    When Lender mengajukan penarikan dana < 100k
    Then Sistem akan menolak dengan pesan error

  Scenario: [PS-02] Penolakan penarikan dengan Lender tidak terverifikasi
    Given Loan ID "01" dengan terverifikasi (KYC = false)
    When Lender mengajukan penarikan dana > 100k
    Then Sistem akan menolak penarikan dana dengan pesan error

  Scenario: [PS-03] Pengajuan penarikan dana berhasil dengan data valid
    Given Loan ID "01" dengan terverifikasi (KYC = true)
    When Lender mengajukan penarikan dana > 100k
    Then Sistem akan mengurangi saldo tersedia dengan jumlah penarikan yang di input