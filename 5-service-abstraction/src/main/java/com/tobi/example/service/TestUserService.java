package com.tobi.example.service;

import com.tobi.example.domain.User;
import com.tobi.example.repository.UserRepository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class TestUserService extends UserService{

    private String id;

    public TestUserService(UserRepository userRepository, String id) {
        super(userRepository);
        this.id = id;
    }

    protected void upgradeLevel(User user){
        System.out.println("currentTransactionName : {}"+ TransactionSynchronizationManager.getCurrentTransactionName());
        if(user.getId().equals(this.id)) throw new TestUserServiceException();
        super.upgradeLevel(user);
    }
}
