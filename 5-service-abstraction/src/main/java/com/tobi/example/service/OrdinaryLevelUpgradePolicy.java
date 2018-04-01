package com.tobi.example.service;

import com.tobi.example.domain.User;
import org.springframework.stereotype.Component;

@Component
public class OrdinaryLevelUpgradePolicy implements UserLevelUpgradePolicy{


    @Override
    public boolean canUpgradeLevel(User user) {
        //비지니스로직
        return false;
    }
}
