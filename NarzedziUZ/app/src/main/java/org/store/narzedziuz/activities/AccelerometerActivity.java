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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private float lastAcceleration = SensorManager.GRAVITY_EARTH;
    private float currentAcceleration = SensorManager.GRAVITY_EARTH;
    private float filteredAcceleration = 0.0f;
    private long lastShakeTime = 0L;
    private ConstraintLayout layoutBefore;
    private ConstraintLayout layoutAfter;

    private static final long SHAKE_COOLDOWN_MS = 500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discount_draw);

        Toolbar toolbar = findViewById(R.id.toolbar);
        shakeImage = findViewById(R.id.shakeImage);
        shakeBar = findViewById(R.id.shakeBar);

        setSupportActionBar(toolbar);


        layoutBefore = findViewById(R.id.layoutBeforeShake);
        layoutAfter = findViewById(R.id.layoutAfterShake);

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

            lastAcceleration = currentAcceleration;
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
    private void updateProgressAfterShake() {
        progres += 20;
        if (progres > 100) {
            progres = 100;
        }

        shakeBar.setProgress(progres);

        if (progres == 100) {
            Toast.makeText(this, R.string.shakeuz_promotion_won, Toast.LENGTH_SHORT).show();

            layoutBefore.setVisibility(View.GONE);
            layoutAfter.setVisibility(View.VISIBLE);
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