Feature: Pengajuan Investasi Lender
  Sebagai Lender
  Saya bisa mendanai ke loan tertentu
  Sehingga saya berkontribusi dalam crowdfunding

  Scenario: [IL-01] Pengajuan investasi berhasil dengan pada status loan FUNDING
    Given Loan dengan status FUNDING
    When Lender input dana yang ingin diberikan
    Then Loan akan akan terisi sesuai nominal dana yang di input

  Scenario: [IL-02] Pengajuan investasi berhasil dengan pada status loan not FUNDING
    Given Loan dengan status not FUNDING
    When Lender input dana yang ingin diberikan
    Then Sistem akan menolak dengan pesan error karena status not FUNDING

  Scenario: [IL-03] Menolak investasi jika nominal nol atau negatif
    Given Loan dengan status FUNDING
    When Lender input dana investasi <= 0
    Then Sistem harus menolak pengajuan dengan pesan error

  Scenario: [IL-04] Pengajuan investasi jika nominal melebihi target
    Given Loan dengan status FUNDING
    When Lender input dana yang ingin diberikan > target
    Then Sistem akan menolak investasi dengan pesan error