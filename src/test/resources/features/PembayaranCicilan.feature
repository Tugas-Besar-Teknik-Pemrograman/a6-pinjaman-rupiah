Feature: Pembayaran Cicilan Borrower
Sebagai Borrower yang telah melakukan peminjaman
Saya akan membayar Cicilan per bulan
agar melunasi pinjaman saya 

Scenario: [PC-01] Menghitung pembayaran cicilan dengan skema bunga fixed
Given loan dengan ID "LN-001" memiliki tagihan yang masih aktif sebesar 10000000 dan tenor 5 bulan
And loan tersebut menggunakan bunga fixed sebesar 5% 
When sistem menghitung tagihan bulan ini
Then nominal tagihan mencapai 2500000

Scenario: [PC-02] Menghitung pembayaran cicilan dengan skema bunga syariah
Given loan dengan ID "LN-001" memiliki tagihan yang masih aktif sebesar 10000000 dan tenor 5 bulan
And loan tersebut menggunakan bunga syariah dengan margin flat sebesar 150000 
When sistem menghitung tagihan bulan ini
Then nominal tagihan mencapai 2150000

Scenario: [PC-03] Menghitung pembayaran cicilan dengan skema bunga float
Given loan dengan ID "LN-001" memiliki tagihan yang masih aktif sebesar 10000000 dan tenor 5 bulan
And loan tersebut menggunakan bunga float sebesar 5% 
When sistem menghitung tagihan bulan pertama
Then nominal tagihan mencapai 2500000
When borrower membayar lunas tagihan pertama
And sistem menghitung tagihan bulan kedua
Then tagihan bulan kedua harus 2400000

Scenario: [PC-04] Pembayaran cicilan berhasil dengan nominal pas
Given loan dengan ID "LN-002" memiliki tagihan bulan ini
When borrower melakukan pembayaran sesuai tagihan bulan ini
Then Sistem akan menerima pembayaran 
And sisa tagihan bulan ini akan menjadi 0

Scenario: [PC-05] Pembayaran cicilan gagal karena nominal kurang dari tagihan
Given loan dengan ID "LN-003" memiliki tagihan bulan ini
When borrower melakukan pembayaran kurang dari tagihan bulan ini
Then sistem akan menolak pembayaran 
And sistem mengirim notifikasi "Nominal pembayaran kurang dari nominal tagihan"
Then sisa tagihan bulan ini akan tetap 

Scenario: [PC-06] Pembayaran cicilan terakhir yang melunasi seluruh pinjaman
Given loan dengan ID "LN-004" memiliki sisa tagihan keseluruhan sebesar 2500000 dan status "REPAYMENT"
When borrower melakukan pembayaran lunas sebesar 2500000
Then sisa tagihan keseluruhan akan menjadi 0
And status pinjaman "LN-004" berubah menjadi "CLOSED"

Scenario: [PC-07] Perhitungan tagihan dengan tambahan denda karena keterlambatan (OVERDUE)
Given loan dengan ID "LN-005" memiliki status "OVERDUE"
And loan tersebut memiliki cicilan pokok dan bunga bulan ini sebesar 2500000
When sistem menghitung tagihan bulan ini
Then nominal tagihan harus lebih besar dari 2500000 karena ditambah denda keterlambatan

