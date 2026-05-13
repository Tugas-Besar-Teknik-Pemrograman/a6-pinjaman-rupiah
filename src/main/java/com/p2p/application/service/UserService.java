package com.p2p.application.service;

import com.p2p.domain.User.User;
import com.p2p.domain.User.UserRepository;

public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(String nama, String email, String password, int usia, int role) {

        if (usia < 18) {
            throw new IllegalStateException("Usia minimal untuk mendaftar adalah 18 tahun");
        }

        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            throw new IllegalStateException("Email sudah terdaftar");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password minimal harus 8 karakter");
        }

        User userBaru = new User(nama, email, password, usia, role);

        userRepository.save(userBaru);

        return userBaru;
    }
}