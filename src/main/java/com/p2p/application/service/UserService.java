package com.p2p.application.service;

import com.p2p.domain.borrower.Borrower;
import com.p2p.domain.borrower.BorrowerId;
import com.p2p.domain.borrower.BorrowerRepository;
import com.p2p.domain.lender.Lender;
import com.p2p.domain.lender.LenderId;
import com.p2p.domain.lender.LenderRepository;
import com.p2p.domain.user.User;
import com.p2p.domain.user.UserRepository;
import com.p2p.domain.valueobject.Money;

import java.math.BigDecimal;

public class UserService {

    private final UserRepository userRepository;
    private final BorrowerRepository borrowerRepository;
    private final LenderRepository lenderRepository;

    public UserService(UserRepository userRepository, BorrowerRepository borrowerRepository, LenderRepository lenderRepository) {
        this.userRepository = userRepository;
        this.borrowerRepository = borrowerRepository;
        this.lenderRepository = lenderRepository;
    }

    public User registerUser(String nama, String email, String password, int usia, int role,BigDecimal penghasilan) {

        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid");
        }

        // Buat User menggunakan Factory Method pattern
        com.p2p.domain.user.factory.UserFactory factory;
        if (role == 1) {
            factory = new com.p2p.domain.user.factory.BorrowerUserFactory();
        } else if (role == 2) {
            factory = new com.p2p.domain.user.factory.LenderUserFactory();
        } else {
            throw new IllegalArgumentException("Role pengguna tidak valid");
        }
        User userBaru = factory.createUser(nama, email, password, usia, penghasilan);
        userRepository.save(userBaru);

        // Ambil ID yang di-generate oleh User untuk dipakai sebagai ID Domain
        String generatedId = userBaru.getId().getValue();

        //role borrower
        if (role == 1) {
            BorrowerId bId = new BorrowerId(generatedId);
            Money uangPenghasilan = new Money(penghasilan, Money.IDR);

            // Limit akan otomatis dihitung 30% dari penghasilan oleh constructor
            Borrower profilBorrower = new Borrower(bId, uangPenghasilan);
            borrowerRepository.save(profilBorrower);

        }
        //role lender
        else if (role == 2) {
            LenderId lId = new LenderId(generatedId);
            Money saldoAwal = new Money(BigDecimal.ZERO, Money.IDR);

            Lender profilLender = new Lender(lId, saldoAwal);
            lenderRepository.save(profilLender);
        }

        return userBaru;
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Email tidak ditemukan");
        }

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Password salah");
        }

        return user;
    }
}
