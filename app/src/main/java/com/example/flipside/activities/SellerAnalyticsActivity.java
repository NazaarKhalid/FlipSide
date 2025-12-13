package com.example.flipside.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flipside.R;
import com.example.flipside.services.StoreAnalyticsFacade;
import com.google.firebase.auth.FirebaseAuth;

public class SellerAnalyticsActivity extends AppCompatActivity {

    private TextView tvTotalRevenue, tvTotalOrders, tvItemsSold;
    private ProgressBar progressBar;

    private StoreAnalyticsFacade analyticsFacade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_analytics);

        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvTotalOrders = findViewById(R.id.tvTotalOrders);
        tvItemsSold = findViewById(R.id.tvItemsSold);
        progressBar = findViewById(R.id.progressBar);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        analyticsFacade = new StoreAnalyticsFacade();

        progressBar.setVisibility(View.VISIBLE);
        analyticsFacade.fetchStoreStats(currentUserId, new StoreAnalyticsFacade.AnalyticsCallback() {
            @Override
            public void onDataCalculated(double totalRevenue, int totalOrders, int totalItemsSold) {
                progressBar.setVisibility(View.GONE);
                tvTotalRevenue.setText("PKR " + totalRevenue);
                tvTotalOrders.setText(String.valueOf(totalOrders));
                tvItemsSold.setText(String.valueOf(totalItemsSold));
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SellerAnalyticsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}