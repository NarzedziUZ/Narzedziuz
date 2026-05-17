package org.store.narzedziuz.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import org.store.narzedziuz.R;
import org.store.narzedziuz.callbacks.OnCartLoaded;
import org.store.narzedziuz.callbacks.OnComplete;
import org.store.narzedziuz.callbacks.OnOrderLoaded;
import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.models.Order;
import org.store.narzedziuz.models.OrderItem;
import org.store.narzedziuz.repositories.CartRepository;
import org.store.narzedziuz.repositories.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private EditText etStreet, etHouseNumber, etCity, etZipCode, etCountry;
    private RadioGroup rgPayment;
    private RadioButton rbBlik, rbCard, rbTransfer, rbPaypal;
    private LinearLayout layoutBlik, layoutCard;
    private EditText etBlikCode, etCardNumber, etCardExpiry, etCardCvc;
    private TextView tvTotal;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private String userId;
    private double finalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.checkout_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        userId     = FirebaseAuth.getInstance().getCurrentUser().getUid();
        finalPrice = getIntent().getDoubleExtra("finalPrice", 0.0);

        etStreet      = findViewById(R.id.et_street);
        etHouseNumber = findViewById(R.id.et_house_number);
        etCity        = findViewById(R.id.et_city);
        etZipCode     = findViewById(R.id.et_zip_code);
        etCountry     = findViewById(R.id.et_country);
        rgPayment     = findViewById(R.id.rg_payment);
        rbBlik        = findViewById(R.id.rb_blik);
        rbCard        = findViewById(R.id.rb_card);
        rbTransfer    = findViewById(R.id.rb_transfer);
        rbPaypal      = findViewById(R.id.rb_paypal);
        layoutBlik    = findViewById(R.id.layout_blik);
        layoutCard    = findViewById(R.id.layout_card);
        etBlikCode    = findViewById(R.id.et_blik_code);
        etCardNumber  = findViewById(R.id.et_card_number);
        etCardExpiry  = findViewById(R.id.et_card_expiry);
        etCardCvc     = findViewById(R.id.et_card_cvc);
        tvTotal       = findViewById(R.id.tv_total);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        progressBar   = findViewById(R.id.progress_bar);

        tvTotal.setText(getString(R.string.checkout_total_format, finalPrice));

        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            layoutBlik.setVisibility(checkedId == R.id.rb_blik ? View.VISIBLE : View.GONE);
            layoutCard.setVisibility(checkedId == R.id.rb_card ? View.VISIBLE : View.GONE);
        });

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String street  = etStreet.getText().toString().trim();
        String houseNo = etHouseNumber.getText().toString().trim();
        String city    = etCity.getText().toString().trim();
        String zip     = etZipCode.getText().toString().trim();
        String country = etCountry.getText().toString().trim();

        if (TextUtils.isEmpty(street))  { etStreet.setError(getString(R.string.checkout_validation_required)); return; }
        if (TextUtils.isEmpty(houseNo)) { etHouseNumber.setError(getString(R.string.checkout_validation_required)); return; }
        if (TextUtils.isEmpty(city))    { etCity.setError(getString(R.string.checkout_validation_required)); return; }
        if (TextUtils.isEmpty(zip))     { etZipCode.setError(getString(R.string.checkout_validation_required)); return; }
        if (TextUtils.isEmpty(country)) { etCountry.setError(getString(R.string.checkout_validation_required)); return; }

        int checkedId = rgPayment.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, R.string.checkout_error_select_payment, Toast.LENGTH_SHORT).show();
            return;
        }

        // Walidacja szczegółów płatności
        String paymentMethod;
        if (checkedId == R.id.rb_blik) {
            String blik = etBlikCode.getText().toString().trim();
            if (blik.length() != 6) { etBlikCode.setError(getString(R.string.checkout_error_blik_length)); return; }
            paymentMethod = getString(R.string.payment_method_blik);
        } else if (checkedId == R.id.rb_card) {
            String cardNum = etCardNumber.getText().toString().replaceAll("\\s","");
            if (cardNum.length() != 16) { etCardNumber.setError(getString(R.string.checkout_error_card_length)); return; }
            String last4 = cardNum.substring(12);
            paymentMethod = getString(R.string.payment_method_card, last4);
        } else if (checkedId == R.id.rb_transfer) {
            paymentMethod = getString(R.string.payment_method_transfer);
        } else {
            paymentMethod = getString(R.string.payment_method_paypal);
        }

        String address = street + " " + houseNo + ", " + zip + " " + city + ", " + country;
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        // Pobierz koszyk, utwórz zamówienie, wyczyść koszyk
        getCart(items -> {
            if (items == null || items.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(this, "Koszyk jest pusty", Toast.LENGTH_SHORT).show();
                return;
            }

            Order order = new Order(userId, address, paymentMethod, finalPrice);
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem ci : items) {
                String pName = ci.getProduct() != null ? ci.getProduct().getName() : ci.getProductId();
                orderItems.add(new OrderItem(ci.getProductId(), pName, ci.getQuantity(), ci.getPrice()));
            }
            order.setItems(orderItems);

            OrderRepository.getInstance().createOrder(order, new OnOrderLoaded() {
                @Override
                public void onSuccess(Order savedOrder) {
                    CartRepository.getInstance().clearCart(userId, new OnComplete() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            org.store.narzedziuz.widgets.CartWidgetProvider.updateWidget(CheckoutActivity.this);
                            Intent intent = new Intent(CheckoutActivity.this, OrderSummaryActivity.class);
                            intent.putExtra("orderId", savedOrder.getId());
                            startActivity(intent);
                            finish();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            progressBar.setVisibility(View.GONE);
                            btnPlaceOrder.setEnabled(true);
                        }
                    });
                }
                @Override
                public void onFailure(Exception e) {
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(CheckoutActivity.this, getString(R.string.checkout_error_order_failed, e.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }, e -> {
            progressBar.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(true);
            Toast.makeText(this, R.string.cart_error_loading, Toast.LENGTH_SHORT).show();
        });
    }

    // Pomocnicza metoda – pobiera koszyk z dwoma lambdami
    private void getCart(CartLoadedCallback onSuccess, ErrorCallback onError) {
        CartRepository.getInstance().getCart(userId, new OnCartLoaded() {
            @Override public void onSuccess(List<CartItem> items) { onSuccess.onLoaded(items); }
            @Override public void onFailure(Exception e) { onError.onError(e); }
        });
    }

    interface CartLoadedCallback { void onLoaded(List<CartItem> items); }
    interface ErrorCallback      { void onError(Exception e); }
}
