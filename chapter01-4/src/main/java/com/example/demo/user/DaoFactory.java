package com.example.demo.user;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.DConnectionMaker;

public class DaoFactory {

    public UserDao userDao(){
        //팩토리메소는 UserDao타입의 오브젝트를 어떻게 만들고 어떻게 준비시킬지 결정한다.
        ConnectionMaker connectionMaker = new DConnectionMaker();
        UserDao userDao = new UserDao(connectionMaker);

        return userDao;
    }

}
