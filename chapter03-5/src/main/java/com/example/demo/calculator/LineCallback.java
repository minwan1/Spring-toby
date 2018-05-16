package com.example.demo.calculator;

import java.io.BufferedReader;
import java.io.IOException;

public interface LineCallback<T> {
    T doSomethingWithReader(String line, T result) throws IOException;
}
