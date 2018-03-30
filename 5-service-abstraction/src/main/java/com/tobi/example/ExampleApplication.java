package com.tobi.example;

import com.tobi.example.repository.UserRepository;
import com.tobi.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class ExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleApplication.class, args);
	}

	@Autowired
    private UserRepository userRepository;

	@Bean
	public UserService userService(){
        return  new UserService(userRepository);
    }
}
