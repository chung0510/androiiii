package com.example.timhieu

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.R.id.btnExtendRent
import com.example.timhieu.network.Order
import com.example.timhieu.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class OrderDetailActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var expireTime: Date? = null
    private var currentOrder: Order? = null
    private lateinit var tvRemainingTime: TextView
    private lateinit var ivQRCode: ImageView

    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateRemainingTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        val lockerId = intent.getStringExtra("LOCKER_ID") ?: ""
        val address = intent.getStringExtra("LOCKER_ADDRESS") ?: ""

        findViewById<TextView>(R.id.tvDetailLockerId).text = "Mã tủ: $lockerId"
        findViewById<TextView>(R.id.tvDetailAddress).text = "Địa chỉ: $address"
        tvRemainingTime = findViewById(R.id.tvRemainingTime)
        ivQRCode = findViewById(R.id.ivQRCode)

        findViewById<ImageButton>(R.id.btnBackDetail).setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        val tvUnlockCode = findViewById<TextView>(R.id.tvUnlockCode)
        findViewById<MaterialButton>(R.id.btnGetCode).setOnClickListener {
            val code = (100000..999999).random()
            tvUnlockCode.text = code.toString()
            generateQRCode("LOCKER_UNLOCK_$lockerId$code")
            Toast.makeText(this, "Mã mới đã được tạo", Toast.LENGTH_SHORT).show()
        }

        findViewById<MaterialButton>(btnExtendRent)
            .setOnClickListener {
                currentOrder?.let { order ->

                    val intent = Intent(this, RentLockerActivity::class.java)
                    intent.putExtra("IS_EXTENSION", true)
                    intent.putExtra("ORDER_ID", order.id)
                    intent.putExtra("CUSTOMER_NAME", order.customerName)
                    intent.putExtra("PHONE", order.phone)
                    intent.putExtra("LOCKER_ID", order.lockerId)
                    intent.putExtra("LOCKER_ADDRESS", order.address)

                    startActivity(intent)
                }
            }

        fetchOrderDetails(lockerId)
    }

    private fun generateQRCode(content: String) {
        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Sử dụng bitMatrix.get(x, y) và so sánh rõ ràng để tránh lỗi Type Mismatch
                    val isBlack = bitMatrix.get(x, y)
                    bitmap.setPixel(x, y, if (isBlack) Color.BLACK else Color.WHITE)
                }
            }
            ivQRCode.setImageBitmap(bitmap)
            ivQRCode.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Không thể tạo mã QR", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchOrderDetails(lockerId: String) {
        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""

        RetrofitClient.api.getUserOrders(userId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    val activeOrder = orders.find { it.lockerId == lockerId && it.status.equals("ACTIVE", ignoreCase = true) }
                    activeOrder?.let {
                        currentOrder = it
                        try {
                            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                            sdf.timeZone = TimeZone.getDefault()
                            android.util.Log.d(
                                "TIME_DEBUG",
                                "expireAt raw = ${it.expireAt}"
                            )

                            expireTime = sdf.parse(
                                it.expireAt?.substring(0, 19)
                            )

                            android.util.Log.d(
                                "TIME_DEBUG",
                                "expireTime parsed = $expireTime"
                            )
                            handler.post(updateTimeRunnable)
                        } catch (e: Exception) {
                            tvRemainingTime.text = "Lỗi định dạng thời gian"
                        }
                    } ?: run {
                        tvRemainingTime.text = "Không tìm thấy đơn hàng"
                    }
                } else {
                    tvRemainingTime.text = "Lỗi Server: ${response.code()}"
                }
            }
            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                tvRemainingTime.text = "Lỗi kết nối: ${t.message}"
            }
        })
    }

    private fun updateRemainingTime() {
        expireTime?.let {
            val now = Date()
            val diff = it.time - now.time
            android.util.Log.d(
                "TIME_DEBUG",
                "now = ${Date()}"
            )

            android.util.Log.d(
                "TIME_DEBUG",
                "expire = $expireTime"
            )
            if (diff > 0) {
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                val seconds = (diff / 1000) % 60
                tvRemainingTime.text = String.format(Locale.US, "Thời gian còn lại: %02d:%02d:%02d", hours, minutes, seconds)
            } else {
                tvRemainingTime.text = "Đã hết hạn"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onResume() {
        super.onResume()
        val lockerId = intent.getStringExtra("LOCKER_ID") ?: ""
        fetchOrderDetails(lockerId)
    }
}
