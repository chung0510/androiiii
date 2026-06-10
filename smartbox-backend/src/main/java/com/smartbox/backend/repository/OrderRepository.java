package com.smartbox.backend.repository;

import com.smartbox.backend.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {

    Order findByLockerCode(String lockerCode);

    List<Order> findByUserId(String userId);

    Order findByPaymentCode(String paymentCode);

    List<Order> findByLockerId(String lockerId);

    List<Order> findByLockerIdAndSlotNumber(String lockerId, Integer slotNumber);
}
