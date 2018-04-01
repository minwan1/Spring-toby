package com.tobi.example.service;

import com.tobi.example.Level;
import com.tobi.example.domain.User;
import com.tobi.example.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static com.tobi.example.service.UserService.MIN_LOGCOUNT_FOR_SILVER;
import static com.tobi.example.service.UserService.MIN_RECCOMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    private List<User> users;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        users = Arrays.asList(
                new User("test1", Level.BASIC,MIN_LOGCOUNT_FOR_SILVER-1,0,"test"),
                new User("test2", Level.BASIC,MIN_LOGCOUNT_FOR_SILVER,0,"test"),
                new User("test3", Level.SILVER,60,MIN_RECCOMEND_FOR_GOLD-1,"test"),
                new User("test4", Level.SILVER,60,MIN_RECCOMEND_FOR_GOLD,"test"),
                new User("test5", Level.GOLD,100,Integer.MAX_VALUE,"test"));
    }

    @Test
    public void upgradeLevels() {

        for(User user :users) userRepository.save(user);

        userService.upgradeLevels();

        checkLevel(users.get(0), false);
        checkLevel(users.get(1), true);
        checkLevel(users.get(2), false);
        checkLevel(users.get(3), true);
        checkLevel(users.get(4), false);
    }

    private void checkLevel(User user, boolean upgraded) {
        User userUpdate = userRepository.findOne(user.getId());
        if(upgraded)
            Assert.assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        else
            Assert.assertThat(userUpdate.getLevel(), is(user.getLevel()));

    }
}