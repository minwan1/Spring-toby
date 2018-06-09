package com.example.demo.user.service;

import com.example.demo.user.User;
import com.example.demo.user.repository.UserDao;
import org.springframework.mail.MailSender;

import java.util.List;

public class TestUserService extends UserServiceImpl{
    private String id = "madnite1";

    public TestUserService(UserDao userDao, MailSender mailSender) {
        super(userDao, mailSender);
    }

    protected void upgradeLevel(User user) {
        if (user.getId().equals(this.id)) throw new TestUserServiceException();

        super.upgradeLevel(user);
    }

    static class TestUserServiceException extends RuntimeException {
    }

    public List<User> getAll(){
        for(User user : super.getAll()){
            super.update(user);
        }
        return null;
    }
}
