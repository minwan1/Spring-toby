package com.tobi.example.service;

import com.tobi.example.domain.User;

public interface UserLevelUpgradePolicy {

    boolean canUpgradeLevel(User user);
}
