package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.adapters.OrderAdapter;
import org.store.narzedziuz.callbacks.OnOrdersLoaded;
import org.store.narzedziuz.models.AppUser;
import org.store.narzedziuz.models.Order;
import org.store.narzedziuz.repositories.OrderRepository;
import org.store.narzedziuz.repositories.UserRepository;

import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvInitials, tvFullName, tvEmail, tvOrderCount, tvTotalSpent;
    private RecyclerView recyclerOrders;
    private OrderAdapter orderAdapter;
    private Button btnWishlist, btnLogout, btnChangeLanguage;
    private ProgressBar progressBar;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.profile_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        tvInitials   = findViewById(R.id.tv_initials);
        tvFullName   = findViewById(R.id.tv_full_name);
        tvEmail      = findViewById(R.id.tv_email);
        tvOrderCount = findViewById(R.id.tv_order_count);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        recyclerOrders = findViewById(R.id.recycler_orders);
        btnWishlist  = findViewById(R.id.btn_wishlist);
        btnLogout    = findViewById(R.id.btn_logout);
        btnChangeLanguage = findViewById(R.id.btn_change_language);
        progressBar  = findViewById(R.id.progress_bar);

        orderAdapter = new OrderAdapter(this, order -> {
            Intent i = new Intent(this, OrderSummaryActivity.class);
            i.putExtra("orderId", order.getId());
            startActivity(i);
        });
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);
        recyclerOrders.setNestedScrollingEnabled(false);

        btnWishlist.setOnClickListener(v -> startActivity(new Intent(this, WishlistActivity.class)));
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });


        btnChangeLanguage.setOnClickListener(v -> {
            LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
            String currentLang = "pl"; // domyślny fallback

            if (!currentLocales.isEmpty()) {
                currentLang = currentLocales.get(0).getLanguage();
            } else {
                currentLang = Locale.getDefault().getLanguage();
            }

            if (currentLang.equals("en")) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("pl"));
            } else {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
            }
        });

        loadProfile();
        loadOrders();
    }

    private void loadProfile() {
        UserRepository.getInstance().getUser(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(AppUser user) {
                tvInitials.setText(user.getInitials());
                tvFullName.setText(user.getFullName());
                tvEmail.setText(user.getEmail());
            }
            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        OrderRepository.getInstance().getUserOrders(userId, new OnOrdersLoaded() {
            @Override
            public void onSuccess(List<Order> orders) {
                progressBar.setVisibility(View.GONE);
                orderAdapter.setOrders(orders);
                tvOrderCount.setText(String.valueOf(orders.size()));
                double total = orders.stream().mapToDouble(Order::getTotalPrice).sum();
                tvTotalSpent.setText(getString(R.string.profile_total_spent_format, total));
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}