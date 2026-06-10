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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RentLockerActivity : AppCompatActivity() {

    private var totalPrice = 0
    private var isRentByHour = true
    private var selectedDuration = 0
    private var currentLockerId = ""
    private var currentLockerAddress = ""
    private var isExtension = false
    private var pricePerHour = 30000
    private var pricePerDay = 150000
    private var pricePerMonth = 1000000
    private var priceOnce = 15000
    private var isRentByMonth = false
    private var isRentOnce = false
    private lateinit var edtRenterName: TextInputEditText
    private lateinit var edtPhone: TextInputEditText

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rent_locker)

        currentLockerId = intent.getStringExtra("LOCKER_ID") ?: "L001"
        currentLockerAddress = intent.getStringExtra("LOCKER_ADDRESS") ?: ""
        isExtension = intent.getBooleanExtra("IS_EXTENSION", false)
        val currentSlot = intent.getIntExtra("SLOT_NUMBER", 0)

        val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)
        edtRenterName = findViewById(R.id.edtRenterName)
        edtPhone = findViewById(R.id.edtPhone)
        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "")
        val phoneNumber = sharedPref.getString("PHONE", "")

        edtRenterName.setText(username)
        edtPhone.setText(phoneNumber)
        edtRenterName.keyListener = null
        edtPhone.keyListener = null

        val btnConfirm = findViewById<Button>(R.id.btnConfirmRent)


        val orderId = intent.getStringExtra("ORDER_ID")
        val customerName = intent.getStringExtra("CUSTOMER_NAME")
        val phone = intent.getStringExtra("PHONE")

        val spinnerSlot = findViewById<AutoCompleteTextView>(R.id.spinnerSlot)
        val tvSlotTitle = findViewById<TextView>(R.id.tvSlotTitle)
        val slotLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutSlot)
        val tvLockerInfo = findViewById<TextInputEditText>(R.id.tvLockerInfo)

        val optionOnce = findViewById<com.google.android.material.card.MaterialCardView>(R.id.optionOnce)
        val optionHour = findViewById<com.google.android.material.card.MaterialCardView>(R.id.optionHour)
        val optionDay = findViewById<com.google.android.material.card.MaterialCardView>(R.id.optionDay)
        val optionMonth = findViewById<com.google.android.material.card.MaterialCardView>(R.id.optionMonth)

        val gridHours = findViewById<GridLayout>(R.id.gridHours)
        val gridDays = findViewById<GridLayout>(R.id.gridDays)
        val gridMonths = findViewById<GridLayout>(R.id.gridMonths)
        tvLockerInfo.setText("$currentLockerId - $currentLockerAddress")

        if(!isExtension)
        {
            loadFreeSlots(spinnerSlot)
        }


        if (isExtension) {
            val tvCurrentSlot = findViewById<TextView>(R.id.tvCurrentSlot)
            tvCurrentSlot.visibility = View.VISIBLE
            tvCurrentSlot.text = "Ngăn đang thuê: Ngăn $currentSlot"
            slotLayout.visibility = View.GONE
            tvSlotTitle.visibility = View.GONE
            findViewById<TextInputLayout>(R.id.layoutSlot).visibility = View.GONE
            btnConfirm.text = "XÁC NHẬN GIA HẠN"
            edtRenterName.setText(customerName)
            edtPhone.setText(phone)
            edtRenterName.keyListener = null
            edtPhone.keyListener = null
            optionOnce.isEnabled = false
            optionOnce.isClickable = false
            optionOnce.alpha = 0.4f
        }

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbarRent)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        fun deselectButtons(grid: GridLayout) {
            for (i in 0 until grid.childCount) {
                val child = grid.getChildAt(i)
                if (child is com.google.android.material.button.MaterialButton) {
                    child.setBackgroundColor(getColor(android.R.color.transparent))
                    child.setTextColor(getColor(R.color.primary))
                    child.strokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
                }
            }
        }

        fun selectButton(button: com.google.android.material.button.MaterialButton) {
            button.setBackgroundColor(getColor(R.color.primary))
            button.setTextColor(getColor(R.color.white))
            button.strokeColor = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
        }

        fun deselectAll() {
            optionOnce.isChecked = false
            optionHour.isChecked = false
            optionDay.isChecked = false
            optionMonth.isChecked = false
            gridHours.visibility = View.GONE
            gridDays.visibility = View.GONE
            gridMonths.visibility = View.GONE
            deselectButtons(gridHours)
            deselectButtons(gridDays)
            deselectButtons(gridMonths)
            selectedDuration = 0
            totalPrice = 0
            updatePrice(0, tvTotalPrice)
        }

        optionOnce.setOnClickListener {
            deselectAll()
            optionOnce.isChecked = true
            isRentOnce = true
            isRentByHour = false
            isRentByMonth = false
            selectedDuration = 1
            totalPrice = priceOnce
            updatePrice(totalPrice, tvTotalPrice)
        }

        optionHour.setOnClickListener {
            deselectAll()
            optionHour.isChecked = true
            isRentOnce = false
            isRentByHour = true
            isRentByMonth = false
            gridHours.visibility = View.VISIBLE
            updatePrice(0, tvTotalPrice)
        }

        optionDay.setOnClickListener {
            deselectAll()
            optionDay.isChecked = true
            isRentOnce = false
            isRentByHour = false
            isRentByMonth = false
            gridDays.visibility = View.VISIBLE
            updatePrice(0, tvTotalPrice)
        }

        optionMonth.setOnClickListener {
            deselectAll()
            optionMonth.isChecked = true
            isRentOnce = false
            isRentByHour = false
            isRentByMonth = true
            gridMonths.visibility = View.VISIBLE
            updatePrice(0, tvTotalPrice)
        }

        // Setup hour selection
        for (i in 0 until gridHours.childCount) {
            val child = gridHours.getChildAt(i)
            if (child is com.google.android.material.button.MaterialButton) {
                child.setOnClickListener {
                    deselectButtons(gridHours)
                    selectButton(child)
                    selectedDuration = child.text.toString().replace("h", "").toInt()
                    totalPrice = selectedDuration * pricePerHour
                    updatePrice(totalPrice, tvTotalPrice)
                }
            }
        }

        // Setup day selection
        for (i in 0 until gridDays.childCount) {
            val child = gridDays.getChildAt(i)
            if (child is com.google.android.material.button.MaterialButton) {
                child.setOnClickListener {
                    deselectButtons(gridDays)
                    selectButton(child)
                    selectedDuration = child.text.toString().replace(" ngày", "").toInt()
                    totalPrice = selectedDuration * pricePerDay
                    updatePrice(totalPrice, tvTotalPrice)
                }
            }
        }

        // Setup month selection
        for (i in 0 until gridMonths.childCount) {
            val child = gridMonths.getChildAt(i)
            if (child is com.google.android.material.button.MaterialButton) {
                child.setOnClickListener {
                    deselectButtons(gridMonths)
                    selectButton(child)
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
                isRentOnce -> "ONCE"
                isRentByMonth -> "MONTH"
                isRentByHour -> "HOUR"
                else -> "DAY"
            }

            var slotNumber = 0
            if(!isExtension)
            {
                val selectedSlotText = spinnerSlot.text.toString()
                slotNumber =
                    selectedSlotText
                        .replace("Ngăn ", "")
                        .toIntOrNull() ?: 0

                if(slotNumber == 0)
                {
                    Toast.makeText(
                        this,
                        "Vui lòng chọn ngăn",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            val request =
                CreateOrderRequest(
                    customerName = name,
                    phone = phone,
                    packageType = "SMALL",
                    userId = userId,
                    lockerId = currentLockerId,
                    lockerAddress = currentLockerAddress,
                    slotNumber = slotNumber,
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
                                intent.putExtra("ORDER_ID", orderId)
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
                                val order = response.body()
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
                                    "ORDER_ID",
                                    order?.id
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
    private fun loadFreeSlots(spinnerSlot: AutoCompleteTextView)
    {
        RetrofitClient.api
            .getFreeSlots(currentLockerId)
            .enqueue(
                object : Callback<List<Int>>
                {
                    override fun onResponse(call: Call<List<Int>>, response: Response<List<Int>>)
                    {
                        if(response.isSuccessful)
                        {
                            val slotList = response.body()?.map { "Ngăn $it" } ?: emptyList()
                            val adapter =
                                ArrayAdapter(
                                    this@RentLockerActivity,
                                    android.R.layout.simple_spinner_dropdown_item,
                                    slotList
                                )
                            spinnerSlot.setAdapter(adapter)
                        }
                    }
                    override fun onFailure(
                        call: Call<List<Int>>,
                        t: Throwable
                    ) {
                    }
                }
            )
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
