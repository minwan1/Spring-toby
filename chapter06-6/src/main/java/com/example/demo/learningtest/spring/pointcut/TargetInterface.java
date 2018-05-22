package com.example.demo.learningtest.spring.pointcut;

public interface TargetInterface {
    void hello();
	void hello(String a);
	int minus(int a, int b) throws RuntimeException;
	int plus(int a, int b);
}
