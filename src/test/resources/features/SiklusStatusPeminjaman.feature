Feature: Siklus Status Peminjaman
    Saya sebagai sistem
    Saya ingin memastikan status peminjaman berubah
    Agar status peminjaman sesuai kondisi yang terjadi

    Scenario: [SSP-01] Borrower meminjam uang dan status menjadi "FUNDING"
        Given borrower memiliki akun yang terverifikasi
        When borrower mengajukan pinjaman dengan jumlah tertentu
        Then status peminjaman harus berubah menjadi "FUNDING"
    
    Scenario: [SSP-02] Borrower menerima pinjaman berasal dari lender dan status menjadi "FUNDING_READY"
        Given borrower telah mengajukan pinjaman dan statusnya "FUNDING"
        When Lender mendanai pinjaman tersebut
        Then status peminjaman harus berubah menjadi "FUNDING_READY"

    Scenario: [SSP-03] Borrower menerima uang pinjaman dan status menjadi "DISBURSED"
        Given borrower telah menerima dana pinjaman
        When proses pencairan selesai
        Then status peminjaman harus berubah menjadi "DISBURSED"

    Scenario: [SSP-04] Borrower melakukan pembayaran cicilan dan status menjadi "REPAYMENT"
        Given borrower memiliki pinjaman aktif
        When borrower melakukan pembayaran cicilan
        Then status masih "REPAYMENT" sampai semua cicilan lunas

    Scenario: [SSP-05] Borrower melunasi pinjaman dan status menjadi "CLOSED"
        Given borrower masih memiliki pinjaman terakhir yang harus di bayar
        When borrower melakukan pembayaran cicilan terakhir
        Then status berubah menjadi "CLOSED" karena sudah lunas

    Scenario: [SSP-06] Borrower meminjam namun ditolak dan status menjadi "REJECTED"
        Given borrower mengajukan pinjaman
        When sistem melakukan validasi pinjaman
        Then status pengajuan berubah menjadi "REJECTED"
        And Diberikan text penolakan yang berisi alasan kenapa ditolak
        
        Examples: Alasan(Akun Tidak Terverifikasi,Akun Credit Score nya Rendah, Pinjaman melebihi limit)

    Scenario: [SSP-07] Borrower mengajukan pinjaman namun tidak ada yang mendanai lalu status menjadi "CANCELED"
        Given borrower mengajukan pinjaman
        When tidak ada lender yang mendanai dalam waktu tertentu
        Then status pengajuan berubah menjadi "CANCELED"

    Scenario: [SSP-08] Borrower telat melakukan pembayaran cicilan dan status menjadi "OVERDUE"
        Given borrower memiliki pinjaman aktif
        When borrower melewati tanggal jatuh tempo pembayaran cicilan
        Then status peminjaman berubah menjadi "OVERDUE"

    Scenario: [SSP-09] Borrower melakukan pembayaran cicilan setelah jatuh tempo dan status menjadi "REPAYMENT"
        Given borrower memiliki pinjaman dengan status "OVERDUE"
        When borrower melakukan pembayaran cicilan setelah jatuh tempo dengan dendanya
        Then status peminjaman berubah kembali menjadi "REPAYMENT"
