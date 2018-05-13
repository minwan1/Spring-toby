package com.tobi.example.service;

import com.tobi.example.Level;
import com.tobi.example.domain.User;
import com.tobi.example.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UserService {

    protected UserRepository userRepository;
    public static final int MIN_LOGCOUNT_FOR_SILVER = 50;
    public static final int MIN_RECCOMEND_FOR_GOLD = 30;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User updateUserName(User user){

        User pstUser = userRepository.findOne(user.getId());
        pstUser.updateName(user.getName());
        return user;
    }

    @Transactional
    public User createUser(User user){
        return userRepository.save(user);
    }


    @Transactional
    public void upgradeLevels(){
        List<User> users = (List<User>) userRepository.findAll();
        for(User user : users){
            if(canUpgradeLevel(user))
                upgradeLevel(user);
        }
    }

    private boolean canUpgradeLevel(User user){
        Level currentLevel = user.getLevel();
        switch (currentLevel){
            case BASIC: return (user.getLoginCount() >= MIN_LOGCOUNT_FOR_SILVER);
            case SILVER: return (user.getRecommend() >= MIN_RECCOMEND_FOR_GOLD);
            case GOLD: return false;
            default: throw new IllegalArgumentException("Unknown Level");
        }
    }

    protected void upgradeLevel(User user){
        user.updateLevel();
        if(user.getId().equals("test4")) throw new TestUserServiceException();
        userRepository.save(user);
    }

}
