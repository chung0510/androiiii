package com.smartbox.backend.controller;

import java.util.List;

import com.smartbox.backend.model.ExtendOrder;
import com.smartbox.backend.model.Order;
import com.smartbox.backend.model.PaymentStatusResponse;
import com.smartbox.backend.repository.ExtendOrderRepository;
import com.smartbox.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import com.smartbox.backend.model.Locker;
import com.smartbox.backend.repository.LockerRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private ExtendOrderRepository
            extendOrderRepository;


    @PostMapping("/create-order")
    public Order createOrder(
            @RequestBody Order order
    ) {
        order.setPaymentCode(generatePaymentCode());
        order.setLockerCode(generateLockerCode());
        order.setPaymentStatus("PENDING");
        order.setStatus("PENDING");
        if("ONCE".equals(order.getRentType()))
        {
            order.setExpireAt(null);
        }
        else
        {
            LocalDateTime expireTime;
            switch(order.getRentType())
            {
                case "HOUR":
                    expireTime = LocalDateTime.now().plusHours(order.getDuration());
                    break;

                case "DAY":
                    expireTime = LocalDateTime.now().plusDays(order.getDuration());
                    break;

                case "MONTH":
                    expireTime = LocalDateTime.now().plusMonths(order.getDuration());
                    break;

                default:
                    expireTime = LocalDateTime.now().plusHours(1);
            }
            order.setExpireAt(expireTime.toString());
        }
        Locker locker = lockerRepository.findById(order.getLockerId()).orElse(null);
        List<Order> sameSlotOrders = orderRepository.findByLockerIdAndSlotNumber(order.getLockerId(), order.getSlotNumber());
        for(Order exist : sameSlotOrders)
        {
            if(!"CANCELLED".equals(exist.getStatus()) && !"COMPLETED".equals(exist.getStatus())) {
                throw new RuntimeException("Ngăn này đã được thuê");
            }
        }
        if (locker == null) {throw new RuntimeException("Locker not found");}
        if (locker.getAvailableSlots() <= 0) {throw new RuntimeException("Locker is full");}
        return orderRepository.save(order);
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
    public boolean checkLocker(
            @RequestBody Order request
    ) {
        Order order = orderRepository.findByLockerCode(request.getLockerCode());
        return order != null;
    }

    @GetMapping("/check-payment/{paymentCode}")
    public PaymentStatusResponse checkPayment(
            @PathVariable String paymentCode
    ) {
        List<Order> orders = orderRepository.findAll();
        for(Order order : orders){
            if(order.getPaymentCode().equals(paymentCode)){
                return new PaymentStatusResponse(order.getPaymentStatus());
            }
        }
        List<ExtendOrder> extendOrders = extendOrderRepository.findAll();
        for(ExtendOrder extend : extendOrders){
            if(extend.getPaymentCode().equals(paymentCode)){
                return new PaymentStatusResponse(extend.getPaymentStatus());
            }
        }
        return new PaymentStatusResponse(
                "NOT_FOUND"
        );
    }

    @PostMapping("/fake-payment/{paymentCode}")
    public String fakePayment(
            @PathVariable String paymentCode
    )
    {
        List<Order> orders = orderRepository.findAll();
        for(Order order : orders){
            if(order.getPaymentCode().equals(paymentCode))
            {
                order.setPaymentStatus("PAID");
                order.setStatus("ACTIVE");
                orderRepository.save(order);
                Locker locker = lockerRepository.findById(order.getLockerId()).orElse(null);
                if(locker != null)
                {
                    locker.setAvailableSlots(locker.getAvailableSlots() - 1);
                    lockerRepository.save(locker);
                }
                return "{\"status\":\"SUCCESS\"}";
            }
        }

        List<ExtendOrder> extendOrders = extendOrderRepository.findAll();
        for(ExtendOrder extend : extendOrders){
            if(extend.getPaymentCode().equals(paymentCode)){
                extend.setPaymentStatus("PAID");
                extendOrderRepository.save(extend);
                Order order = orderRepository.findById(extend.getOrderId()).orElse(null);
                if(order != null){
                    LocalDateTime expireTime = LocalDateTime.parse(order.getExpireAt());
                    switch(extend.getRentType()){
                        case "HOUR":
                            expireTime = expireTime.plusHours(extend.getDuration());
                            break;
                        case "DAY":
                            expireTime = expireTime.plusDays(extend.getDuration());
                            break;
                        case "MONTH":
                            expireTime = expireTime.plusMonths(extend.getDuration());
                            break;
                        case "ONCE":
                            expireTime = LocalDateTime.now().plusHours(1);
                            break;
                    }
                    order.setExpireAt(expireTime.toString());
                    orderRepository.save(order);
                }
                return "{\"status\":\"SUCCESS\"}";
            }
        }
        return "{\"status\":\"NOT_FOUND\"}";
    }

    @PostMapping("/webhook/payment")
    public ResponseEntity<?> paymentWebhook(
            @RequestBody Map<String,Object> body
    ){
        try {
            String rawData = body.toString();
            List<Order> orders = orderRepository.findAll();
            for (Order order : orders) {
                if (rawData.contains(order.getPaymentCode())) {
                    order.setPaymentStatus("PAID");
                    order.setStatus("ACTIVE");
                    orderRepository.save(order);
                    Locker locker = lockerRepository.findById(order.getLockerId()).orElse(null);
                    if(locker != null)
                    {
                        locker.setAvailableSlots(locker.getAvailableSlots() - 1);
                        lockerRepository.save(locker);
                    }
                    System.out.println("ORDER PAID: " + order.getPaymentCode());
                    break;
                }
            }

            List<ExtendOrder> extendOrders = extendOrderRepository.findAll();
            for (ExtendOrder extend : extendOrders) {
                if (rawData.contains(extend.getPaymentCode())) {
                    extend.setPaymentStatus("PAID");
                    extendOrderRepository.save(extend);
                    Order order = orderRepository.findById(extend.getOrderId()).orElse(null);
                    if(order != null){
                        LocalDateTime expireTime = LocalDateTime.parse(order.getExpireAt());
                        switch(extend.getRentType()){
                            case "HOUR":
                                expireTime = expireTime.plusHours(extend.getDuration());
                                break;
                            case "DAY":
                                expireTime = expireTime.plusDays(extend.getDuration());
                                break;
                            case "MONTH":
                                expireTime = expireTime.plusMonths(extend.getDuration());
                                break;
                        }
                        order.setExpireAt(expireTime.toString());
                        orderRepository.save(order);
                    }
                    System.out.println("EXTEND PAID SUCCESS: " + extend.getPaymentCode());
                    break;
                }
            }

            return ResponseEntity.ok("OK");
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body("ERROR");
        }
    }

    @GetMapping("/orders/{userId}")
    public List<Order> getUserOrders(
            @PathVariable String userId
    ) {
        return orderRepository.findByUserId(
                userId
        );
    }

    public static class ExtendRequest {
        private int duration;
        private String rentType;
        public int getDuration() {
            return duration;
        }
        public void setDuration(int duration) {
            this.duration = duration;
        }
        public String getRentType() {
            return rentType;
        }
        public void setRentType(String rentType) {
            this.rentType = rentType;
        }
    }

    @GetMapping("/rented-lockers/{userId}")
    public List<Order> getRentedLockers(
            @PathVariable String userId
    ) {

        return orderRepository.findByUserId(userId)
                .stream()
                .filter(o ->
                        "PAID".equals(
                                o.getPaymentStatus()
                        )
                )
                .toList();
    }

    @GetMapping("/free-slots/{lockerId}")
    public List<Integer> getFreeSlots(
            @PathVariable String lockerId
    )
    {
        Locker locker = lockerRepository.findById(lockerId).orElse(null);
        if(locker == null)
        {
            return List.of();
        }
        List<Order> orders = orderRepository.findByLockerId(lockerId);
        Set<Integer> usedSlots = new HashSet<>();
        for(Order order : orders)
        {
            if(
                    "PAID".equals(order.getPaymentStatus())
                            &&
                            "ACTIVE".equals(order.getStatus())
            )
            {
                usedSlots.add(
                        order.getSlotNumber()
                );
            }
        }
        List<Integer> freeSlots =
                new ArrayList<>();
        for(
                int i = 1;
                i <= locker.getTotalSlots();
                i++
        )
        {
            if(!usedSlots.contains(i))
            {
                freeSlots.add(i);
            }
        }
        return freeSlots;
    }

    @PostMapping("/create-extend/{id}")
    public ExtendOrder createExtendOrder(
            @PathVariable String id,
            @RequestBody ExtendRequest request
    ) {
        Order order = orderRepository.findById(id).orElse(null);
        if(order == null){
            return null;
        }
        ExtendOrder extend = new ExtendOrder();
        extend.setOrderId(id);
        extend.setDuration(request.getDuration());
        extend.setRentType(request.getRentType());
        extend.setPaymentCode(generatePaymentCode());
        extend.setPaymentStatus("PENDING");
        int amount = 0;
        switch(request.getRentType()){
            case "HOUR":
                amount = request.getDuration() * 10000;
                break;
            case "DAY":
                amount = request.getDuration() * 50000;
                break;
            case "MONTH":
                amount = request.getDuration() * 300000;
                break;
        }
        extend.setAmount(amount);
        return extendOrderRepository.save(extend);
    }

    @PostMapping("/finish-order/{orderId}")
    public Order finishOrder(@PathVariable String orderId)
    {
        Order order = orderRepository.findById(orderId).orElse(null);
        if(order == null)
        {
            throw new RuntimeException("Order not found");
        }
        if("COMPLETED".equals(order.getStatus()))
        {
            return order;
        }
        Locker locker = lockerRepository.findById(order.getLockerId()).orElse(null);
        if(locker != null)
        {
            locker.setAvailableSlots(locker.getAvailableSlots() + 1);
            lockerRepository.save(locker);
        }
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    @PostMapping("/cancel-order/{id}")
    public Order cancelOrder(
            @PathVariable String id
    )
    {
        Order order =
                orderRepository
                        .findById(id)
                        .orElse(null);

        if(order == null)
        {
            return null;
        }

        order.setStatus("CANCELLED");
        order.setCodeUsedCount(0);
        return orderRepository.save(order);
    }
}