package com.smartbox.backend.repository;

import com.smartbox.backend.model.Locker;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LockerRepository
        extends MongoRepository<Locker, String> {
}