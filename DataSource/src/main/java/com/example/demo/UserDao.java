package com.example.demo;

import java.sql.SQLException;

public class UserDao {
	
	public void add(){
		System.out.println("add");
	}
	
	public void get(){
		System.out.println("get");
	}
	
	public void deleteAll() throws SQLException{
		System.out.println("delete from users");
	}
	
	public void getCount() throws SQLException{
		System.out.println("select count(*) from users");
	}

}
