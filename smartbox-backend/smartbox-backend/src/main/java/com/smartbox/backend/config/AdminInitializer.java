package com.smartbox.backend.config;

import com.smartbox.backend.model.User;
import com.smartbox.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository repository;

    public AdminInitializer(UserRepository repository) {
        this.repository = repository;
    }
    @Override
    public void run(String... args) {

        long adminCount =
                repository.countByRole("ADMIN");

        if(adminCount == 0){

            User user = new User();

            user.setUsername("admin");
            user.setPassword("Admin123");
            user.setPhone("0000000000");
            user.setRole("ADMIN");

            repository.save(user);
        }
    }
}