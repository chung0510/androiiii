package com.example.timhieu

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.R.id.btnExtendRent
import com.example.timhieu.network.Order
import com.example.timhieu.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        val lockerId = intent.getStringExtra("LOCKER_ID") ?: ""

        findViewById<TextView>(R.id.tvDetailLockerId).text = "Mã tủ: $lockerId"
        tvRemainingTime = findViewById(R.id.tvRemainingTime)
        ivQRCode = findViewById(R.id.ivQRCode)

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
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
                    intent.putExtra("LOCKER_ADDRESS", order.lockerAddress)
                    intent.putExtra("SLOT_NUMBER", order.slotNumber)

                    startActivity(intent)
                }
            }
        findViewById<MaterialButton>(R.id.btnFinishRent)
            .setOnClickListener {
                val order = currentOrder ?: return@setOnClickListener
                MaterialAlertDialogBuilder(this)
                    .setTitle("Kết thúc thuê")
                    .setMessage(
                        "Bạn có chắc muốn kết thúc thuê tủ này không?"
                    )
                    .setPositiveButton("Đồng ý") { _, _ ->
                        RetrofitClient.api
                            .finishOrder(order.id ?: "")
                            .enqueue(
                                object : Callback<Order> {
                                    override fun onResponse(
                                        call: Call<Order>,
                                        response: Response<Order>
                                    ) {
                                        if(response.isSuccessful)
                                        {
                                            Toast.makeText(
                                                this@OrderDetailActivity,
                                                "Đã trả tủ thành công",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            val intent =
                                                Intent(
                                                    this@OrderDetailActivity,
                                                    UserActivity::class.java
                                                )
                                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            startActivity(intent)
                                            finish()
                                        }
                                        else
                                        {
                                            Toast.makeText(
                                                this@OrderDetailActivity,
                                                "Không thể trả tủ",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    override fun onFailure(
                                        call: Call<Order>,
                                        t: Throwable
                                    ) {
                                        Toast.makeText(
                                            this@OrderDetailActivity,
                                            t.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        fetchOrderDetails(orderId)
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

    private fun fetchOrderDetails(orderId: String) {
        RetrofitClient.api
            .getOrder(orderId)
            .enqueue(object : Callback<Order> {
                override fun onResponse(
                    call: Call<Order>,
                    response: Response<Order>
                ) {
                    if (
                        response.isSuccessful &&
                        response.body() != null
                    ) {
                        val order = response.body()!!
                        currentOrder = order
                        findViewById<TextView>(
                            R.id.tvDetailLockerId
                        ).text =
                            "Mã tủ: ${order.lockerId}"
                        findViewById<TextView>(
                            R.id.tvDetailAddress
                        ).text =
                            "Địa chỉ: ${order.lockerAddress}"
                        findViewById<TextView>(R.id.tvSlotNumber
                        ).text =
                            "Ngăn: ${order.slotNumber}"
                        if(order.status == "COMPLETED"){
                            val btnExtendRent = findViewById<MaterialButton>(R.id.btnExtendRent)
                            val btnGetCode = findViewById<MaterialButton>(R.id.btnGetCode)
                            val btnFinishRent = findViewById<MaterialButton>(R.id.btnFinishRent)
                            btnExtendRent.isEnabled = false
                            btnGetCode.isEnabled = false
                            btnFinishRent.isEnabled = false
                        }
                        if(order.rentType == "ONCE" || order.rentType == "ONE_TIME"
                        ){
                            findViewById<MaterialButton>(R.id.btnExtendRent).visibility = View.GONE
                            tvRemainingTime.text = "Thuê theo lượt"
                        } else {
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                                expireTime = sdf.parse(order.expireAt?.substring(0,19))
                                handler.post(updateTimeRunnable)
                            } catch (e: Exception){
                                tvRemainingTime.text = "Lỗi định dạng thời gian"
                            }
                        }
                    } else {
                        tvRemainingTime.text = "Không tìm thấy đơn hàng"
                    }
                }

                override fun onFailure(
                    call: Call<Order>,
                    t: Throwable
                ) {

                    tvRemainingTime.text =
                        "Lỗi kết nối: ${t.message}"
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
        val orderId = intent.getStringExtra("ORDER_ID") ?: ""
        fetchOrderDetails(orderId)
    }
}
