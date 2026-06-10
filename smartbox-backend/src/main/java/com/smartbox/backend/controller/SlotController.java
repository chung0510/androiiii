package com.smartbox.backend.controller;

import com.smartbox.backend.model.Order;
import com.smartbox.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SlotController {

    private final OrderRepository orderRepository;

    @GetMapping("/available-slots")
    public List<String> getAvailableSlots() {

        List<String> allSlots = Arrays.asList(
                "A1","A2","A3","A4",
                "B1","B2","B3","B4"
        );

        Set<String> activeSlots = new HashSet<>();

        for (Order order : orderRepository.findAll()) {

            if (!"PAID".equals(order.getPaymentStatus()))
                continue;

            if (order.getSlots() != null) {
                activeSlots.addAll(order.getSlots());
            }
        }

        List<String> available = new ArrayList<>();

        for (String slot : allSlots) {
            if (!activeSlots.contains(slot)) {
                available.add(slot);
            }
        }

        return available;
    }

    @GetMapping("/active-slots")
    public List<String> getActiveSlots() {

        Set<String> active = new HashSet<>();

        for (Order order : orderRepository.findAll()) {

            if (!"PAID".equals(order.getPaymentStatus()))
                continue;

            if (order.getSlots() != null) {
                active.addAll(order.getSlots());
            }
        }

        return new ArrayList<>(active);
    }
}