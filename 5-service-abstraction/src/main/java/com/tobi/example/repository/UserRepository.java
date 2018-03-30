package com.tobi.example.repository;


import com.tobi.example.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
}
