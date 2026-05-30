package com.p2p.domain.user.factory;

import com.p2p.domain.user.User;
import java.math.BigDecimal;

public class LenderUserFactory extends UserFactory {
    @Override
    public User createUser(String nama, String email, String password, int usia, BigDecimal penghasilan) {
        return new User(nama, email, password, usia, 2, BigDecimal.ZERO);
    }
}
