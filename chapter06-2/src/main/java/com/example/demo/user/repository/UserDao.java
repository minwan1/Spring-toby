package com.example.demo.user.repository;

import com.example.demo.user.User;

import java.util.List;

public interface UserDao {

    void add(User user);
    User get(String id);
    void deleteAll();
    int getCount();
    List<User> getAll();
    void update(User user);

}
