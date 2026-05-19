package com.p2p.application.service;

import com.p2p.domain.user.User;
import com.p2p.domain.user.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String nama, String email, String password, int usia, int role) {

        // 1. Cek email duplikat (application-layer concern: perlu akses repository)
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        // 2. Cek format email sederhana (application-layer concern)
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid");
        }

        // 3. Buat User — validasi usia, role, dan password dihandle oleh domain constructor
        User userBaru = new User(nama, email, password, usia, role);

        userRepository.save(userBaru);

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
