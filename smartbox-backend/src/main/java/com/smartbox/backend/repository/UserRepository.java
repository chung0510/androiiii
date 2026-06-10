package com.smartbox.backend.repository;

import com.smartbox.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByUsername(String username);
    User findByPhone(String phone);
    long countByRole(String role);
}
