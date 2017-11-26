package com.example.demo;

import java.sql.SQLException;

public class UserDao {
	
	public void add(){
		System.out.println("zxcv");
	}
	
	public void get(){
		System.out.println("asdf");
	}
	
	public void deleteAll() throws SQLException{
		System.out.println("delete from users");
	}
	
	public void getCount() throws SQLException{
		System.out.println("select count(*) from users");
	}

}
