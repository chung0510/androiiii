package com.smartbox.backend.repository;

import com.smartbox.backend.model.ExtendOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExtendOrderRepository
        extends MongoRepository<ExtendOrder,String> {

    ExtendOrder findByPaymentCode(
            String paymentCode
    );
}