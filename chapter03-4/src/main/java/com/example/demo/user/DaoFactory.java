package com.example.demo.user;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.DConnentionMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DaoFactory {

    @Autowired
    private DataSource dataSource;

    @Bean
    public UserDao userDao() throws ClassNotFoundException {
        return new UserDao(jdbcContext(), dataSource);
    }

    @Bean
    public JdbcContext jdbcContext() throws ClassNotFoundException {
        JdbcContext jdbcContext = new JdbcContext(dataSource);
        return jdbcContext;
    }

    @Bean
    public ConnectionMaker connectionMaker(){
        return new DConnentionMaker();
    }



}
