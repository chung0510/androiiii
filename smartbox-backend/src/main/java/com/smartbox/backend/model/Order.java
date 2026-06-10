package com.smartbox.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "orders")
public class Order {

    @Id
    @JsonProperty("id")

    private String id;
    private String customerName;
    private String phone;
    private String packageType;
    private String paymentCode;
    private String lockerCode;
    private String paymentStatus;
    private List<String> slots;
    private Map<String, Integer> slotUses;
    private String selectedSlot;
    private String durationType;
    private int durationValue;
    private long amount;
    private Date startTime;
    private Date endTime;
    private String status;
    private boolean returned;
    private String userId;
    private String lockerId;
    private String lockerAddress;
    private Integer slotNumber;
    private String expireAt;
    private Integer duration;
    private String rentType;
    private Integer codeUsedCount;
}
