package com.smartbox.backend.controller;

import java.util.List;
import com.smartbox.backend.model.Order;
import com.smartbox.backend.model.PaymentStatusResponse;
import com.smartbox.backend.model.SePayWebhookRequest;
import com.smartbox.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import com.smartbox.backend.model.Locker;
import com.smartbox.backend.repository.LockerRepository;

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

    @PostMapping("/create-order")
    public Order createOrder(
            @RequestBody Order order
    ) {

        order.setPaymentCode(generatePaymentCode());

        order.setLockerCode(generateLockerCode());

        order.setPaymentStatus("PENDING");

        order.setStatus("ACTIVE");

        LocalDateTime expireTime;

        if ("HOUR".equals(order.getRentType())) {
            expireTime = LocalDateTime.now().plusHours(order.getDuration());
        }
        else {
            expireTime = LocalDateTime.now().plusDays(order.getDuration());
        }

        order.setExpireAt(expireTime.toString());

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

        Order order =
                orderRepository.findByLockerCode(
                        request.getLockerCode());

        return order != null;
    }

    @GetMapping("/check-payment/{paymentCode}")
    public PaymentStatusResponse checkPayment(
            @PathVariable String paymentCode
    ) {

        List<Order> orders =
                orderRepository.findAll();

        for(Order order : orders){

            if(order.getPaymentCode()
                    .equals(paymentCode)){

                return new PaymentStatusResponse(
                        order.getPaymentStatus());
            }
        }

        return new PaymentStatusResponse(
                "NOT_FOUND");
    }


    @PostMapping("/fake-payment/{paymentCode}")
    public String fakePayment(
            @PathVariable String paymentCode
    ) {

        List<Order> orders =
                orderRepository.findAll();

        for(Order order : orders){

            if(order.getPaymentCode()
                    .equals(paymentCode)){

                order.setPaymentStatus("PAID");

                orderRepository.save(order);

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
            System.out.println("========== WEBHOOK ==========");
            System.out.println(body);
            String rawData = body.toString();
            List<Order> orders = orderRepository.findAll();
            for(Order order : orders){
                if(rawData.contains(order.getPaymentCode())){
                    order.setPaymentStatus("PAID");
                    orderRepository.save(order);
                    System.out.println("PAID SUCCESS: " + order.getPaymentCode()
                    );
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
    @PostMapping("/extend-order/{orderId}")
    public Order extendOrder(
            @PathVariable String orderId,
            @RequestBody ExtendRequest request
    ) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        LocalDateTime expireTime = LocalDateTime.parse(order.getExpireAt().substring(0, 19));
        if ("HOUR".equals(request.getRentType())) {
            expireTime = expireTime.plusHours(request.getDuration());
        } else {
            expireTime = expireTime.plusDays(request.getDuration());
        }
        order.setExpireAt(expireTime.toString()
        );
        return orderRepository.save(order);
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
}