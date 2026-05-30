package com.p2p.domain.user.factory;

import com.p2p.domain.user.User;
import java.math.BigDecimal;

public abstract class UserFactory {
    public abstract User createUser(String nama, String email, String password, int usia, BigDecimal penghasilan);
}
