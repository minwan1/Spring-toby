package com.example.demo.user;

public enum Level {
    GOLD(3, null, "골드"), SILVER(2, GOLD, "실버"), BASIC(1, SILVER, "베이직");

    private final int value;
    private final Level next;
    private final String name;

    Level(int value, Level level, String name) {
        this.value = value;
        this.next = level;
        this.name = name;
    }

    public int intValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public static Level valueOf(int value) {
        switch (value) {
            case 1:
                return BASIC;
            case 2:
                return SILVER;
            case 3:
                return GOLD;
            default:
                throw new AssertionError("Unknown value: " + value);
        }
    }

    public Level nextLevel() {
        return this.next;
    }


}