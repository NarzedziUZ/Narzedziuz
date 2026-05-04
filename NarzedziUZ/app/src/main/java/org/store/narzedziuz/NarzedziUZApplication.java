package org.store.narzedziuz;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.store.narzedziuz.utils.DataSeeder;
import org.store.narzedziuz.utils.NotificationHelper;

public class NarzedziUZApplication extends Application implements DefaultLifecycleObserver {

    private static final String PREFS_NAME = "NarzedziUZ_prefs";
    private static final String KEY_SEEDED = "data_seeded_v2";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        seedInitialDataIfNeeded();
        NotificationHelper.scheduleDailyNotification(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        checkAndSendReminder();
    }

    private void checkAndSendReminder() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        boolean wonToday = NotificationHelper.hasUserWonToday(this, userId);

        if (!wonToday) {
            NotificationHelper.showNotification(this);
        }
    }

    private void seedInitialDataIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_SEEDED, false)) {
            DataSeeder.seedData(() -> {
                prefs.edit().putBoolean(KEY_SEEDED, true).apply();
            });
        }
    }
}
