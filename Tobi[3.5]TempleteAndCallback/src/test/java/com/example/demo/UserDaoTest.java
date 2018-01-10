package com.example.demo;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wan.tobi.DaoFactory;
import com.wan.tobi.user.dao.UserDao;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations="/ApplicationContext.xml")
//@ContextConfiguration(classes={BeanA.class, BeanB.class, TestBeanC.class})
@ContextConfiguration(classes={DaoFactory.class})
@DirtiesContext// 테스트 메소드에서 애플리케이션 컨텍스트의 구성이나 상태를 변경한다는것을 테스트 컨텍스트 프레임워크에 알려준다.
public class UserDaoTest {
	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private UserDao dao;
	
	static Set<UserDaoTest> testObjects = new HashSet<UserDaoTest>();
	static ApplicationContext contextObject = null;
	
	
	@Before
	public void setUp(){
//		DataSource dataSource = new SingleConnectionDataSource("jdbc:mysql://localhost:3306/wan", "root", "1234", true);
	}
	
	@Test
	public void addAndGet() throws SQLException{
//		ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
//		dao.add(User user);
		assertThat("test",is("test"));
	}
	
	@Test
	public void test1(){
		assertThat(testObjects, not(hasItem(this)));
		testObjects.add(this);
		
		assertThat(contextObject == null || contextObject == this.context, is(true));
		contextObject = this.context;
	}
	

}
