package com.example.demo.learningtest.spring.pointcut;

import org.junit.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TargetTest {
    @Test
    public void methodSignaturePointcut() throws SecurityException, NoSuchMethodException {

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int  com.example.demo.learningtest.spring.pointcut.Target.minus(int, int) throws java.lang.RuntimeException)");
        // minus 메소드 지정

        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(Target.class.getMethod("minus", int.class, int.class), null), is(true));

        assertThat(pointcut.getClassFilter().matches(Target.class) &&
                pointcut.getMethodMatcher().matches(Target.class.getMethod("plus", int.class, int.class), null), is(false));
        //실패

        assertThat(pointcut.getClassFilter().matches(Bean.class) ,is(false));
        //실패
    }
}