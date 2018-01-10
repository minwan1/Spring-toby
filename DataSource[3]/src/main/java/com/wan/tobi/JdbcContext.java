package com.wan.tobi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wan.tobi.user.dao.StatementStrategy;

@Service
public class JdbcContext {
	
	@Autowired
	private DataSource dataSource;

	//템플릿
	public void workWithStatementStrategy(StatementStrategy stmt) throws SQLException{
		Connection c = null;
		PreparedStatement ps = null;

		try {
			c = dataSource.getConnection();
			ps = stmt.makePreparedStatement(c);

		} catch (SQLException e) {
		    throw e;
		} finally {
		    if(ps != null){try{ps.close();}catch(SQLException e){}}
		    if(c != null){try{c.close();}catch(SQLException e){}}
		}
	}
}
