package com.example.demo.user;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.DConnentionMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoFactory {

    @Bean
    public UserDao userDao(){
        //팩토리메소는 UserDao타입의 오브젝트를 어떻게 만들고 어떻게 준비시킬지 결정한다.
        return new UserDao(connectionMaker());
    }


    @Bean
    public ConnectionMaker connectionMaker(){
        return new DConnentionMaker();
    }

}
