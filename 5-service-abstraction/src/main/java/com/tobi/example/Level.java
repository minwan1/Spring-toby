package com.tobi.example;

public enum Level {
    BASIC(1),
    SILVER(2),
    GOLD(3)
    ;

    private final int value;

    Level(int value){
        this.value = value;
    }

    public int value(){
        return value;
    }
}
