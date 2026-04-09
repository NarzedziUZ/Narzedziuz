package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.adapters.CartAdapter;
import org.store.narzedziuz.callbacks.OnCartLoaded;
import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.models.DiscountCode;
import org.store.narzedziuz.repositories.CartRepository;
import org.store.narzedziuz.utils.DiscountCodeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvSubtotal, tvDiscount, tvTotal, tvDiscountInfo;
    private EditText etDiscountCode;
    private Button btnApplyDiscount, btnCheckout;
    private View layoutSummary;

    private String userId;
    private DiscountCode activeDiscount = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cart_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        recyclerCart     = findViewById(R.id.recycler_cart);
        progressBar      = findViewById(R.id.progress_bar);
        tvEmpty          = findViewById(R.id.tv_empty);
        tvSubtotal       = findViewById(R.id.tv_subtotal);
        tvDiscount       = findViewById(R.id.tv_discount);
        tvTotal          = findViewById(R.id.tv_total);
        tvDiscountInfo   = findViewById(R.id.tv_discount_info);
        etDiscountCode   = findViewById(R.id.et_discount_code);
        btnApplyDiscount = findViewById(R.id.btn_apply_discount);
        btnCheckout      = findViewById(R.id.btn_checkout);
        layoutSummary    = findViewById(R.id.layout_summary);

        cartAdapter = new CartAdapter(this, (item, position) -> removeItem(item));
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);

        btnApplyDiscount.setOnClickListener(v -> applyDiscountCode());
        btnCheckout.setOnClickListener(v -> proceedToCheckout());

        loadCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCart();
    }



    private void loadCart() {
        if (userId == null) { tvEmpty.setVisibility(View.VISIBLE); layoutSummary.setVisibility(View.GONE); return; }
        progressBar.setVisibility(View.VISIBLE);
        CartRepository.getInstance().getCart(userId, new OnCartLoaded() {
            @Override
            public void onSuccess(List<CartItem> items) {
                progressBar.setVisibility(View.GONE);
                cartAdapter.setItems(items);
                if (items.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    layoutSummary.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    layoutSummary.setVisibility(View.VISIBLE);
                    updateSummary(items);
                }
                org.store.narzedziuz.widgets.CartWidgetProvider.updateWidget(CartActivity.this);
            }
            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, "Błąd ładowania koszyka", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary(List<CartItem> items) {
        double subtotal = 0;
        for (CartItem item : items) subtotal += item.getTotalPrice();

        tvSubtotal.setText(String.format(Locale.getDefault(), "Suma: %.2f PLN", subtotal));

        if (activeDiscount != null) {
            double discountAmount = subtotal * activeDiscount.getPercent() / 100.0;
            double total = subtotal - discountAmount;
            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscountInfo.setVisibility(View.VISIBLE);
            tvDiscount.setText(String.format(Locale.getDefault(), "Rabat -%d%%: -%.2f PLN", activeDiscount.getPercent(), discountAmount));
            tvDiscountInfo.setText(getString(R.string.discount_applied, activeDiscount.getCode(), activeDiscount.getPercent()));
            tvTotal.setText(String.format(Locale.getDefault(), "Do zapłaty: %.2f PLN", total));
        } else {
            tvDiscount.setVisibility(View.GONE);
            tvDiscountInfo.setVisibility(View.GONE);
            tvTotal.setText(String.format(Locale.getDefault(), "Do zapłaty: %.2f PLN", subtotal));
        }
    }

    private void applyDiscountCode() {
        String code = etDiscountCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) { Toast.makeText(this, "Podaj kod rabatowy", Toast.LENGTH_SHORT).show(); return; }
        DiscountCode dc = DiscountCodeHelper.findByCode(code);
        if (dc != null) {
            activeDiscount = dc;
            Toast.makeText(this, getString(R.string.discount_applied, dc.getCode(), dc.getPercent()), Toast.LENGTH_SHORT).show();
            updateSummary(cartAdapter.getItems());
        } else {
            Toast.makeText(this, R.string.invalid_discount, Toast.LENGTH_SHORT).show();
        }
    }

    private void removeItem(CartItem item) {
        if (userId == null) return;
        CartRepository.getInstance().removeFromCart(userId, item.getId(), new OnComplete() {
            @Override public void onSuccess() { loadCart();
                org.store.narzedziuz.widgets.CartWidgetProvider.updateWidget(CartActivity.this);}
            @Override public void onFailure(Exception e) {
                Toast.makeText(CartActivity.this, "Błąd usuwania", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void proceedToCheckout() {
        List<CartItem> items = cartAdapter.getItems();
        if (items.isEmpty()) { Toast.makeText(this, "Koszyk jest pusty", Toast.LENGTH_SHORT).show(); return; }

        double subtotal = 0;
        for (CartItem item : items) subtotal += item.getTotalPrice();
        double finalPrice = activeDiscount != null
                ? subtotal * (100 - activeDiscount.getPercent()) / 100.0
                : subtotal;

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("finalPrice", finalPrice);
        startActivity(intent);
    }
}
