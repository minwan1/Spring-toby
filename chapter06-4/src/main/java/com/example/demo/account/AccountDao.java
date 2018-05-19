package com.example.demo.account;

import com.example.demo.connection.ConnectionMaker;

public class AccountDao {

    private ConnectionMaker connectionMaker;

    public AccountDao(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }
}
