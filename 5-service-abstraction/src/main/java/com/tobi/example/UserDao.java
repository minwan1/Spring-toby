package com.tobi.example;

import com.tobi.example.domain.User;

public class UserDao {
    public User get(String id) {
        return new User().builder()
                .id("test")
                .level(Level.BASIC)
                .loginCount(1)
                .recommend(1)
                .build();
    }
}
