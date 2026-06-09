package com.example.timhieu.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/create-order")
    fun createOrder(
        @Body request: CreateOrderRequest
    ): Call<CreateOrderResponse>

    @POST("api/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

    @POST("api/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @GET("api/check-payment/{paymentCode}")
    fun checkPayment(
        @Path("paymentCode") paymentCode: String
    ): Call<PaymentStatusResponse>

    @POST("generate-lockers")
    fun generateLockers(
        @Body request: LocationRequest
    ): Call<List<Locker>>

    @GET("lockers")
    fun getLockers(): Call<List<Locker>>

    @GET("api/orders/{userId}")
    fun getUserOrders(
        @Path("userId") userId: String
    ): Call<List<Order>>

    @GET("api/rented-lockers/{userId}")
    fun getRentedLockers(
        @Path("userId") userId: String
    ): Call<List<Order>>

    @POST("api/create-extend/{id}")
    fun createExtendOrder(
        @Path("id") orderId: String,
        @Body request: ExtendOrderRequest
    ): Call<ExtendOrderResponse>

    @POST("api/finish-order/{orderId}")
    fun finishOrder(
        @Path("orderId") orderId:String
    ): Call<Order>

    @GET("api/free-slots/{lockerId}")
    fun getFreeSlots(
        @Path("lockerId") lockerId: String
    ): Call<List<Int>>
}
