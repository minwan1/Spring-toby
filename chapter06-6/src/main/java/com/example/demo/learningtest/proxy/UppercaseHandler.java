package com.example.demo.learningtest.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UppercaseHandler implements InvocationHandler {
    private Object target;

    public UppercaseHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object returnValue = method.invoke(target, args);

        if (returnValue instanceof String && method.getName().startsWith("say")) {
            return ((String) returnValue).toUpperCase();
        }

        return returnValue;
    }
}
