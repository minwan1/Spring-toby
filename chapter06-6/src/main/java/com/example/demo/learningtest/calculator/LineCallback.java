package com.example.demo.learningtest.calculator;

import java.io.IOException;

public interface LineCallback<T> {
    T doSomethingWithReader(String line, T result) throws IOException;
}
