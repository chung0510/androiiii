package com.example.timhieu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val role = getSharedPreferences(
            "USER_DATA",
            MODE_PRIVATE
        ).getString("ROLE", "USER")

        if(role != "ADMIN"){

            finish()

            startActivity(
                Intent(
                    this,
                    UserActivity::class.java
                )
            )

            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        findViewById<Button>(R.id.btnAdminLogout)
            .setOnClickListener {

                getSharedPreferences(
                    "USER_DATA",
                    MODE_PRIVATE
                )
                    .edit()
                    .clear()
                    .apply()

                val intent =
                    Intent(this, MainActivity::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish()
            }

        findViewById<Button>(R.id.btnManageLockers).setOnClickListener {
            // Handle manage lockers navigation
        }

        findViewById<Button>(R.id.btnViewOrders).setOnClickListener {
            // Handle view orders navigation
        }
    }
}