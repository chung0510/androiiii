package com.smartbox.backend.model;

import lombok.Data;

@Data
public class PaymentStatusResponse {
    private String status;

    public PaymentStatusResponse(String status) {
        this.status = status;
    }
}
