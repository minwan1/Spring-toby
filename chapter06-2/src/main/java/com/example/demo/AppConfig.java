package com.example.demo;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.DConnentionMaker;
import com.example.demo.mail.DummyMailService;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserDaoJdbc;
import com.example.demo.user.repository.UserDao;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserServiceImpl;
import com.example.demo.user.service.UserServiceTx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public UserDao userDao() throws ClassNotFoundException {
        return new UserDaoJdbc(dataSource);
    }

    @Bean
    public ConnectionMaker connectionMaker(){
        return new DConnentionMaker();
    }

    @Bean
    public UserService userService() throws ClassNotFoundException {
        return new UserServiceTx(userServiceImpl(), platformTransactionManager());
    }

    @Bean
    public UserServiceImpl userServiceImpl() throws ClassNotFoundException {
        return new UserServiceImpl(userDao(), mailSender());
    }

    @Bean
    public MailSender mailSender(){
        return new DummyMailService();
    }

    @Bean
    public PlatformTransactionManager platformTransactionManager(){
        return new DataSourceTransactionManager(dataSource);
    }

}
