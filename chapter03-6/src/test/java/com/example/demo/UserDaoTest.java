package com.example.demo;

import com.example.demo.user.DaoFactory;
import com.example.demo.user.JdbcContext;
import com.example.demo.user.User;
import com.example.demo.user.UserDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoTest {

    @Autowired
    private ApplicationContext applicationContext;

    private UserDao dao;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() throws Exception {
        dao = applicationContext.getBean("userDao",UserDao.class);
        user1 = new User("gyumee", "박성철", "springno1");
        user2 = new User("leegw700", "이길원", "springno2");
        user3 = new User("bumjin", "박범진", "springno3");
    }

    @Test
    public void getAll() {
        // given
        dao.deleteAll();
        List<User> userList0 = dao.getAll();

        assertThat(userList0.size(), is(0));

        dao.add(user1);
        List<User> userList1 = dao.getAll();
        assertThat(userList1.size(), is(1));
        checkSameUser(user1, userList1.get(0));

        dao.add(user2);
        List<User> userList2 = dao.getAll();
        assertThat(userList2.size(), is(2));
        checkSameUser(user1, userList2.get(0));
        checkSameUser(user2, userList2.get(1));

        dao.add(user3);
        List<User> userList3 = dao.getAll();
        assertThat(userList3.size(), is(3));
        checkSameUser(user3, userList3.get(0));
        checkSameUser(user1, userList3.get(1));
        checkSameUser(user2, userList3.get(2));
    }

    private void checkSameUser(User givenUser, User actualUser) {
        assertThat(givenUser.getId(), is(actualUser.getId()));
        assertThat(givenUser.getName(), is(actualUser.getName()));
        assertThat(givenUser.getPassword(), is(actualUser.getPassword()));
    }


    @Test
    public void addAndGet() throws ClassNotFoundException, SQLException {

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User userget1 = dao.get(user1.getId());
        assertThat(userget1.getName(), is(user1.getName()));
        assertThat(userget1.getPassword(), is(user1.getPassword()));

        User userget2 = dao.get(user2.getId());
        assertThat(userget2.getName(), is(user2.getName()));
        assertThat(userget2.getPassword(), is(user2.getPassword()));

    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() throws SQLException, ClassNotFoundException {

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unkonw_id");
    }


    @Test
    public void count() throws SQLException, ClassNotFoundException {

        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }


}
