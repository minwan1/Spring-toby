package com.tobi.example;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Getter
@Entity
@NoArgsConstructor
public class User {

    @Id
    private String id;
    private String name;
    private Level level;
    private int login;
    private int recommed;


    @Builder
    public User(String id,Level level, int login, int recommed,String name) {
        this.id = id;
        this.level = level;
        this.login = login;
        this.recommed = recommed;
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateLevel(Level level){
        this.level = level;
    }
}
