package com.wan.tobi.user.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

import com.wan.tobi.JdbcContext;
import com.wan.tobi.user.User;

/**
 * 
 * @author wan
 * 클라이언트 역활을함 (콜백/템플릿)
 * 클라이언트 -> 템플릿 -> 콜백
 */
public class UserDao {
	
	
	@Autowired
	private JdbcContext jdbcContext;

	
	public void deleteAll() throws SQLException {
 
		jdbcContext.workWithStatementStrategy(new StatementStrategy() {
			//콜백 메소드 역활
			@Override
			public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
				return c.prepareStatement("delete from users");
			}
		});
	}
	
	public void add(User user) throws SQLException {
		
		jdbcContext.workWithStatementStrategy(new StatementStrategy() {
			
			//콜백 메소드 역활
			@Override
			public PreparedStatement makePreparedStatement(Connection c) throws SQLException {
				PreparedStatement ps = c.prepareStatement("insert into users(id,name,password) values(?,?,?)");
				ps.setString(1, user.getId());
				ps.setString(2, user.getName());
				ps.setString(3, user.getPassword());
				return ps;
			}

		});
		
	}

	public void get(){
		System.out.println("get()");
	}
	
	public void getCount() throws SQLException{
		System.out.println("select count(*) from users");
	}

}
