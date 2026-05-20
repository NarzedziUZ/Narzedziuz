// org/store/narzedziuz/utils/ShakeDetector.java
package org.store.narzedziuz.utils;

public class ShakeDetector {

    public interface OnShakeListener {
        void onShake();
    }

    private static final float SHAKE_THRESHOLD = 3.0f;
    private static final long SHAKE_COOLDOWN_MS = 500L;

    private float currentAcceleration = 9.81f; // GRAVITY_EARTH
    private float filteredAcceleration = 0.0f;
    private long lastShakeTime = 0L;

    private final OnShakeListener listener;

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    public void process(float x, float y, float z, long currentTimeMs) {
        float lastAcc = currentAcceleration;
        currentAcceleration = (float) Math.sqrt(x * x + y * y + z * z);

        float delta = Math.abs(currentAcceleration - lastAcc);
        filteredAcceleration = 0.8f * filteredAcceleration + 0.2f * delta;

        if (filteredAcceleration > SHAKE_THRESHOLD
                && currentTimeMs - lastShakeTime > SHAKE_COOLDOWN_MS) {
            lastShakeTime = currentTimeMs;
            listener.onShake();
        }
    }

    // Getter do testów
    public float getFilteredAcceleration() {
        return filteredAcceleration;
    }
}