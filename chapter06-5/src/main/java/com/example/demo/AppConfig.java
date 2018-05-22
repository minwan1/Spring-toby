package com.example.demo;

import com.example.demo.connection.ConnectionMaker;
import com.example.demo.connection.DConnentionMaker;
import com.example.demo.mail.DummyMailService;
import com.example.demo.message.MessageFactoryBean;
import com.example.demo.proxy.NameMatchClassMethodPointcut;
import com.example.demo.transaction.TransactionAdvice;
import com.example.demo.transaction.TxProxyFactoryBean;
import com.example.demo.user.repository.UserDaoJdbc;
import com.example.demo.user.repository.UserDao;
import com.example.demo.user.service.TestUserServiceImpl;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserServiceImpl;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public UserDao userDao() throws ClassNotFoundException {
        return new UserDaoJdbc(dataSource);
    }

    @Bean
    public ConnectionMaker connectionMaker(){
        return new DConnentionMaker();
    }


    @Bean
    public PlatformTransactionManager platformTransactionManager(){
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionAdvice transactionAdvice(){
        return new TransactionAdvice(platformTransactionManager());
    }

    @Bean
    public NameMatchClassMethodPointcut transactionPointcut(){
        NameMatchClassMethodPointcut pointcut = new NameMatchClassMethodPointcut();
        pointcut.setMappedName("upgrade*");
        pointcut.setMappedClassName("*ServiceImpl");
        return pointcut;
    }

    @Bean
    public DefaultPointcutAdvisor transactionAdvisor(){
        DefaultPointcutAdvisor defaultPointcutAdvisor = new DefaultPointcutAdvisor();
        defaultPointcutAdvisor.setAdvice(transactionAdvice());
        defaultPointcutAdvisor.setPointcut(transactionPointcut());
        return defaultPointcutAdvisor;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator(){
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public MailSender mailSender(){
        return new DummyMailService();
    }

    @Bean
    public UserService userService() throws ClassNotFoundException {
        return new UserServiceImpl(userDao(), mailSender());
    }

    @Bean
    public UserService testUserService() throws ClassNotFoundException {
        return new TestUserServiceImpl(userDao(), mailSender());
    }

//    @Bean
//    public ProxyFactoryBean userService() throws ClassNotFoundException {
//        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
//        proxyFactoryBean.setTarget(transactionAdvice());
//        proxyFactoryBean.setInterceptorNames("transactionAdvisor");
//        return proxyFactoryBean;
//    }

//    @Bean
//    public NameMatchMethodPointcut nameMatchMethodPointcut(){
//        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
//        pointcut.setMappedName("upgrade*");
//        return pointcut;
//    }

//    @Bean
//    public UserServiceImpl userServiceImpl() throws ClassNotFoundException {
//        return new UserServiceImpl(userDao(), mailSender());
//    }

//    @Bean
//    public MessageFactoryBean message(){
//        MessageFactoryBean messageFactoryBean = new MessageFactoryBean();
//        messageFactoryBean.setText("Factory Bean");
//        return messageFactoryBean;
//    }

}
