package com.example.smartbox;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.smartbox.api.ApiService;
import com.example.smartbox.api.PaymentResponse;
import com.example.smartbox.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;

public class QRCodeActivity extends AppCompatActivity {

    LinearLayout layoutQR;
    LinearLayout layoutSuccess;

    TextView tvPaymentCode;
    TextView tvLockerCode;

    ImageView imgQR;
    ImageView imgSuccessQR;

    String paymentCode;
    String lockerCode;

    ArrayList<String> slots;

    TextView tvSlot;

    String packageType;

    int amount = 0;

    Handler handler = new Handler();

    Runnable pollingRunnable;

    boolean isPaid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);

        layoutQR =
                findViewById(R.id.layoutQR);

        layoutSuccess =
                findViewById(R.id.layoutSuccess);

        tvPaymentCode =
                findViewById(R.id.tvPaymentCode);

        tvLockerCode =
                findViewById(R.id.tvLockerCode);

        tvSlot =
                findViewById(R.id.tvSlot);

        imgQR =
                findViewById(R.id.imgQR);

        Button btnPaid =
                findViewById(R.id.btnPaid);

        Button btnHome =
                findViewById(R.id.btnHome);

        paymentCode =
                getIntent().getStringExtra(
                        "paymentCode");

        imgSuccessQR =
                findViewById(R.id.imgSuccessQR);

        lockerCode =
                getIntent().getStringExtra(
                        "lockerCode");

        slots =
                getIntent()
                        .getStringArrayListExtra(
                                "slots");

        packageType =
                getIntent().getStringExtra(
                        "packageType");

        amount =
                getIntent().getIntExtra(
                        "amount",
                        0);


        // HIỂN THỊ PAYMENT CODE
        tvPaymentCode.setText(
                "Nội dung CK: " + paymentCode);

        // QR REALTIME VIETQR
        String qrUrl =
                "https://img.vietqr.io/image/"
                        + "MB-060205102004-compact2.png"
                        + "?amount=" + amount
                        + "&addInfo=" + paymentCode
                        + "&accountName=SMARTBOX";

        Glide.with(this)
                .load(qrUrl)
                .into(imgQR);

        // ANIMATION
        layoutQR.startAnimation(
                AnimationUtils.loadAnimation(
                        this,
                        android.R.anim.fade_in));

        // START POLLING
        startPollingPayment();

        // GIẢ LẬP THANH TOÁN
        btnPaid.setOnClickListener(v -> {

            ApiService apiService =
                    RetrofitClient
                            .getClient()
                            .create(ApiService.class);

            apiService.fakePayment(paymentCode)
                    .enqueue(new Callback<PaymentResponse>() {

                        @Override
                        public void onResponse(
                                Call<PaymentResponse> call,
                                Response<PaymentResponse> response) {

                            if(response.isSuccessful()
                                    && response.body() != null){

                                Toast.makeText(
                                        QRCodeActivity.this,
                                        "Đã giả lập thanh toán",
                                        Toast.LENGTH_SHORT).show();

                            }else{

                                Toast.makeText(
                                        QRCodeActivity.this,
                                        "Lỗi payment",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<PaymentResponse> call,
                                Throwable t) {

                            Toast.makeText(
                                    QRCodeActivity.this,
                                    t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // HOME
        btnHome.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            QRCodeActivity.this,
                            MainActivity.class);

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

            finish();
        });
    }

    private void startPollingPayment(){

        pollingRunnable = new Runnable() {
            @Override
            public void run() {

                if(isPaid){
                    return;
                }

                ApiService apiService =
                        RetrofitClient
                                .getClient()
                                .create(ApiService.class);

                apiService.checkPayment(paymentCode)
                        .enqueue(new Callback<PaymentResponse>() {

                            @Override
                            public void onResponse(
                                    Call<PaymentResponse> call,
                                    Response<PaymentResponse> response) {

                                if(response.isSuccessful()
                                        && response.body() != null){

                                    String status =
                                            response.body()
                                                    .getStatus();

                                    System.out.println(
                                            "PAYMENT STATUS = "
                                                    + status);

                                    if(status.equals("PAID")){

                                        isPaid = true;

                                        showSuccess();

                                    }else{

                                        handler.postDelayed(
                                                pollingRunnable,
                                                3000);
                                    }

                                }else{

                                    handler.postDelayed(
                                            pollingRunnable,
                                            3000);
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<PaymentResponse> call,
                                    Throwable t) {

                                t.printStackTrace();

                                handler.postDelayed(
                                        pollingRunnable,
                                        3000);
                            }
                        });
            }
        };

        handler.post(pollingRunnable);
    }

    private void showSuccess(){

        handler.removeCallbacks(
                pollingRunnable);

        runOnUiThread(() -> {

            layoutQR.setVisibility(
                    View.GONE);

            layoutSuccess.setVisibility(
                    View.VISIBLE);

            layoutSuccess.startAnimation(
                    AnimationUtils.loadAnimation(
                            this,
                            android.R.anim.fade_in));

            if(slots != null){

                tvSlot.setText(
                        "Ngăn được cấp: "
                                + String.join(
                                ", ",
                                slots));

            }else{

                tvSlot.setText(
                        "Ngăn được cấp: Không xác định");
            }

            tvLockerCode.setText(
                    "Mã mở tủ: " + lockerCode);

            // GENERATE QR REALTIME
            try {

                QRCodeWriter writer =
                        new QRCodeWriter();

                Bitmap bitmap =
                        Bitmap.createBitmap(
                                512,
                                512,
                                Bitmap.Config.RGB_565);

                var bitMatrix =
                        writer.encode(
                                lockerCode,
                                BarcodeFormat.QR_CODE,
                                512,
                                512);

                for(int x = 0; x < 512; x++){

                    for(int y = 0; y < 512; y++){

                        bitmap.setPixel(
                                x,
                                y,
                                bitMatrix.get(x,y)
                                        ? 0xFF000000
                                        : 0xFFFFFFFF);
                    }
                }

                imgSuccessQR.setImageBitmap(bitmap);

            } catch (WriterException e) {

                e.printStackTrace();
            }

            Toast.makeText(
                    QRCodeActivity.this,
                    "Thanh toán thành công",
                    Toast.LENGTH_SHORT).show();
        });


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(
                pollingRunnable);
    }


}
