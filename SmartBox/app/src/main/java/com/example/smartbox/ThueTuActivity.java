package com.example.smartbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartbox.api.ApiService;
import com.example.smartbox.api.Order;
import com.example.smartbox.api.RetrofitClient;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThueTuActivity extends AppCompatActivity {
    EditText edtName, edtPhone;
    Spinner spinnerDuration;
    RecyclerView recyclerOption;
    TextView tvSelected;
    TextView tvAmount;
    LinearLayout layoutSlots;
    String selectedPackage = "";
    ArrayList<String> selectedSlots = new ArrayList<>();
    long totalAmount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thue_tu);

        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);

        recyclerOption = findViewById(R.id.recyclerOption);

        tvSelected = findViewById(R.id.tvSelected);
        tvAmount = findViewById(R.id.tvAmount);

        layoutSlots = findViewById(R.id.layoutSlots);

        ImageView btnBack =
                findViewById(R.id.btnBack);

        Button btnThue =
                findViewById(R.id.btnThue);

        btnBack.setOnClickListener(v -> finish());

//        setupSpinner();

        setupPackageRecycler();

        loadAvailableSlots();

        spinnerDuration.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view,
                            int position,
                            long id) {

                        updateAmount();
                    }

                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {

                    }
                });

        btnThue.setOnClickListener(v -> createOrder());
    }

    private void updateDurationSpinner() {

        ArrayList<String> durations =
                new ArrayList<>();

        if(selectedPackage.contains("giờ")){

            for(int i = 1; i <= 24; i++){

                durations.add(String.valueOf(i));
            }

        }else if(selectedPackage.contains("ngày")){

            for(int i = 1; i <= 30; i++){

                durations.add(String.valueOf(i));
            }

        }else if(selectedPackage.contains("tháng")){

            for(int i = 1; i <= 3; i++){

                durations.add(String.valueOf(i));
            }

        }else if(selectedPackage.contains("1 lần")){

            durations.add("1");
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        durations);

        spinnerDuration.setAdapter(adapter);
    }

//    private void setupSpinner() {
//
//        String[] type = {
//                "Trong chung cư",
//                "Ngoài chung cư"
//        };
//
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(
//                        this,
//                        android.R.layout.simple_spinner_dropdown_item,
//                        type);
//
//
//        String[] duration = {
//                "1",
//                "2",
//                "3",
//                "4",
//                "5"
//        };
//
//        ArrayAdapter<String> durationAdapter =
//                new ArrayAdapter<>(
//                        this,
//                        android.R.layout.simple_spinner_dropdown_item,
//                        duration);
//
//        spinnerDuration.setAdapter(durationAdapter);
//    }

    private void setupPackageRecycler() {

        recyclerOption.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false));

        ArrayList<OptionModel> list =
                new ArrayList<>();

        list.add(
                new OptionModel(
                        "Thuê theo giờ",
                        "30k/h"));

        list.add(
                new OptionModel(
                        "Thuê theo ngày",
                        "150k/ngày"));

        list.add(
                new OptionModel(
                        "Thuê theo tháng",
                        "1tr/tháng"));

        list.add(
                new OptionModel(
                        "Thuê 1 lần",
                        "15k/lần"));

        OptionAdapter adapter =
                new OptionAdapter(
                        list,
                        model -> {

                            selectedPackage =
                                    model.getTitle()
                                            + " - "
                                            + model.getPrice();
                            updateDurationSpinner();

                            tvSelected.setText(
                                    "Đã chọn: "
                                            + selectedPackage);

                            tvSelected.startAnimation(
                                    AnimationUtils.loadAnimation(
                                            this,
                                            android.R.anim.fade_in));

                            updateAmount();
                        });

        recyclerOption.setAdapter(adapter);
    }

    private void loadAvailableSlots() {

        ApiService api =
                RetrofitClient
                        .getClient()
                        .create(ApiService.class);

        api.getAvailableSlots()
                .enqueue(
                        new Callback<List<String>>() {

                            @Override
                            public void onResponse(
                                    Call<List<String>> call,
                                    Response<List<String>> response) {

                                if(response.body()==null)
                                    return;

                                layoutSlots.removeAllViews();

                                for(String slot :
                                        response.body()){

                                    CheckBox cb =
                                            new CheckBox(
                                                    ThueTuActivity.this);

                                    cb.setText(slot);

                                    cb.setOnCheckedChangeListener(
                                            (buttonView,isChecked)->{

                                                if(isChecked){

                                                    selectedSlots.add(slot);

                                                }else{

                                                    selectedSlots.remove(slot);
                                                }

                                                updateAmount();
                                            });

                                    layoutSlots.addView(cb);
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<List<String>> call,
                                    Throwable t) {

                                Toast.makeText(
                                        ThueTuActivity.this,
                                        "Không tải được danh sách ngăn",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    private void updateAmount() {

        if(selectedPackage.isEmpty())
            return;

        int duration =
                Integer.parseInt(
                        spinnerDuration
                                .getSelectedItem()
                                .toString());

        long unitPrice = 0;

        if(selectedPackage.contains("giờ")){

            unitPrice = 30000;

        }else if(selectedPackage.contains("ngày")){

            unitPrice = 150000;

        }else if(selectedPackage.contains("tháng")){

            unitPrice = 1000000;

        }else if(selectedPackage.contains("1 lần")){

            unitPrice = 15000;
        }

        totalAmount =
                selectedSlots.size()
                        * duration
                        * unitPrice;

        tvAmount.setText(
                "Tổng tiền: "
                        + totalAmount
                        + " VNĐ");
    }

    private void createOrder() {

        String name =
                edtName.getText()
                        .toString()
                        .trim();

        String phone =
                edtPhone.getText()
                        .toString()
                        .trim();

        if(name.isEmpty()
                || phone.isEmpty()
                || selectedPackage.isEmpty()
                || selectedSlots.isEmpty()){

            Toast.makeText(
                    this,
                    "Vui lòng nhập đầy đủ thông tin",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        Order order =
                new Order();

        order.setCustomerName(name);

        order.setPhone(phone);

        order.setPackageType(
                selectedPackage);

        order.setSlots(
                selectedSlots);

        order.setDurationValue(
                Integer.parseInt(
                        spinnerDuration
                                .getSelectedItem()
                                .toString()));

        if(selectedPackage.contains("giờ")){

            order.setDurationType("HOUR");

        }else if(selectedPackage.contains("ngày")){

            order.setDurationType("DAY");

        }else if(selectedPackage.contains("tháng")){

            order.setDurationType("MONTH");

        }else{

            order.setDurationType("ONE_TIME");
        }

        ApiService api =
                RetrofitClient
                        .getClient()
                        .create(ApiService.class);

        api.createOrder(order)
                .enqueue(new Callback<Order>() {

                    @Override
                    public void onResponse(
                            Call<Order> call,
                            Response<Order> response) {

                        if(response.body()==null)
                            return;

                        Order result =
                                response.body();

                        Intent intent =
                                new Intent(
                                        ThueTuActivity.this,
                                        QRCodeActivity.class);

                        intent.putExtra(
                                "paymentCode",
                                result.getPaymentCode());

                        intent.putExtra(
                                "lockerCode",
                                result.getLockerCode());

                        intent.putExtra(
                                "packageType",
                                result.getPackageType());

                        intent.putExtra(
                                "amount",
                                (int) result.getAmount());

                        intent.putStringArrayListExtra(
                                "slots",
                                new ArrayList<>(
                                        result.getSlots()));

                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(
                            Call<Order> call,
                            Throwable t) {

                        Toast.makeText(
                                ThueTuActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}