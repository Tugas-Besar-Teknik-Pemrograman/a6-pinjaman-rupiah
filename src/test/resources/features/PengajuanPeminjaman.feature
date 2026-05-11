Feature: Pengajuan peminjaman borrower
  Sebagai Borrower
  Saya ingin mengajukan pinjaman
  Agar saya bisa mendapatkan pinjaman dana

  //PP = PengajuanPeminjaman
  Scenario: [PP-01] Pengajuan peminjaman berhasil dengan borrower terverfikasi
    Given Borrower dengan ID "01" terverifikasi (KYC = true)
    When Borrower mengajukan peminjaman sebesar 200000
    Then Sistem membuat Loan dengan status FUNDING

  Scenario: [PP-02] pengajuan peminjaman ditolak dengan borrower tidak terverifikasi
    Given Borrower dengan ID "01" terverifikasi (KYC = false)
    When Borrower mengajukan peminjaman sebesar 200000
    Then Sistem akan menolak peminjaman

  Scenario: [PP-03] pengajuan peminjaman berhasil dengan borrower terverifikasi dan meminjam kurang dari limit peminjaman
    Given Borrower dengan ID "01" terverifikasi
    And limit peminjaman 500000
    When Borrower mengajukan peminjaman sebesar 200000
    Then Sistem membuat Loan dengan status FUNDING

  Scenario: [PP-04] pengajuan peminjaman ditolak dengan borrower terverifikasi dan meminjam lebih dari limit peminjaman
    Given Borrower dengan ID "01" terverifikasi
    And limit peminjaman 500000
    When Borrower mengajukan peminjaman sebesar 600000
    Then Sistem akan menolak peminjaman

  Scenario: [PP-05] Pengajuan peminjaman berhasil dengan borrower terverfikasi dan credit score diatas ambang batas (600)
    Given Borrower dengan ID "01" terverifikasi
    And Credit score Borrower 700
    When Borrower mengajukan peminjaman sebesar 200000
    Then Sistem membuat Loan dengan status FUNDING

  Scenario: [PP-06] Pengajuan peminjaman ditolak dengan borrower terverfikasi dan credit score dibawah ambang batas (600)
    Given Borrower dengan ID "01" terverifikasi
    And Credit score Borrower 500
    When Borrower mengajukan peminjaman sebesar 200000
    Then Sistem akan menolak peminjaman

  Scenario: [PP-07] Pengajuan peminjaman ditolak karena nominal tidak valid (nol atau negatif)
    Given Borrower dengan ID "01" terverifikasi
    When Borrower mengajukan peminjaman sebesar -1
    Then Sistem akan menolak peminjaman dengan pesan "Nominal pinjaman harus lebih dari 0"

  Scenario: [PP-08] Pengajuan peminjaman ditolak karena Borrower masih memiliki pinjaman aktif
    Given Borrower dengan ID "01" terverifikasi
    And Borrower "01" memiliki pinjaman aktif dengan status "DISBURSED"
    When Borrower mengajukan peminjaman sebesar 200000
    Then Sistem akan menolak peminjaman dengan pesan "Harap lunasi pinjaman sebelumnya terlebih dahulu"