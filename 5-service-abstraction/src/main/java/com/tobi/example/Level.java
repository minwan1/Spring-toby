package com.tobi.example;

public enum Level {
    GOLD(3, null),
    SILVER(2, GOLD),
    BASIC(1, SILVER),
    ;

    private final int value;
    private final Level next;

    Level(int value, Level next){
        this.value = value;
        this.next = next;
    }

    public int value(){
        return value;
    }

    public Level nextLevel(){
        return this.next;
    }
}
