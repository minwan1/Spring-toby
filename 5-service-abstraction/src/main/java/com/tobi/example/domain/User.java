package com.tobi.example.domain;


import com.tobi.example.Level;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
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
    private int recommend;


    @Builder
    public User(String id, Level level, int login, int recommend, String name) {
        this.id = id;
        this.level = level;
        this.login = login;
        this.recommend = recommend;
        this.name = name;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void setLevel(Level level){
        this.level = level;
    }


    public void updateLevel(){
        final Level nextLevel = this.level.nextLevel();
        if(nextLevel == null){
            throw new IllegalStateException(this.level + "은 업그레이드가 불가능합니다.");
        }else{
            this.level = nextLevel;
        }
    }
}
