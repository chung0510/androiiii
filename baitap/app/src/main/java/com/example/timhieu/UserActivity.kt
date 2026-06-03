package com.example.timhieu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.network.Locker
import com.example.timhieu.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.timhieu.network.Order

class UserActivity : AppCompatActivity() {

    private lateinit var lvRentedLockers: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        lvRentedLockers = findViewById(R.id.lvRentedLockers)

        lvRentedLockers.setOnItemClickListener { _, _, position, _ ->
            val order = lvRentedLockers.adapter
                    .getItem(position)
                        as? Order
            order?.let {
                val intent =
                    Intent(
                        this,
                        OrderDetailActivity::class.java
                    )
                intent.putExtra(
                    "LOCKER_ID",
                    it.lockerId
                )
                intent.putExtra(
                    "LOCKER_ADDRESS",
                    it.address
                )
                intent.putExtra(
                    "LOCKER_CODE",
                    it.lockerCode
                )
                intent.putExtra(
                    "PAYMENT_CODE",
                    it.paymentCode
                )
                startActivity(intent)
            }
        }
        
        fetchRentedLockers()

        findViewById<Button>(R.id.btnFindLocker).setOnClickListener {
            startActivity(Intent(this, SearchLockerActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun fetchRentedLockers() {
        val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", "") ?: ""
        
        if (userId.isEmpty()) return

        RetrofitClient.api.getRentedLockers(userId).enqueue(object : Callback<List<Order>> {
            override fun onResponse(call: Call<List<Order>>, response: Response<List<Order>>) {
                if (response.isSuccessful) {
                    val orders = response.body() ?: emptyList()
                    val adapter = OrderAdapter(this@UserActivity, orders)
                    lvRentedLockers.adapter = adapter
                }
            }

            override fun onFailure(call: Call<List<Order>>, t: Throwable) {
                Toast.makeText(this@UserActivity, "Lỗi kết nối server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchRentedLockers()
    }
}