package com.example.demo.user;

import java.sql.Connection;
import java.sql.SQLException;

public class MUserDao extends UserDao{
    @Override
    public Connection getConnection() throws ClassNotFoundException, SQLException {
        return null; // D 사 DB connection 생성코드
    }
}
