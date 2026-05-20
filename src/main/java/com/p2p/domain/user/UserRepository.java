package com.p2p.domain.user;

import java.util.List;

public interface UserRepository {
    User findByEmail(String email);
    User findById(UserId id);
    void save(User user);
    List<User> findAll();
}