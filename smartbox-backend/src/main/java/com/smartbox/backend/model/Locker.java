package com.smartbox.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "lockers")
public class Locker {

    @Id
    private String lockerId;

    private String address;
    private double latitude;
    private double longitude;
    private String status;
    private int totalSlots;
    private int availableSlots;
    private List<String> slots;
}
