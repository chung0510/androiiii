package com.example.smartbox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartbox.api.ApiService;
import com.example.smartbox.api.Order;
import com.example.smartbox.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Spinner;
import android.widget.ArrayAdapter;

import java.util.List;
import java.util.ArrayList;

public class NhapMaTuActivity extends AppCompatActivity {

    EditText edtCode;
    TextView tvError;

    Spinner spinnerSlot;

    StringBuilder code = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhap_ma_tu);

        edtCode = findViewById(R.id.edtCode);
        tvError = findViewById(R.id.tvError);

        spinnerSlot =
                findViewById(
                        R.id.spinnerSlot);

        loadActiveSlots();

        setupButton(R.id.btn1,"1");
        setupButton(R.id.btn2,"2");
        setupButton(R.id.btn3,"3");
        setupButton(R.id.btn4,"4");
        setupButton(R.id.btn5,"5");
        setupButton(R.id.btn6,"6");
        setupButton(R.id.btn7,"7");
        setupButton(R.id.btn8,"8");
        setupButton(R.id.btn9,"9");
        setupButton(R.id.btn0,"0");

        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnMoTu = findViewById(R.id.btnMoTu);

        btnDelete.setOnClickListener(v -> {

            if(code.length() > 0){

                code.deleteCharAt(code.length()-1);
                edtCode.setText(code.toString());
            }
        });

        btnMoTu.setOnClickListener(v -> {

            Order order = new Order();

            order.setLockerCode(
                    code.toString());

            order.setSelectedSlot(
                    spinnerSlot
                            .getSelectedItem()
                            .toString());

            ApiService apiService =
                    RetrofitClient
                            .getClient()
                            .create(ApiService.class);

            Call<Boolean> call =
                    apiService.checkLocker(order);

            call.enqueue(new Callback<Boolean>() {

                @Override
                public void onResponse(
                        Call<Boolean> call,
                        Response<Boolean> response) {

                    if(response.isSuccessful()
                            && response.body() != null){

                        Boolean success =
                                response.body();

                        if(success){

                            startActivity(
                                    new Intent(
                                            NhapMaTuActivity.this,
                                            MainActivity.class));

                            finish();

                        }else{

                            tvError.setText("Sai mã");
                        }

                    }else{

                        tvError.setText("Lỗi server");
                    }
                }

                @Override
                public void onFailure(
                        Call<Boolean> call,
                        Throwable t) {

                    tvError.setText(
                            "Lỗi kết nối");
                }
            });
        });
    }

    private void setupButton(int id, String number){

        Button btn = findViewById(id);

        btn.setOnClickListener(v -> {

            code.append(number);
            edtCode.setText(code.toString());

            tvError.setText("");
        });
    }

    private void loadActiveSlots(){

        ApiService apiService =
                RetrofitClient
                        .getClient()
                        .create(ApiService.class);

        apiService.getActiveSlots()
                .enqueue(
                        new Callback<List<String>>() {

                            @Override
                            public void onResponse(
                                    Call<List<String>> call,
                                    Response<List<String>> response) {

                                if(response.body()==null)
                                    return;

                                ArrayAdapter<String> adapter =
                                        new ArrayAdapter<>(
                                                NhapMaTuActivity.this,
                                                android.R.layout.simple_spinner_dropdown_item,
                                                response.body());

                                spinnerSlot.setAdapter(
                                        adapter);
                            }

                            @Override
                            public void onFailure(
                                    Call<List<String>> call,
                                    Throwable t) {

                                tvError.setText(
                                        "Không tải được danh sách ngăn");
                            }
                        });
    }
}