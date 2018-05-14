package com.example.demo.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionMaker {

    Connection makeConnection() throws ClassNotFoundException, SQLException;
}
