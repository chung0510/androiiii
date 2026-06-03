package com.smartbox.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "orders")
public class Order {

    @Id
    private String id;
    private String customerName;
    private String phone;
    private String packageType;
    private String paymentCode;
    private String lockerCode;
    private String paymentStatus;
    private String userId;
    private String lockerId;
    private String address;
    private String status;
    private String expireAt;
    private Integer duration;
    private String rentType;
}