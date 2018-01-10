package com.wan.tobi.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public interface StatementStrategy {
	
	//콜백 메소드 
	PreparedStatement makePreparedStatement(Connection c) throws SQLException;

}
