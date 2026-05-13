package com.p2p.domain.user;

public interface UserRepository {
    User findByEmail(String email);
    void save(User user);
}