package com.example.demo.learningtest.proxy;

public class HelloUppercase implements Hello{
    private Hello hello;

    public HelloUppercase(Hello hello) {
        this.hello = hello;
    }
    @Override
    public String sayHello(String name) {
        return hello.sayHello(name).toUpperCase();
    }
    @Override
    public String sayHi(String name) {
        return hello.sayHi(name).toUpperCase();
    }
    @Override
    public String sayThankYou(String name) {
        return hello.sayThankYou(name).toUpperCase();
    }
}
