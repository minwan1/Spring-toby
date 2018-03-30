package com.tobi.example;

public class UserDao {
    public User get(String id) {
        return new User().builder()
                .id("test")
                .level(Level.BASIC)
                .login(1)
                .recommed(1)
                .build();
    }
}
