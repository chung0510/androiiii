package com.smartbox.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "extend_orders")
public class ExtendOrder {

    @Id
    private String id;

    private String orderId;

    private String paymentCode;

    private Integer duration;

    private String rentType;

    private Integer amount;

    private String paymentStatus;
}