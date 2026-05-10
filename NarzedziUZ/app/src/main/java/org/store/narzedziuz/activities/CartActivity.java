package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.adapters.CartAdapter;
import org.store.narzedziuz.callbacks.*;
import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.models.DiscountCode;
import org.store.narzedziuz.repositories.CartRepository;
import org.store.narzedziuz.repositories.DiscountCodeRepository;
import org.store.narzedziuz.utils.CartCalculator;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    RecyclerView recyclerCart;
    CartAdapter cartAdapter;
    ProgressBar progressBar;
    TextView tvEmpty, tvSubtotal, tvDiscount, tvTotal, tvDiscountInfo;
    EditText etDiscountCode;
    Button btnApplyDiscount, btnCheckout;
    View layoutSummary;

    String userId;
    DiscountCode activeDiscount = null;

    // 👉 wstrzykiwalne zależności
    CartRepository cartRepository = CartRepository.getInstance();
    DiscountCodeRepository discountRepository = new DiscountCodeRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        setupToolbar();
        initViews();

        userId = getUserId();

        cartAdapter = new CartAdapter(this, (item, position) -> removeItem(item));
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerCart.setAdapter(cartAdapter);

        btnApplyDiscount.setOnClickListener(v -> applyDiscountCode());
        btnCheckout.setOnClickListener(v -> proceedToCheckout());

        loadCart();
    }

    protected String getUserId() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.cart_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        recyclerCart = findViewById(R.id.recycler_cart);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvTotal = findViewById(R.id.tv_total);
        tvDiscountInfo = findViewById(R.id.tv_discount_info);
        etDiscountCode = findViewById(R.id.et_discount_code);
        btnApplyDiscount = findViewById(R.id.btn_apply_discount);
        btnCheckout = findViewById(R.id.btn_checkout);
        layoutSummary = findViewById(R.id.layout_summary);
    }

    void loadCart() {
        if (userId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            layoutSummary.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        cartRepository.getCart(userId, new OnCartLoaded() {
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
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CartActivity.this, "Błąd ładowania koszyka", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void updateSummary(List<CartItem> items) {
        double subtotal = CartCalculator.calculateSubtotal(items);

        tvSubtotal.setText(String.format(Locale.getDefault(), "Suma: %.2f PLN", subtotal));

        double total = CartCalculator.calculateTotal(subtotal, activeDiscount);

        if (activeDiscount != null) {
            double discountAmount = CartCalculator.calculateDiscountAmount(subtotal, activeDiscount);

            tvDiscount.setVisibility(View.VISIBLE);
            tvDiscountInfo.setVisibility(View.VISIBLE);

            tvDiscount.setText(String.format(Locale.getDefault(),
                    "Rabat -%d%%: -%.2f PLN",
                    activeDiscount.getPercent(),
                    discountAmount));
        } else {
            tvDiscount.setVisibility(View.GONE);
            tvDiscountInfo.setVisibility(View.GONE);
        }

        tvTotal.setText(String.format(Locale.getDefault(), "Do zapłaty: %.2f PLN", total));
    }

    void applyDiscountCode() {
        String code = etDiscountCode.getText().toString().trim();

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "Podaj kod rabatowy", Toast.LENGTH_SHORT).show();
            return;
        }

        discountRepository.verifyPersonalizedCode(code, userId, new OnDiscountCodeLoaded() {
            @Override
            public void onSuccess(DiscountCode dc) {

                if (dc.getProductId() != null) {
                    boolean hasProduct = false;
                    for (CartItem item : cartAdapter.getItems()) {
                        if (dc.getProductId().equals(item.getProductId())) {
                            hasProduct = true;
                            break;
                        }
                    }
                    if (!hasProduct) return;
                }

                activeDiscount = dc;
                updateSummary(cartAdapter.getItems());
            }

            @Override
            public void onFailure(String errorMessage) {
            }
        });
    }

    void removeItem(CartItem item) {
        if (userId == null) return;

        cartRepository.removeFromCart(userId, item.getId(), new OnComplete() {
            @Override public void onSuccess() { loadCart(); }
            @Override public void onFailure(Exception e) { }
        });
    }

    void proceedToCheckout() {
        List<CartItem> items = cartAdapter.getItems();
        if (items.isEmpty()) return;

        double subtotal = CartCalculator.calculateSubtotal(items);
        double finalPrice = CartCalculator.calculateTotal(subtotal, activeDiscount);

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra("finalPrice", finalPrice);
        startActivity(intent);
    }
}







