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

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView shakeImage;
    private ProgressBar shakeBar;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int progres;
    private float currentAcceleration = SensorManager.GRAVITY_EARTH;
    private float filteredAcceleration = 0.0f;
    private long lastShakeTime = 0L;
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
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float lastAcceleration = currentAcceleration;
            currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);

            float delta = Math.abs(currentAcceleration - lastAcceleration);
            filteredAcceleration = 0.8f * filteredAcceleration + 0.2f * delta;

            long now = System.currentTimeMillis();
            if (filteredAcceleration > 3.0f && now - lastShakeTime > SHAKE_COOLDOWN_MS) {
                lastShakeTime = now;
                shakeImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_animation));
                updateProgressAfterShake();
            }
        }
    }
    private void generateAndSaveDiscountCode() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        FirebaseFirestore.getInstance().collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<DocumentSnapshot> productsList = task.getResult().getDocuments();

                        if (productsList.isEmpty()) {
                            Toast.makeText(this, "Błąd: Brak produktów w bazie!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int randomIndex = new Random().nextInt(productsList.size());
                        DocumentSnapshot randomProduct = productsList.get(randomIndex);
                        String randomProductId = randomProduct.getId();
                        String randomProductName = randomProduct.getString("name");

                        String randomCodeString = "SHAKE" + (new Random().nextInt(9000) + 1000);

                        DiscountCode newDiscountCode = new DiscountCode(randomCodeString, 20, randomProductId, userId, todayDate);
                        DiscountCodeRepository repository = new DiscountCodeRepository();

                        repository.createDiscountCode(newDiscountCode, saveTask -> {
                            if (saveTask.isSuccessful()) {
                                DiscountView.setText(randomCodeString);
                                String productNameToDisplay = randomProductName != null ? randomProductName : "wybrany produkt";
                                DiscountName.setText(productNameToDisplay);

                                layoutBefore.setVisibility(View.GONE);
                                layoutAfter.setVisibility(View.VISIBLE);

                                Button btnClaim = findViewById(R.id.btnClaim);
                                btnClaim.setOnClickListener(v -> {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Kod rabatowy", randomCodeString);
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(AccelerometerActivity.this, "Kod skopiowany do schowka!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(this, "Błąd podczas zapisywania kodu.", Toast.LENGTH_SHORT).show();
                            }
                        });
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

    private void checkExistingPromotion() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        // Pokazujemy ładowanie zanim ekran potrząsania się pojawi (opcjonalnie)
        layoutBefore.setVisibility(View.GONE);
        layoutAfter.setVisibility(View.GONE);

        // PYTAMY FIREBASE PRZY WEJŚCIU NA STRONĘ:
        FirebaseFirestore.getInstance().collection("discount_codes")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        DiscountCode existingCode = document.toObject(DiscountCode.class);

                        if (existingCode != null) {
                            if (todayDate.equals(existingCode.getGeneratedDate())) {
                                // 1. Kod jest z dzisiaj! Pokazujemy go natychmiast, z pominięciem potrząsania
                                DiscountView.setText(existingCode.getCode());

                                // Opcjonalnie nazwa produktu (wymagałoby zapytania do bazy products, więc ułatwmy:)
                                DiscountName.setText("Twój produkt");

                                layoutAfter.setVisibility(View.VISIBLE);

                                if (sensorManager != null) {
                                    sensorManager.unregisterListener(this);
                                }

                                Button btnClaim = findViewById(R.id.btnClaim);
                                btnClaim.setOnClickListener(v -> {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Kod rabatowy", existingCode.getCode());
                                    if (clipboard != null) {
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(AccelerometerActivity.this, "Kod skopiowany do schowka!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return; // Kończymy - nie pozwalamy potrząsać
                            } else {
                                // 2. Kod jest stary! Usuwamy go z bazy Firebase
                                document.getReference().delete();
                                // Po usunięciu wyświetlamy ekran potrząsania
                                layoutBefore.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        // 3. Brak kodów w bazie Firebase - użytkownik jest "czysty", może potrząsać
                        layoutBefore.setVisibility(View.VISIBLE);
                    }
                });
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