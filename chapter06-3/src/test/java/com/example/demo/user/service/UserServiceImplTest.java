package com.example.demo.user.service;

import com.example.demo.transaction.TransactionHandler;
import com.example.demo.transaction.TxProxyFactoryBean;
import com.example.demo.user.Level;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.demo.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static com.example.demo.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDao dao;
    @Autowired
    private MailSender mailSender;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private ApplicationContext context;


    private List<User> users;

    @Before
    public void setUp() throws Exception {
        users = Arrays.asList(
                new User("bumjin", "박범진", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "test1@naver.com"),
                new User("joytouch", "강명성", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "test2@naver.com"),
                new User("erwins", "신승한", "p1", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "test3@naver.com"),
                new User("madnite1", "이상호", "p1", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "test4@naver.com"),
                new User("green", "오민규", "p1", Level.GOLD, 100, Integer.MAX_VALUE, "test5@naver.com")
        );
    }

    @Test
    public void bean() throws Exception {

        assertThat(this.userService, is(notNullValue()));
    }

    @Test
    public void upgradeLevels() throws Exception {

        //given
        UserDao mockUserDao = mock(UserDao.class);
        MailSender mockMailSender = mock(MailSender.class);
        final UserServiceImpl userServiceImpl = new UserServiceImpl(mockUserDao, mockMailSender);
        given(mockUserDao.getAll()).willReturn(this.users);


        //when
        userServiceImpl.upgradeLevels();

        //then

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        verify(mockUserDao, times(2)).update(any(User.class));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(), is(Level.GOLD));


        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));

    }

    @Test
    public void testAdd() throws Exception {
        dao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = dao.get(userWithLevel.getId());
        User userWithoutLevelRead = dao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
    }

    @Test
    @DirtiesContext // 해당 메소드 진행후 spring context 재생성
    public void upgradeAllOrNothing() throws Exception {

        UserServiceImpl testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(dao);
        testUserService.setMailSender(mailSender);

        TxProxyFactoryBean txProxyFactoryBean = context.getBean("&userService", TxProxyFactoryBean.class);
        txProxyFactoryBean.setTarget(testUserService);

        UserService userServiceTx = (UserService) txProxyFactoryBean.getObject();

        dao.deleteAll();
        for(User user : users) dao.add(user);

        try {
            userServiceTx.upgradeLevels();
            fail("TestUserServiceException expected");
        }catch(TestUserServiceException e){
        }

        checkLevel(users.get(1),false);
    }

    private void checkLevel(User user, boolean upgraded) {
        User userUpdate = dao.get(user.getId());

        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        } else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

    static class TestUserService extends UserServiceImpl {
        private String id;

        private TestUserService(String id) {
            super(null, null);
            this.id = id;
        }

        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) {
                throw new TestUserServiceException();
            }
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }

    static class MockMailSender implements MailSender {

        private List<String> requests = new ArrayList<>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]); // 전송 요청을 받은 이메일 주소를 저장해둔다. 첫번쨰 수신자 메일이다
        }

        @Override
        public void send(SimpleMailMessage... mailMessage) throws MailException {

        }
    }

    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList<>();

        public MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated(){
            return this.updated;
        }

        @Override
        public List<User> getAll() {
            return this.users;
        }

        @Override
        public void update(User user) {
            updated.add(user);
        }

        @Override
        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            throw new UnsupportedOperationException();
        }
    }


}