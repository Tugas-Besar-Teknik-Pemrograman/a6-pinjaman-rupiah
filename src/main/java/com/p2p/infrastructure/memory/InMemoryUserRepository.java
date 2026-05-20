package com.p2p.infrastructure.memory;
import com.p2p.domain.user.User;
import com.p2p.domain.user.UserId;
import com.p2p.domain.user.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryUserRepository implements UserRepository{
    private final Map<UserId, User> users = new HashMap<>();

    @Override
    public User findById(UserId id) {
        return users.get(id);
    }

    @Override
    public User findByEmail(String email) {
        for (User user : users.values()) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void save(User user) {
        if (user != null && user.getId() != null) {
            users.put(user.getId(), user);
        }
    }
    
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public void clear() {
        users.clear();
    }
}
