package org.store.narzedziuz;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;

import org.store.narzedziuz.utils.DataSeeder;

public class NarzedziUZApplication extends Application {

    private static final String PREFS_NAME = "NarzedziUZ_prefs";
    private static final String KEY_SEEDED = "data_seeded_v2";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        seedInitialDataIfNeeded();
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
