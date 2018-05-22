package com.example.demo.learningtest.calculator;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CalcSumTest {

    private Calculator calculator;
    private String filepath;

    @Before
    public void setup() throws IOException {
        calculator = new Calculator();
        filepath = getClass().getClassLoader().getResource("numbers.txt").getPath();
    }

    @Test
    public void testCalcSum() throws Exception {
        assertThat(calculator.calcSum(filepath), is(10));
    }

    @Test
    public void testCalcMultiply() throws Exception {
        assertThat(calculator.calcMultiply(filepath), is(24));
    }

    @Test
    public void concatenateStrings() throws IOException {
        assertThat(calculator.concatenateString(filepath), is("1234"));

    }
}