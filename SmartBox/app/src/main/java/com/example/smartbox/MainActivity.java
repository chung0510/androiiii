package com.example.smartbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnNhapMa, btnThueTu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnNhapMa = findViewById(R.id.btnNhapMa);
        btnThueTu = findViewById(R.id.btnThueTu);

        btnNhapMa.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            NhapMaTuActivity.class));

            overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
        });

        btnThueTu.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            MainActivity.this,
                            ThueTuActivity.class));

            overridePendingTransition(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right);
        });
    }
}