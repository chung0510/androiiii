package com.example.timhieu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.network.RegisterRequest
import com.example.timhieu.network.RegisterResponse
import com.example.timhieu.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val edtUsername = findViewById<EditText>(R.id.edtRegisterUsername)
        val edtPhone = findViewById<EditText>(R.id.edtRegisterPhone)
        val edtPassword = findViewById<EditText>(R.id.edtRegisterPassword)
        val edtConfirmPassword = findViewById<EditText>(R.id.edtRegisterConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {

            val username =
                edtUsername.text.toString().trim()

            val phone =
                edtPhone.text.toString().trim()

            val password =
                edtPassword.text.toString().trim()

            val confirmPassword =
                edtConfirmPassword.text.toString().trim()

            if (username.isEmpty()) {
                edtUsername.error = "Nhập tên đăng nhập"
                return@setOnClickListener
            }

            if (username.length < 4) {
                edtUsername.error = "Tối thiểu 4 ký tự"
                return@setOnClickListener
            }

            if (!username.matches(
                    Regex("^[a-zA-Z0-9_]{4,20}$")
                )
            ) {
                edtUsername.error =
                    "Chỉ cho phép chữ, số và dấu _"

                return@setOnClickListener
            }

            if (phone.isEmpty()) {
                edtPhone.error = "Nhập số điện thoại"
                return@setOnClickListener
            }

            if(!phone.matches(Regex("^0\\d{9}$")))
            {
                edtPhone.error =
                    "Số điện thoại không hợp lệ"

                return@setOnClickListener
            }

            if (password.length < 8) {

                edtPassword.error =
                    "Mật khẩu tối thiểu 8 ký tự"

                return@setOnClickListener
            }

            if (!password.matches(
                    Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")
                )
            ) {

                edtPassword.error =
                    "Phải có chữ hoa, chữ thường và số"

                return@setOnClickListener
            }

            if (password != confirmPassword) {

                edtConfirmPassword.error =
                    "Mật khẩu không khớp"

                return@setOnClickListener
            }

            registerUser(username, phone, password)
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }

    }

    private fun registerUser(username: String, phone: String, password: String) {
        val request = RegisterRequest(username, password, phone)

        RetrofitClient.api.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()!!
                    Toast.makeText(this@RegisterActivity, registerResponse.message, Toast.LENGTH_SHORT).show()
                    if (registerResponse.success) {
                        finish()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Lỗi: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
