package com.example.demo.mail;

import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class DummyMailService implements MailSender {

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        System.out.println("전송");
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        System.out.println("전송");
    }
}
