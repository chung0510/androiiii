package com.example.timhieu

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.network.PaymentStatusResponse
import com.example.timhieu.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.timhieu.network.User

class PaymentActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    private var paymentCode: String? = null
    private var isPaid = false

    private val checkStatusRunnable = object : Runnable {
        override fun run() {
            if (!isPaid) {
                checkPaymentStatus()
                handler.postDelayed(this, 3000) // Kiểm tra mỗi 3 giây
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Nhận PAYMENT_CODE hoặc ORDER_ID tùy theo luồng RentLocker truyền sang
        paymentCode = intent.getStringExtra("PAYMENT_CODE") ?: intent.getStringExtra("ORDER_ID")
        val tvPaymentCode = findViewById<TextView>(R.id.tvPaymentCode)
        tvPaymentCode.text = "Nội dung CK: $paymentCode\n\n" + "Vui lòng nhập chính xác nội dung này khi chuyển khoản."
        val totalPrice = intent.getIntExtra("TOTAL_PRICE", 0)

        val imgQr = findViewById<ImageView>(R.id.imgQr)

        val paymentCodeValue = paymentCode ?: ""

        val qrUrl =
            "https://img.vietqr.io/image/" +
            "MB-060205102004-compact2.png" +
            "?amount=$totalPrice" +
            "&addInfo=$paymentCodeValue" +
            "&accountName=SMARTBOX"

        Glide.with(this)
            .load(qrUrl)
            .into(imgQr)

        findViewById<ImageButton>(R.id.btnBackPayment).setOnClickListener {
            finish()
        }

        val tvAmount = findViewById<TextView>(R.id.tvPaymentAmount)
        val formattedPrice = String.format(Locale.US, "%,d", totalPrice)
        tvAmount.text = "Số tiền: " + formattedPrice + "đ"

        val btnDone = findViewById<Button>(R.id.btnDonePayment)
        btnDone.isEnabled = false
        btnDone.text = "ĐANG CHỜ THANH TOÁN..."
        val isExtension =
            intent.getBooleanExtra(
                "IS_EXTENSION",
                false
            )

        // Bắt đầu kiểm tra trạng thái tự động
        if (paymentCode != null) {
            handler.post(checkStatusRunnable)
        }
    }

    private fun navigateToOrderDetail() {
        val isExtension = intent.getBooleanExtra("IS_EXTENSION", false)
        if(isExtension){
            val intent = Intent(this, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            return
        }
        val intent =
            Intent(
                this,
                OrderDetailActivity::class.java
            )
        intent.putExtra(
            "LOCKER_ID",
            getIntent().getStringExtra(
                "LOCKER_ID"
            )
        )
        intent.putExtra(
            "LOCKER_ADDRESS",
            getIntent().getStringExtra(
                "LOCKER_ADDRESS"
            )
        )
        intent.putExtra(
            "PAYMENT_CODE",
            paymentCode
        )
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun checkPaymentStatus() {
        paymentCode?.let { id ->
            RetrofitClient.api.checkPayment(id).enqueue(object : Callback<PaymentStatusResponse> {
                override fun onResponse(call: Call<PaymentStatusResponse>, response: Response<PaymentStatusResponse>) {
                    if (response.isSuccessful) {
                        val status = response.body()?.status
                        if (status == "paid" || status == "PAID") {
                            isPaid = true
                            handler.removeCallbacks(checkStatusRunnable)
                            Toast.makeText(this@PaymentActivity, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                            navigateToOrderDetail()
                        }
                    }
                }
                override fun onFailure(call: Call<PaymentStatusResponse>, t: Throwable) {
                    // Tiếp tục thử lại
                }
            })
        }
    }
}
