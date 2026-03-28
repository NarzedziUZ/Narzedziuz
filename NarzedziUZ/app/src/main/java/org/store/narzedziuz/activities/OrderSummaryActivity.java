package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.callbacks.OnOrderLoaded;
import org.store.narzedziuz.models.Order;
import org.store.narzedziuz.models.OrderItem;
import org.store.narzedziuz.repositories.OrderRepository;

import java.util.Locale;

public class OrderSummaryActivity extends AppCompatActivity {

    private TextView tvOrderId, tvStatus, tvDate, tvAddress, tvPayment, tvTotal;
    private RecyclerView recyclerItems;
    private Button btnGoHome, btnGoProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.order_summary_title);

        tvOrderId   = findViewById(R.id.tv_order_id);
        tvStatus    = findViewById(R.id.tv_status);
        tvDate      = findViewById(R.id.tv_date);
        tvAddress   = findViewById(R.id.tv_address);
        tvPayment   = findViewById(R.id.tv_payment);
        tvTotal     = findViewById(R.id.tv_total);
        recyclerItems = findViewById(R.id.recycler_items);
        btnGoHome   = findViewById(R.id.btn_go_home);
        btnGoProfile= findViewById(R.id.btn_go_profile);
        progressBar = findViewById(R.id.progress_bar);

        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setNestedScrollingEnabled(false);

        String orderId = getIntent().getStringExtra("orderId");
        String userId  = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        OrderRepository.getInstance().getOrderById(userId, orderId, new OnOrderLoaded() {
            @Override
            public void onSuccess(Order order) {
                progressBar.setVisibility(View.GONE);
                displayOrder(order);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });

        btnGoHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        });
        btnGoProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void displayOrder(Order order) {
        tvOrderId.setText(getString(R.string.order_number, order.getId().substring(0, Math.min(8, order.getId().length()))));
        tvStatus.setText(order.getStatus());
        tvDate.setText(order.getFormattedDate());
        tvAddress.setText(order.getDeliveryAddress());
        tvPayment.setText(order.getPaymentMethod());
        tvTotal.setText(String.format(Locale.getDefault(), "%.2f PLN", order.getTotalPrice()));

        // Adapter inline dla pozycji zamówienia
        recyclerItems.setAdapter(new androidx.recyclerview.widget.RecyclerView.Adapter<OrderItemViewHolder>() {
            @Override
            public int getItemCount() { return order.getItems() != null ? order.getItems().size() : 0; }

            @Override
            public OrderItemViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                View v = getLayoutInflater().inflate(R.layout.item_order_line, parent, false);
                return new OrderItemViewHolder(v);
            }

            @Override
            public void onBindViewHolder(OrderItemViewHolder holder, int pos) {
                OrderItem item = order.getItems().get(pos);
                holder.tvName.setText(item.getProductName());
                holder.tvQty.setText("x" + item.getQuantity());
                holder.tvPrice.setText(String.format(Locale.getDefault(), "%.2f PLN", item.getTotalPrice()));
            }
        });
    }

    static class OrderItemViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView tvName, tvQty, tvPrice;
        OrderItemViewHolder(View v) {
            super(v);
            tvName  = v.findViewById(R.id.tv_product_name);
            tvQty   = v.findViewById(R.id.tv_quantity);
            tvPrice = v.findViewById(R.id.tv_price);
        }
    }
}
