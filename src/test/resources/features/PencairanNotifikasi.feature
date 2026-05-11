Feature: Pencairan Dana dan Notifikasi
  Sebagai sistem P2P Lending
  Saya ingin memproses pencairan dan mengirimkan notifikasi secara otomatis
  Agar Borrower mendapatkan dana dan semua pihak mendapat informasi

  Scenario: [PD-01] Pendanaan terpenuhi dan Loan siap dicairkan
    Given Loan dengan ID "LOAN-001" memiliki status "FUNDING"
    And total dana terkumpul sudah mencapai target 10000000
    When sistem memproses pencairan untuk Loan "LOAN-001"
    Then status Loan "LOAN-001" harus berubah menjadi "FUNDING_READY"

  Scenario: [PD-02] Pencairan berhasil setelah Loan siap dicairkan
    Given Loan dengan ID "LOAN-001" memiliki status "FUNDING_READY"
    When sistem memproses pencairan untuk Loan "LOAN-001"
    Then status Loan "LOAN-001" harus berubah menjadi "DISBURSED"

  Scenario: [PD-03] Borrower menerima notifikasi setelah dana dicairkan
    Given Loan dengan ID "LOAN-001" memiliki status "DISBURSED" 
    And Borrower dengan ID "BR-001" terdaftar di sistem
    When sistem mengirimkan notifikasi pencairan untuk Loan "LOAN-001"
    Then Borrower dengan ID "BR-001" harus menerima notifikasi berhasil

  Scenario: [PD-04] Pencairan gagal jika pendanaan belum terpenuhi
    Given Loan dengan ID "LOAN-002" memiliki status "FUNDING"
    And total dana terkumpul baru mencapai 4000000 dari target 10000000
    When sistem memproses pencairan untuk Loan "LOAN-002"
    Then sistem harus menolak pencairan dengan pesan error
    And status Loan "LOAN-002" tetap "FUNDING"

  Scenario: [PD-05] Borrower menerima notifikasi gagal jika pencairan ditolak
    Given Loan dengan ID "LOAN-002" memiliki status "FUNDING"
    And Borrower dengan ID "BR-002" terdaftar di sistem
    And pencairan untuk Loan "LOAN-002" ditolak karena dana belum terpenuhi
    When sistem mengirimkan notifikasi pencairan untuk Loan "LOAN-002"
    Then Borrower dengan ID "BR-002" harus menerima notifikasi gagal dengan alasan "pendanaan belum terpenuhi"

  