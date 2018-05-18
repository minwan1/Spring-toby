package com.example.demo;

import com.example.demo.user.Level;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserDao;
import com.example.demo.user.UserDaoJdbc;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoJdbcTest {

    @Autowired
    private ApplicationContext applicationContext;

    private UserDao dao;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() throws Exception {
        dao = applicationContext.getBean("userDao",UserDaoJdbc.class);
        this.user1 = new User("gyumee", "박성철", "springno1", Level.BASIC, 1, 0);
        this.user2 = new User("leegw700", "이길원", "springno2", Level.SILVER, 55, 10);
        this.user3 = new User("bumjin", "박범진", "springno3", Level.GOLD, 100, 40);
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

    @Test
    public void update() throws Exception {
        dao.deleteAll();

        dao.add(user1); // 수정할 사용자
        dao.add(user2); // 수정하지 않을 사용자

        user1.setName("오민규");
        user1.setPassword("springno6");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);
        dao.update(user1);

        checkSameUser(user1, dao.get(user1.getId()));
        checkSameUser(user2, dao.get(user2.getId()));
    }


    private void checkSameUser(User givenUser, User actualUser) {
        assertThat(givenUser.getId(), is(actualUser.getId()));
        assertThat(givenUser.getName(), is(actualUser.getName()));
        assertThat(givenUser.getPassword(), is(actualUser.getPassword()));
        assertThat(givenUser.getLevel(), is(actualUser.getLevel()));
        assertThat(givenUser.getLogin(), is(actualUser.getLogin()));
        assertThat(givenUser.getRecommend(), is(actualUser.getRecommend()));
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
