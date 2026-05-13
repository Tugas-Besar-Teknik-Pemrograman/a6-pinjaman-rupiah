package com.p2p.domain.User;

public interface UserRepository {
    User findByEmail(String email);
    void save(User user);
}