package com.example.demo.transaction;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Proxy;

public class TxProxyFactoryBean implements FactoryBean<Object> {
    private Object target;
    private PlatformTransactionManager platformTransactionManager;
    private String pattern;
    private Class<?> serviceInterface;

    public TxProxyFactoryBean(Object target, PlatformTransactionManager platformTransactionManager, String pattern, Class<?> serviceInterface) {
        this.target = target;
        this.platformTransactionManager = platformTransactionManager;
        this.pattern = pattern;
        this.serviceInterface = serviceInterface;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public Object getObject() throws Exception {
        TransactionHandler txHandler = new TransactionHandler();
        txHandler.setTarget(target);
        txHandler.setTransactionManager(platformTransactionManager);
        txHandler.setPattern(pattern);

        return Proxy.newProxyInstance(
                getClass().getClassLoader()
                ,new Class[] { serviceInterface }
                ,txHandler);
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}