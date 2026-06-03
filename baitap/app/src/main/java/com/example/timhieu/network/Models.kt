package com.example.timhieu.network

data class Locker(
    val lockerId: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val status: String,
    val totalSlots: Int,
    val availableSlots: Int
)

data class Order(
    val id: String?,
    val customerName: String?,
    val phone: String?,
    val packageType: String?,
    val paymentCode: String?,
    val lockerCode: String?,
    val paymentStatus: String?,
    val userId: String?,
    val lockerId: String?,
    val address: String?,
    val status: String?,
    val expireAt: String?
)
data class CreateOrderRequest(
    val customerName: String,
    val phone: String,
    val packageType: String,
    val userId: String,
    val lockerId: String,
    val address: String,
    val duration: Int,
    val rentType: String
)

data class CreateOrderResponse(
    val paymentCode: String,
    val lockerCode: String,
    val paymentStatus: String
)

data class PaymentStatusResponse(
    val status: String
)

data class RegisterRequest(
    val username: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val userId: String?,
    val username: String?
)
data class User(
    val id: String?,
    val username: String,
    val email: String?,
    val phone: String?
)
data class RegisterResponse(
    val success: Boolean,
    val message: String
)

data class LocationRequest(
    val lat: Double,
    val lng: Double
)

data class ExtendOrderRequest(
    val duration: Int,
    val rentType: String
)