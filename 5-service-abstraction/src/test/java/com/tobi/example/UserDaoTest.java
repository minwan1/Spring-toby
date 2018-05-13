package com.tobi.example;

import com.tobi.example.domain.User;
import com.tobi.example.repository.UserRepository;
import com.tobi.example.service.UserService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoTest {

    private UserDao userDao;

    @Autowired
    private UserService userService;


    @Autowired
    private UserRepository userRepository;
    private User user;
    private String NEW_NAME = "test12345";

    @Before
    public void setUp() throws Exception {
        userDao = new UserDao();
        user = new User().builder()
                .id("test")
                .level(Level.BASIC)
                .loginCount(1)
                .name("test")
                .recommend(1)
                .build();
    }

    @Test
    public void saveUser() {
        userRepository.save(user);
        User user1 = userRepository.findOne(user.getId());
        checkSameUser(user, user1);
    }

    @Test
    public void addAndGet() {

        User user1 = userDao.get(user.getId());
        checkSameUser(user, user1);
    }


    @Test
    public void update() {
        userService.createUser(user);
        User newUser = userService.updateUserName(User.builder()
                .id(user.getId())
                .name(NEW_NAME)
                .build());
        Assert.assertThat(newUser.getName(), CoreMatchers.is(NEW_NAME));
    }

    private void checkSameUser(User user, User user1) {
        Assert.assertThat(user.getId(), CoreMatchers.is(user1.getId()));
        Assert.assertThat(user.getLevel(), CoreMatchers.is(user1.getLevel()));
        Assert.assertThat(user.getRecommend(), CoreMatchers.is(user1.getRecommend()));
    }
}