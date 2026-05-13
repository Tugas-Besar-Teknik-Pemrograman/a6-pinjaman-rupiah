package com.p2p.domain.user;

public interface UserRepository {
    User findByEmail(String email);
    User findById(UserId id);

    void save(User user);
}