package com.example.timhieu

import android.content.Intent
import android.os.Bundle
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timhieu.network.LoginRequest
import com.example.timhieu.network.LoginResponse
import com.example.timhieu.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtUsername = findViewById<TextInputEditText>(R.id.edtUsername)
        val edtPassword = findViewById<TextInputEditText>(R.id.edtPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        findViewById<TextView>(R.id.tvGoToRegister)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        val request = LoginRequest(username, password)
        RetrofitClient.api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginResponse = response.body()!!
                    
                    // Lưu userId vào SharedPreferences
                    val sharedPref = getSharedPreferences("USER_DATA", MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("USER_ID", loginResponse.userId)
                        putString("ROLE", loginResponse.role)
                        apply()
                    }

                    Toast.makeText(this@MainActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()

                    if(loginResponse.role == "ADMIN") {

                        startActivity(
                            Intent(
                                this@MainActivity,
                                AdminActivity::class.java
                            )
                        )
                    } else {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                UserActivity::class.java
                            )
                        )
                    }
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, getString(R.string.connection_error, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
