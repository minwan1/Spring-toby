package com.example.demo.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MessageFactoryBeanTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void getMessageFromFactoryBean() {
        Object message = applicationContext.getBean("message");
        assertThat(message instanceof Message, is(true));
        assertThat(((Message)message).getText(), is("Factory Bean") );
    }


    @Test
    public void getFactoryBean() {
        Object factory = applicationContext.getBean("&message");
        assertThat(factory instanceof MessageFactoryBean, is(true));
    }
}