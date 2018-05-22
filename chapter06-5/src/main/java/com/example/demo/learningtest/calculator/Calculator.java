package com.example.demo.learningtest.calculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {

    public Integer calcSum(String filepath) throws IOException {
        return fileLineReadTemplate(filepath, new LineCallback<Integer>() {
            @Override
            public Integer doSomethingWithReader(String line, Integer result) throws IOException {
                result += Integer.valueOf(line);
                return result;
            }
        }, 0);
    }

    public Integer calcMultiply(String filepath) throws IOException {
        return fileLineReadTemplate(filepath, new LineCallback<Integer>() {
            @Override
            public Integer doSomethingWithReader(String line, Integer result) throws IOException {
                result *= Integer.valueOf(line);
                return result;
            }
        }, 1);
    }

    public String concatenateString(String stringFilepath) throws IOException {
        return fileLineReadTemplate(stringFilepath, new LineCallback<String>() {
            @Override
            public String doSomethingWithReader(String line, String result) throws IOException {
                return result + line;
            }
        }, "");
    }

    private <T>T fileLineReadTemplate(String filepath, LineCallback<T> callback, T initValue) throws IOException {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filepath));
            T result = initValue;
            String line = null;

            while ((line = br.readLine()) != null) {
                result = callback.doSomethingWithReader(line, result);
            }
            return result;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

}
