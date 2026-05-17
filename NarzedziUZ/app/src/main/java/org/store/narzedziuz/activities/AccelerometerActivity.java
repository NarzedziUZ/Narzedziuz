package org.store.narzedziuz.activities;


import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.store.narzedziuz.models.DiscountCode;
import org.store.narzedziuz.repositories.DiscountCodeRepository;
import java.util.Random;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.store.narzedziuz.R;
import org.store.narzedziuz.utils.ShakeDetector;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView shakeImage;
    private ProgressBar shakeBar;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int progres;
    private ShakeDetector shakeDetector;
    private ConstraintLayout layoutBefore;
    private ConstraintLayout layoutAfter;
    private TextView DiscountView;
    private TextView DiscountName;

    private static final long SHAKE_COOLDOWN_MS = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_draw);

        Toolbar toolbar = findViewById(R.id.toolbar);
        shakeImage = findViewById(R.id.shakeImage);
        shakeBar = findViewById(R.id.shakeBar);
        DiscountView = findViewById(R.id.tvPromoCode);
        DiscountName = findViewById(R.id.tvPromoName);
        Button btnClaim = findViewById(R.id.btnClaim);

        layoutBefore = findViewById(R.id.layoutBeforeShake);
        layoutAfter = findViewById(R.id.layoutAfterShake);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, R.string.shakeuz_no_accelerometer, Toast.LENGTH_SHORT).show();
        }
        shakeDetector = new ShakeDetector(() -> {
            shakeImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation));
            updateProgressAfterShake();
        });
        checkExistingPromotion();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            shakeDetector.process(
                    event.values[0],
                    event.values[1],
                    event.values[2],
                    System.currentTimeMillis()
            );
        }
    }
    private void generateAndSaveDiscountCode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.shakeuz_need_to_be_logged_to_get_code, Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // 1. Pobieramy wszystkie produkty z bazy, aby wylosować jeden z nich
        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> productsList = task.getResult().getDocuments();

                        if (productsList.isEmpty()) {
                            Toast.makeText(this, R.string.shakeuz_no_product_error, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 2. Losujemy jeden produkt z pobranej listy
                        int randomIndex = new Random().nextInt(productsList.size());
                        DocumentSnapshot randomProduct = productsList.get(randomIndex);
                        String randomProductId = randomProduct.getId();

                        // Opcjonalnie: pobieramy nazwę produktu, żeby pokazać ją użytkownikowi
                        String randomProductName = randomProduct.getString("name");

                        // 3. Generujemy kod zniżkowy dla tego konkretnego, wylosowanego produktu
                        String randomCodeString = "SHAKE" + (new Random().nextInt(9000) + 1000);
                        int discountPercent = 20; // 20% zniżki

                        DiscountCode newDiscountCode = new DiscountCode(randomCodeString, discountPercent, randomProductId, userId);
                        DiscountCodeRepository repository = new DiscountCodeRepository();

                        // 4. Zapisujemy kod w Firebase
                        repository.createDiscountCode(newDiscountCode, saveTask -> {
                            if (saveTask.isSuccessful()) {
                                // Ustawiamy kod na ekranie
                                DiscountView.setText(randomCodeString);

                                // DODANE: Ustawiamy nazwę produktu na ekranie
                                if (randomProductName != null) {
                                    DiscountName.setText(randomProductName);
                                } else {
                                    DiscountName.setText(getString(R.string.shakeuz_default_product_name)); // zabezpieczenie gdyby pole name w Firebase było puste
                                }
                                savePromotionToMemory(randomCodeString, randomProductName,userId);
                                layoutBefore.setVisibility(View.GONE);
                                layoutAfter.setVisibility(View.VISIBLE);

                                // DODANE: Logika przycisku "Odbierz" (Kopiowanie do schowka)
                                Button btnClaim = findViewById(R.id.btnClaim);
                                btnClaim.setOnClickListener(v -> {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText(getString(R.string.shakeuz_clipboard_label), randomCodeString);
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(AccelerometerActivity.this, R.string.shakeuz_code_was_copied, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                Toast.makeText(this, R.string.shakeuz_error_saving_code, Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {
                        Toast.makeText(this, R.string.shakeuz_error_fetching_products, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updateProgressAfterShake() {
        progres += 20;
        if (progres > 100) {
            progres = 100;
        }

        shakeBar.setProgress(progres);

        if (progres == 100) {
            Toast.makeText(this, R.string.shakeuz_promotion_won, Toast.LENGTH_SHORT).show();

            // Wywołujemy naszą nową metodę do zapisu w Firebase
            generateAndSaveDiscountCode();
            // Zatrzymujemy nasłuchiwanie, żeby kod nie generował się wielokrotnie
            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
        }
    }
    private void savePromotionToMemory(String code, String productName, String userId) {
        SharedPreferences prefs = getSharedPreferences("ShakePromotionPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        editor.putString("promo_date", todayDate);
        editor.putString("promo_code", code);
        editor.putString("promo_product", productName);

        // DODANE: Zapisujemy do kogo należy ta promocja
        editor.putString("promo_user_id", userId);

        editor.apply();
    }

    // Sprawdza czy użytkownik dzisiaj już wylosował promocję
    private void checkExistingPromotion() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return; // Jak nie ma usera to zignoruj sprawdzanie

        String currentUserId = currentUser.getUid();

        SharedPreferences prefs = getSharedPreferences("ShakePromotionPrefs", MODE_PRIVATE);

        String savedDate = prefs.getString("promo_date", "");
        String savedUserId = prefs.getString("promo_user_id", "");
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // ZMIENIONE: Sprawdzamy czy data jest dzisiejsza ORAZ czy to ten sam użytkownik
        if (savedDate.equals(todayDate) && savedUserId.equals(currentUserId)) {
            String savedCode = prefs.getString("promo_code", "");
            String savedProduct = prefs.getString("promo_product", "");

            DiscountView.setText(savedCode);
            DiscountName.setText(savedProduct);

            layoutBefore.setVisibility(View.GONE);
            layoutAfter.setVisibility(View.VISIBLE);

            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }

            Button btnClaim = findViewById(R.id.btnClaim);
            btnClaim.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Kod rabatowy", savedCode);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(AccelerometerActivity.this, "Kod został skopiowany do schowka!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}