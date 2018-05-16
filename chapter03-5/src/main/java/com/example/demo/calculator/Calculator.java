package com.example.demo.calculator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Calculator {


    public Integer calcSum(String filepath) throws IOException {
        return fileReadTemplate(filepath, new LineCallback() {
            @Override
            public Integer doSomethingWithReader(String line, Integer result) throws IOException {
                result += Integer.valueOf(line);
                return result;
            }
        }, 0);
    }

    public int calcMultiply(String filepath) throws IOException {
        return fileReadTemplate(filepath, new LineCallback() {
            @Override
            public Integer doSomethingWithReader(String line, Integer result) throws IOException {
                result *= Integer.valueOf(line);
                return result;
            }
        }, 1);
    }

    private Integer fileReadTemplate(String filepath, LineCallback callback, Integer initValue) throws IOException {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filepath));
            Integer result = initValue;
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
