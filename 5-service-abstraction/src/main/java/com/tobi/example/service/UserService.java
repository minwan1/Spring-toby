package com.tobi.example.service;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tobi.example.Level;
import com.tobi.example.User;
import com.tobi.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User updateUserName(User user){

        User pstuser = userRepository.findOne(user.getId());
        pstuser.updateName(user.getName());
        return user;
    }

    @Transactional
    public User createUser(User user){
        return userRepository.save(user);
    }


    @Transactional
    public List<User> upgradeLevels(){
        List<User> users = (List<User>) userRepository.findAll();
        for(User user : users){
            boolean changed = false;

            if(user.getLevel() == Level.BASIC &&user.getLogin() >= 50){
                user.updateLevel(Level.SILVER);
                changed = true;
            }
            else if(user.getLevel() == Level.SILVER &&user.getLogin() >= 30){
                user.updateLevel(Level.SILVER);
                changed = true;
            }else if(user.getLevel() == Level.SILVER){
                changed = false;
            }else{
                changed = false;
            }

            if(changed){
                userRepository.save(users);
            }
        }

        return users;
    }



}
