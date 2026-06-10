package com.example.smartbox.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("api/create-order")
    Call<Order> createOrder(
            @Body Order order
    );
    @POST("api/check-locker")
    Call<Boolean> checkLocker(
            @Body Order order
    );
    @GET("api/check-payment/{paymentCode}")
    Call<PaymentResponse> checkPayment(
            @Path("paymentCode") String paymentCode
    );
    @POST("api/fake-payment/{paymentCode}")
    Call<PaymentResponse> fakePayment(
            @Path("paymentCode") String paymentCode
    );
    @GET("api/available-slots")
    Call<List<String>> getAvailableSlots();
    @GET("api/active-slots")
    Call<List<String>> getActiveSlots();
}