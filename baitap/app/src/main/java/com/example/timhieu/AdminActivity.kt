package com.example.timhieu

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment

class AdminActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var txtTitle: TextView

    private lateinit var menuDashboard: LinearLayout
    private lateinit var menuNotification: LinearLayout
    private lateinit var menuLocker: LinearLayout
    private lateinit var menuOrders: LinearLayout
    private lateinit var menuAccount: LinearLayout
    private lateinit var menuPrice: LinearLayout
    private lateinit var menuReport: LinearLayout
    private lateinit var menuLogout: LinearLayout

    private lateinit var layoutLockerSub: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {

        val role = getSharedPreferences(
            "USER_DATA",
            MODE_PRIVATE
        ).getString("ROLE", "USER")

        if (role != "ADMIN") {
            startActivity(
                Intent(
                    this,
                    UserActivity::class.java
                )
            )
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        initViews()

        setupDefaultScreen()

        setupDrawer()

        setupMenuEvents()
    }

    private fun initViews() {

        drawerLayout =
            findViewById(R.id.drawerLayout)

        val topBar =
            findViewById<View>(R.id.layoutTopBar)

        txtTitle =
            topBar.findViewById(R.id.txtTitle)

        menuDashboard =
            findViewById(R.id.menuDashboard)

        menuNotification =
            findViewById(R.id.menuNotification)

        menuLocker =
            findViewById(R.id.menuLocker)

        menuOrders =
            findViewById(R.id.menuOrders)

        menuAccount =
            findViewById(R.id.menuAccount)

        menuPrice =
            findViewById(R.id.menuPrice)

        menuReport =
            findViewById(R.id.menuReport)

        menuLogout =
            findViewById(R.id.menuLogout)

        layoutLockerSub =
            findViewById(R.id.layoutLockerSub)
    }

    private fun setupDefaultScreen() {

        replaceFragment(
            DashboardFragment()
        )

        txtTitle.text = "Dashboard"

        menuDashboard.setBackgroundResource(
            R.drawable.bg_menu_active
        )
    }

    private fun setupDrawer() {

        val btnMenu =
            findViewById<ImageButton>(R.id.btnMenu)

        val btnCloseDrawer =
            findViewById<ImageButton>(R.id.btnCloseDrawer)

        btnMenu.setOnClickListener {

            drawerLayout.openDrawer(
                GravityCompat.START
            )
        }

        btnCloseDrawer.setOnClickListener {

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }
    }

    private fun setupMenuEvents() {

        val menuLockerList =
            findViewById<View>(R.id.menuLockerList)

        val menuAddLocker =
            findViewById<View>(R.id.menuAddLocker)

        // DASHBOARD

        menuDashboard.setOnClickListener {

            resetMenu()

            menuDashboard.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            replaceFragment(
                DashboardFragment()
            )

            txtTitle.text = "Dashboard"

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // THÔNG BÁO

        menuNotification.setOnClickListener {

            resetMenu()

            menuNotification.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            txtTitle.text = "Thông báo"

            showDevelopingMessage()

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // QUẢN LÝ TỦ

        menuLocker.setOnClickListener {

            resetMenu()

            menuLocker.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            layoutLockerSub.isVisible =
                !layoutLockerSub.isVisible
        }

        // DANH SÁCH TỦ
        menuLockerList.setOnClickListener {
            resetMenu()
            menuLocker.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            replaceFragment(
                LockerListFragment()
            )

            txtTitle.text = "Danh sách tủ"
            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // THÊM TỦ

        menuAddLocker.setOnClickListener {

            resetMenu()

            menuLocker.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            txtTitle.text = "Thêm tủ mới"

            Toast.makeText(
                this,
                "AddLockerFragment chưa được tạo",
                Toast.LENGTH_SHORT
            ).show()

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // ĐƠN THUÊ

        menuOrders.setOnClickListener {

            resetMenu()

            menuOrders.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            txtTitle.text = "Đơn thuê"

            showDevelopingMessage()

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // QUẢN LÝ TÀI KHOẢN

        menuAccount.setOnClickListener {

            resetMenu()

            menuAccount.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            txtTitle.text = "Quản lý tài khoản"

            showDevelopingMessage()

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // THIẾT LẬP GIÁ

        menuPrice.setOnClickListener {

            resetMenu()

            menuPrice.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            txtTitle.text = "Thiết lập giá"

            showDevelopingMessage()

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // BÁO CÁO

        menuReport.setOnClickListener {

            resetMenu()

            menuReport.setBackgroundResource(
                R.drawable.bg_menu_active
            )

            txtTitle.text = "Báo cáo"

            showDevelopingMessage()

            drawerLayout.closeDrawer(
                GravityCompat.START
            )
        }

        // ĐĂNG XUẤT

        menuLogout.setOnClickListener {

            getSharedPreferences(
                "USER_DATA",
                MODE_PRIVATE
            )
                .edit()
                .clear()
                .apply()

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)

            finish()
        }
    }

    private fun replaceFragment(
        fragment: Fragment
    ) {

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragmentContainer,
                fragment
            )
            .commit()
    }

    private fun resetMenu() {

        menuDashboard.setBackgroundResource(0)

        menuNotification.setBackgroundResource(0)

        menuLocker.setBackgroundResource(0)

        menuOrders.setBackgroundResource(0)

        menuAccount.setBackgroundResource(0)

        menuPrice.setBackgroundResource(0)

        menuReport.setBackgroundResource(0)
    }

    private fun showDevelopingMessage() {

        Toast.makeText(
            this,
            "Chức năng đang phát triển",
            Toast.LENGTH_SHORT
        ).show()
    }
}