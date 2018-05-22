package com.example.demo.user.service;

import com.example.demo.user.User;
import com.example.demo.user.repository.UserDao;
import org.springframework.mail.MailSender;

public class TestUserServiceImpl extends UserServiceImpl{
    private String id = "madnite1";

    public TestUserServiceImpl(UserDao userDao, MailSender mailSender) {
        super(userDao, mailSender);
    }

    protected void upgradeLevel(User user) {
        if (user.getId().equals(this.id)) throw new TestUserServiceException();

        super.upgradeLevel(user);
    }

    static class TestUserServiceException extends RuntimeException {
    }
}
