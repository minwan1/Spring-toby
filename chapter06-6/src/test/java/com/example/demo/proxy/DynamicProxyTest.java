package com.example.demo.proxy;

import com.example.demo.learningtest.proxy.Hello;
import com.example.demo.learningtest.proxy.HelloTarget;
import com.example.demo.learningtest.proxy.UppercaseHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;

import java.lang.reflect.Proxy;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DynamicProxyTest {


    @Test
    public void classNamePointcutAdvisorTest() {

        NameMatchMethodPointcut classMethodPointcut = new NameMatchMethodPointcut() {
            public ClassFilter getClassFilter() {
                return new ClassFilter() {
                    @Override
                    public boolean matches(Class<?> clazz) {
                        return clazz.getSimpleName().startsWith("HelloT");
                    }
                };
            }
        };

        classMethodPointcut.setMappedName("sayH*");

        checkAdviced(new HelloTarget(), classMethodPointcut, true);

        class HelloWorld extends HelloTarget {};
        checkAdviced(new HelloWorld(), classMethodPointcut, false);

        class HelloToby extends HelloTarget {};
        checkAdviced(new HelloToby(), classMethodPointcut, true);
    }

    private void checkAdviced(Object target, Pointcut pointcut, boolean adviced) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        Hello proxyHello = (Hello)proxyFactoryBean.getObject();

        if (adviced) {
            assertThat(proxyHello.sayHello("Toby"), is("HELLO TOBY"));
            assertThat(proxyHello.sayHi("Toby"), is("HI TOBY"));
            assertThat(proxyHello.sayThankYou("Toby"), is("Thank You Toby"));
        } else {
            assertThat(proxyHello.sayHello("Toby"), is("Hello Toby"));
            assertThat(proxyHello.sayHi("Toby"), is("Hi Toby"));
            assertThat(proxyHello.sayThankYou("Toby"), is("Thank You Toby"));
        }
    }



    @Test
    public void simpleProxy() { // Spring jdk 프록시 빈 생성
        final Hello hello =  (Hello) Proxy.newProxyInstance(getClass().getClassLoader()
                ,new Class[]{Hello.class}
                ,new UppercaseHandler(new HelloTarget())
        );

        assertThat(hello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(hello.sayHi("Toby"), is("HI TOBY"));
        assertThat(hello.sayThankYou("Toby"), is("THANK YOU TOBY"));
    }

    @Test
    public void proxyFactoryBean() { // Spring 팩토리 빈 생성

        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());
        pfBean.addAdvice(new UppercaseAdvice());

        Hello proxyHello = (Hello) pfBean.getObject();

        assertThat(proxyHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxyHello.sayHi("Toby"), is("HI TOBY"));
        assertThat(proxyHello.sayThankYou("Toby"), is("THANK YOU TOBY"));
    }

    @Test
    public void pointcutAdvisor() { // Spring 팩토리 빈 생성

        ProxyFactoryBean pfBean = new ProxyFactoryBean();
        pfBean.setTarget(new HelloTarget());

        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*");

        pfBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        Hello proxyHello = (Hello) pfBean.getObject();

        assertThat(proxyHello.sayHello("Toby"), is("HELLO TOBY"));
        assertThat(proxyHello.sayHi("Toby"), is("HI TOBY"));
        assertThat(proxyHello.sayThankYou("Toby"), is("Thank You Toby"));
    }

    static class UppercaseAdvice implements MethodInterceptor{
        @Override
        public Object invoke(MethodInvocation methodInvocation) throws Throwable {
            String ret = (String) methodInvocation.proceed();
            return ret.toUpperCase();
        }
    }

}
