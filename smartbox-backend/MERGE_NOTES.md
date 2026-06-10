# Unified backend notes

This backend merges the two uploaded backends into one Spring Boot project.

Compatible endpoints kept for both apps:
- `/api/register`, `/api/login`, `/api/user/{id}`
- `/generate-lockers`, `/lockers`
- `/api/create-order`
- `/api/check-locker`
- `/api/check-payment/{paymentCode}`
- `/api/fake-payment/{paymentCode}`
- `/api/webhook/payment`
- `/api/sepay-webhook`
- `/api/orders/{userId}`
- `/api/rented-lockers/{userId}`
- `/api/free-slots/{lockerId}`
- `/api/create-extend/{id}`
- `/api/finish-order/{orderId}`
- `/api/cancel-order/{id}`
- `/api/available-slots`
- `/api/active-slots`
- `/api/return-locker/{lockerCode}`

The unified `Order` model contains both the legacy SmartBox fields and the newer baitap fields.
