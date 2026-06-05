package com.example.timhieu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.FrameLayout

class RentLockerActivity : AppCompatActivity() {

    private var totalPrice = 0
    private var isRentByHour = true
    private var selectedDuration = 0
    private var currentLockerId = ""
    private var currentLockerAddress = ""
    private var isExtension = false
    private var pricePerHour = 30000
    private var pricePerDay = 150000
    private var pricePerMonth = 500000
    private var isRentByMonth = false

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_locker)

        currentLockerId = intent.getStringExtra("LOCKER_ID") ?: "L001"
        currentLockerAddress = intent.getStringExtra("LOCKER_ADDRESS") ?: ""
        isExtension = intent.getBooleanExtra("IS_EXTENSION", false)

        val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)
        val edtRenterName = findViewById<EditText>(R.id.edtRenterName)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmRent)

        val orderId = intent.getStringExtra("ORDER_ID")
        val customerName = intent.getStringExtra("CUSTOMER_NAME")
        val phone = intent.getStringExtra("PHONE")

        if (isExtension) {
            btnConfirm.text = "XÁC NHẬN GIA HẠN"
            edtRenterName.setText(customerName)
            edtPhone.setText(phone)
            edtRenterName.isEnabled = false
            edtPhone.isEnabled = false
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val spinnerSlot = findViewById<Spinner>(R.id.spinnerSlot)
        val availableSlots =
            intent.getIntExtra(
                "AVAILABLE_SLOTS",
                0
            )
        val prefix = currentLockerId
        val slotList = mutableListOf<String>()
        for(i in 1..availableSlots)
        {
            slotList.add(prefix + i)
        }
        val slotAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            slotList
        )
        val types = arrayOf("trong chung cư", "ngoài chung cư")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, types)
        spinnerType.adapter = adapter
        spinnerSlot.adapter = slotAdapter


        val optionHour =
            findViewById<FrameLayout>(R.id.optionHour)

        val optionDay =
            findViewById<FrameLayout>(R.id.optionDay)
        val optionMonth =
            findViewById<FrameLayout>(R.id.optionMonth)

        val gridHours = findViewById<GridLayout>(R.id.gridHours)
        val gridDays = findViewById<GridLayout>(R.id.gridDays)
        val gridMonths = findViewById<GridLayout>(R.id.gridMonths)

        optionHour.setOnClickListener {
            isRentByHour = true
            isRentByMonth = false
            gridHours.visibility = View.VISIBLE
            gridDays.visibility = View.GONE
            gridMonths.visibility = View.GONE
            updatePrice(0, tvTotalPrice)
        }

        optionDay.setOnClickListener {
            isRentByHour = false
            isRentByMonth = false
            gridHours.visibility = View.GONE
            gridDays.visibility = View.VISIBLE
            gridMonths.visibility = View.GONE
            updatePrice(0, tvTotalPrice)
        }

        optionMonth.setOnClickListener {
            isRentByHour = false
            isRentByMonth = true
            gridHours.visibility = View.GONE
            gridDays.visibility = View.GONE
            gridMonths.visibility = View.VISIBLE
            updatePrice(0, tvTotalPrice)
        }

        // Setup hour selection
        for (i in 0 until gridHours.childCount) {
            val child = gridHours.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener {
                    selectedDuration = child.text.toString().replace("h", "").toInt()
                    totalPrice = selectedDuration * pricePerHour
                    updatePrice(totalPrice, tvTotalPrice)
                }
            }
        }

        // Setup day selection
        for (i in 0 until gridDays.childCount) {
            val child = gridDays.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener {
                    selectedDuration = child.text.toString().replace(" ngày", "").toInt()
                    totalPrice = selectedDuration * pricePerDay
                    updatePrice(totalPrice, tvTotalPrice)
                }
            }
        }

        // Setup month selection
        for (i in 0 until gridMonths.childCount) {
            val child = gridMonths.getChildAt(i)
            if (child is Button) {
                child.setOnClickListener {
                    selectedDuration = child.text.toString().replace(" tháng", "").toInt()
                    totalPrice = selectedDuration * pricePerMonth
                    updatePrice(totalPrice, tvTotalPrice)
                }
            }
        }

        btnConfirm.setOnClickListener {

            val name = edtRenterName.text.toString()
            val phone = edtPhone.text.toString()

            if (name.isEmpty() || phone.isEmpty()) {

                Toast.makeText(
                    this,
                    "Nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val sharedPref =
                getSharedPreferences(
                    "USER_DATA",
                    MODE_PRIVATE
                )

            val userId =
                sharedPref.getString(
                    "USER_ID",
                    ""
                ) ?: ""

            val rentType = when {
                isRentByMonth -> "MONTH"
                isRentByHour -> "HOUR"
                else -> "DAY"
            }
            val request =
                CreateOrderRequest(
                    customerName = name,
                    phone = phone,
                    packageType = "SMALL",
                    userId = userId,
                    lockerId = currentLockerId,
                    address = currentLockerAddress,
                    duration = selectedDuration,
                    rentType = rentType
                )

            // ===== GIA HẠN =====

            if (isExtension) {
                RetrofitClient.api.createExtendOrder(
                    orderId ?: "",
                    ExtendOrderRequest(
                        duration = selectedDuration,
                        rentType = rentType
                    )
                ).enqueue(
                    object :
                        Callback<ExtendOrderResponse> {
                        override fun onResponse(
                            call: Call<ExtendOrderResponse>,
                            response: Response<ExtendOrderResponse>
                        ) {
                            if(response.isSuccessful){
                                val extend = response.body()
                                val intent = Intent(this@RentLockerActivity, PaymentActivity::class.java)
                                intent.putExtra("PAYMENT_CODE", extend?.paymentCode)
                                intent.putExtra("TOTAL_PRICE", extend?.amount ?: 0)
                                intent.putExtra("IS_EXTENSION", true)
                                intent.putExtra("ORDER_ID", orderId)
                                startActivity(intent)
                            }
                        }
                        override fun onFailure(
                            call: Call<ExtendOrderResponse>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                this@RentLockerActivity,
                                t.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )

                return@setOnClickListener
            }

            // ===== THUÊ MỚI =====

            RetrofitClient.api.createOrder(request)

                .enqueue(

                    object :
                        Callback<CreateOrderResponse> {

                        override fun onResponse(
                            call: Call<CreateOrderResponse>,
                            response: Response<CreateOrderResponse>
                        ) {

                            if (response.isSuccessful) {

                                val order =
                                    response.body()

                                val intent =
                                    Intent(
                                        this@RentLockerActivity,
                                        PaymentActivity::class.java
                                    )

                                intent.putExtra(
                                    "PAYMENT_CODE",
                                    order?.paymentCode
                                )

                                intent.putExtra(
                                    "LOCKER_ID",
                                    currentLockerId
                                )

                                intent.putExtra(
                                    "LOCKER_ADDRESS",
                                    currentLockerAddress
                                )

                                intent.putExtra(
                                    "LOCKER_CODE",
                                    order?.lockerCode
                                )

                                intent.putExtra(
                                    "TOTAL_PRICE",
                                    totalPrice
                                )

                                startActivity(intent)
                            }
                        }

                        override fun onFailure(
                            call: Call<CreateOrderResponse>,
                            t: Throwable
                        ) {

                            Toast.makeText(
                                this@RentLockerActivity,
                                t.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
        }
    }

    private fun updatePrice(price: Int, tv: TextView) {
        if (price > 0) {
            tv.visibility = View.VISIBLE
            tv.text = "Tổng tiền: $price"
        } else {
            tv.visibility = View.GONE
        }
    }
}
