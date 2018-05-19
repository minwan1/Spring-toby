package com.example.demo.proxy;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class HelloTargetTest {
    @Test
    public void simpleProxy() {
        Hello hello =  new HelloUppercase(new HelloTarget());
        assertThat(hello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(hello.sayHi("Toby"), is("HI TOBY"));
        assertThat(hello.sayThankYou("Toby"), is("THANK YOU TOBY"));
    }
}