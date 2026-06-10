package com.smartbox.backend.controller;

import com.smartbox.backend.model.*;
import com.smartbox.backend.repository.ExtendOrderRepository;
import com.smartbox.backend.repository.LockerRepository;
import com.smartbox.backend.repository.OrderRepository;
import com.smartbox.backend.service.PriceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final LockerRepository lockerRepository;
    private final ExtendOrderRepository extendOrderRepository;

    @PostMapping("/create-order")
    public Order createOrder(@RequestBody Order order) {
        order.setPaymentCode(generatePaymentCode());
        order.setLockerCode(generateLockerCode());
        order.setPaymentStatus("PENDING");
        if (order.getLockerId() != null) {
            order.setStatus("ACTIVE");
            if (order.getCodeUsedCount() == null) {
                order.setCodeUsedCount(0);
            }
            if (order.getDuration() != null && order.getRentType() != null) {
                LocalDateTime expireAt =
                        calculateExpireAt(
                                normalizeRentType(order.getRentType()),
                                order.getDuration()
                        );
                order.setExpireAt(expireAt.toString());
                if (order.getAmount() <= 0) {
                    order.setAmount(
                            calculateAmount(
                                    1,
                                    normalizeRentType(order.getRentType()),
                                    order.getDuration()
                            )
                    );
                }
            }
        }
        return orderRepository.save(order);
    }

    private long calculateAmount(
            int quantity,
            String rentType,
            int duration) {

        return (long) quantity
                * duration
                * PriceUtils.getUnitPrice(rentType);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<Order> getOrder(
            @PathVariable String id) {

        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String normalizeRentType(String rentType) {
        if (!hasText(rentType)) {
            return rentType;
        }
        if ("ONCE".equalsIgnoreCase(rentType)) {
            return "ONE_TIME";
        }
        return rentType.toUpperCase(Locale.ROOT);
    }

    private String generatePaymentCode() {
        Random random = new Random();
        int number = 10000 + random.nextInt(90000);
        return "SMB" + number + "VNS";
    }

    private String generateLockerCode() {
        Random random = new Random();
        int number = 1000 + random.nextInt(9000);
        return String.valueOf(number);
    }

    @PostMapping("/check-locker")
    public boolean checkLocker(@RequestBody Order request) {
        Order order = orderRepository.findByLockerCode(request.getLockerCode());
        if (order == null) {
            return false;
        }
        if (order.isReturned()) {
            return false;
        }

        if (order.getEndTime() != null && new Date().after(order.getEndTime())) {
            order.setStatus("EXPIRED");
            orderRepository.save(order);
            return false;
        }

        if (order.getSlots() == null || !order.getSlots().contains(request.getSelectedSlot())) {
            return false;
        }

        if ("ONE_TIME".equals(normalizeRentType(order.getDurationType()))) {
            String slot = request.getSelectedSlot();
            Integer remain = order.getSlotUses() == null ? null : order.getSlotUses().get(slot);
            if (remain == null || remain <= 0) {
                return false;
            }
            order.getSlotUses().put(slot, remain - 1);
            orderRepository.save(order);
        }

        return true;
    }

    @GetMapping("/check-payment/{paymentCode}")
    public PaymentStatusResponse checkPayment(@PathVariable String paymentCode) {
        Order order = orderRepository.findByPaymentCode(paymentCode);
        if (order != null) {
            return new PaymentStatusResponse(order.getPaymentStatus());
        }

        ExtendOrder extendOrder = extendOrderRepository.findByPaymentCode(paymentCode);
        if (extendOrder != null) {
            return new PaymentStatusResponse(extendOrder.getPaymentStatus());
        }

        return new PaymentStatusResponse("NOT_FOUND");
    }

    @PostMapping("/fake-payment/{paymentCode}")
    public String fakePayment(@PathVariable String paymentCode) {
        Order order = orderRepository.findByPaymentCode(paymentCode);
        if (order != null) {
            markOrderPaid(order);
            return "{\"status\":\"SUCCESS\"}";
        }

        ExtendOrder extendOrder = extendOrderRepository.findByPaymentCode(paymentCode);
        if (extendOrder != null) {
            markExtendPaid(extendOrder);
            return "{\"status\":\"SUCCESS\"}";
        }

        return "{\"status\":\"NOT_FOUND\"}";
    }

    @PostMapping({"/webhook/payment", "/sepay-webhook"})
    public ResponseEntity<?> paymentWebhook(@RequestBody Object body) {
        try {
            processWebhookText(String.valueOf(body));
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("ERROR");
        }
    }

    @GetMapping("/orders/{userId}")
    public List<Order> getUserOrders(@PathVariable String userId) {
        return orderRepository.findByUserId(userId);
    }

    @GetMapping("/rented-lockers/{userId}")
    public List<Order> getRentedLockers(@PathVariable String userId) {
        return orderRepository.findByUserId(userId).stream()
                .filter(o -> "PAID".equals(o.getPaymentStatus()))
                .toList();
    }

    @GetMapping("/free-slots/{lockerId}")
    public List<Integer> getFreeSlots(@PathVariable String lockerId) {
        Locker locker = lockerRepository.findById(lockerId).orElse(null);
        if (locker == null) {
            return List.of();
        }

        List<Order> orders = orderRepository.findByLockerId(lockerId);
        Set<Integer> usedSlots = new HashSet<>();
        for (Order order : orders) {
            if ("PAID".equals(order.getPaymentStatus()) && "ACTIVE".equals(order.getStatus()) && order.getSlotNumber() != null) {
                usedSlots.add(order.getSlotNumber());
            }
        }

        List<Integer> freeSlots = new ArrayList<>();
        for (int i = 1; i <= locker.getTotalSlots(); i++) {
            if (!usedSlots.contains(i)) {
                freeSlots.add(i);
            }
        }
        return freeSlots;
    }

    @PostMapping("/create-extend/{id}")
    public ExtendOrder createExtendOrder(@PathVariable String id, @RequestBody ExtendRequest request) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }
        ExtendOrder extend = new ExtendOrder();
        extend.setOrderId(id);
        extend.setDuration(request.getDuration());
        extend.setRentType(normalizeRentType(request.getRentType()));
        extend.setPaymentCode(generatePaymentCode());
        extend.setPaymentStatus("PENDING");

        extend.setAmount(
                (int)(
                        PriceUtils.getUnitPrice(
                                extend.getRentType())
                                * extend.getDuration()
                )
        );
        return extendOrderRepository.save(extend);
    }

    @PostMapping("/finish-order/{orderId}")
    public Order finishOrder(@PathVariable String orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        if ("COMPLETED".equals(order.getStatus())) {
            return order;
        }

        Locker locker = lockerRepository.findById(order.getLockerId()).orElse(null);
        if (locker != null) {
            locker.setAvailableSlots(locker.getAvailableSlots() + 1);
            lockerRepository.save(locker);
        }

        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    @PostMapping("/cancel-order/{id}")
    public Order cancelOrder(@PathVariable String id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }
        order.setStatus("CANCELLED");
        order.setCodeUsedCount(0);
        return orderRepository.save(order);
    }

    private void processWebhookText(String rawData) {
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            if (order.getPaymentCode() != null && rawData.contains(order.getPaymentCode())) {
                markOrderPaid(order);
                break;
            }
        }

        List<ExtendOrder> extendOrders = extendOrderRepository.findAll();
        for (ExtendOrder extend : extendOrders) {
            if (extend.getPaymentCode() != null && rawData.contains(extend.getPaymentCode())) {
                markExtendPaid(extend);
                break;
            }
        }
    }

    private void markOrderPaid(Order order) {
        if ("PAID".equals(order.getPaymentStatus())) {
            return;
        }

        order.setPaymentStatus("PAID");
        if (order.getStatus() == null) {
            order.setStatus("ACTIVE");
        }
        orderRepository.save(order);

        if (order.getLockerId() != null) {
            Locker locker = lockerRepository.findById(order.getLockerId()).orElse(null);
            if (locker != null && locker.getAvailableSlots() > 0) {
                locker.setAvailableSlots(locker.getAvailableSlots() - 1);
                lockerRepository.save(locker);
            }
        }
    }

    private void markExtendPaid(ExtendOrder extend) {
        if ("PAID".equals(extend.getPaymentStatus())) {
            return;
        }

        extend.setPaymentStatus("PAID");
        extendOrderRepository.save(extend);

        Order order = orderRepository.findById(extend.getOrderId()).orElse(null);
        if (order == null || order.getExpireAt() == null) {
            return;
        }

        LocalDateTime expireTime = LocalDateTime.parse(order.getExpireAt());
        switch (extend.getRentType()) {
            case "HOUR" -> expireTime = expireTime.plusHours(extend.getDuration());
            case "DAY" -> expireTime = expireTime.plusDays(extend.getDuration());
            case "MONTH" -> expireTime = expireTime.plusMonths(extend.getDuration());
            case "ONE_TIME" -> expireTime = LocalDateTime.now().plusHours(1);
        }
        order.setExpireAt(expireTime.toString());
        orderRepository.save(order);
    }

    private Date calculateEndTime(String durationType, int durationValue) {
        if ("ONE_TIME".equals(durationType)) {
            return null;
        }

        long now = System.currentTimeMillis();
        long end = now;

        switch (durationType) {
            case "HOUR" -> end += durationValue * 60L * 60L * 1000L;
            case "DAY" -> end += durationValue * 24L * 60L * 60L * 1000L;
            case "MONTH" -> end += durationValue * 30L * 24L * 60L * 60L * 1000L;
        }

        return new Date(end);
    }

    private LocalDateTime calculateExpireAt(String rentType, int duration) {
        return switch (rentType) {
            case "HOUR" -> LocalDateTime.now().plusHours(duration);
            case "DAY" -> LocalDateTime.now().plusDays(duration);
            case "MONTH" -> LocalDateTime.now().plusMonths(duration);
            case "ONE_TIME" -> LocalDateTime.now().plusHours(1);
            default -> LocalDateTime.now().plusHours(duration);
        };
    }

    public static class ExtendRequest {
        private int duration;
        private String rentType;

        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public String getRentType() { return rentType; }
        public void setRentType(String rentType) { this.rentType = rentType; }
    }
}
