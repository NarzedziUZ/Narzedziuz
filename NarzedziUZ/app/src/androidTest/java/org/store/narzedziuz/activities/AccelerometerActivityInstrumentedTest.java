package org.store.narzedziuz.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.store.narzedziuz.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

// =============================================================================
// TESTY INSTRUMENTALNE — Espresso + AndroidJUnit4
// Lokalizacja: app/src/androidTest/java/org/store/narzedziuz/activities/
// =============================================================================

/**
 * Wymagane zależności w build.gradle (app):
 *
 * androidTestImplementation 'androidx.test.ext:junit:1.1.5'
 * androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
 * androidTestImplementation 'androidx.test:runner:1.5.2'
 * androidTestImplementation 'androidx.test:rules:1.5.0'
 * androidTestImplementation 'org.mockito:mockito-android:5.3.1'
 *
 * testImplementation 'junit:junit:4.13.2'
 * testImplementation 'org.mockito:mockito-core:5.3.1'
 * testImplementation 'org.mockito:mockito-inline:5.2.0'
 */
@RunWith(AndroidJUnit4.class)
public class AccelerometerActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<AccelerometerActivity> activityRule =
            new ActivityScenarioRule<>(AccelerometerActivity.class);

    private static final String PREFS_NAME = "ShakePromotionPrefs";

    // =========================================================================
    // SETUP / TEARDOWN
    // =========================================================================

    @Before
    public void clearSharedPrefs() {
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    @After
    public void clearPrefsAfter() {
        Context context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();
    }

    // =========================================================================
    // TESTY UKŁADU (Layout visibility)
    // =========================================================================

    /**
     * Sprawdza, czy po uruchomieniu Activity widoczny jest layoutBefore
     * (przed potrząśnięciem) — gdy brak zapisanego kodu promocyjnego.
     */
    @Test
    public void testInitialLayout_layoutBeforeIsVisible() {
        onView(withId(R.id.layoutBeforeShake))
                .check(matches(isDisplayed()));
    }

    /**
     * Sprawdza, czy layoutAfter jest na starcie UKRYTY.
     */
    @Test
    public void testInitialLayout_layoutAfterIsGone() {
        onView(withId(R.id.layoutAfterShake))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }

    /**
     * Sprawdza, czy ProgressBar startuje z wartością 0.
     */
    @Test
    public void testProgressBar_initialValueIsZero() {
        activityRule.getScenario().onActivity(activity -> {
            ProgressBar progressBar = activity.findViewById(R.id.shakeBar);
            assertEquals(0, progressBar.getProgress());
        });
    }

    /**
     * Sprawdza, czy pole kodu promocyjnego jest na starcie puste.
     */
    @Test
    public void testPromoCodeTextView_initiallyEmpty() {
        activityRule.getScenario().onActivity(activity -> {
            TextView tvPromoCode = activity.findViewById(R.id.tvPromoCode);
            assertEquals("", tvPromoCode.getText().toString());
        });
    }

    // =========================================================================
    // TESTY TOOLBAR / NAWIGACJI
    // =========================================================================

    /**
     * Sprawdza, czy Toolbar jest widoczny.
     */
    @Test
    public void testToolbar_isDisplayed() {
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    /**
     * Sprawdza, czy shakeImage jest widoczny.
     */
    @Test
    public void testShakeImage_isVisible() {
        onView(withId(R.id.shakeImage))
                .check(matches(isDisplayed()));
    }

    /**
     * Sprawdza, czy shakeBar (ProgressBar) jest widoczny.
     */
    @Test
    public void testShakeBar_isVisible() {
        onView(withId(R.id.shakeBar))
                .check(matches(isDisplayed()));
    }

    // =========================================================================
    // TESTY checkExistingPromotion() — wczytywanie z SharedPreferences
    // =========================================================================

    /**
     * Gdy w SharedPreferences zapisany jest dzisiejszy kod, weryfikujemy
     * poprawność odczytu kluczy.
     */
    @Test
    public void testCheckExistingPromotion_withSavedTodayPromo_dataIsReadCorrectly() {
        Context context = ApplicationProvider.getApplicationContext();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String testCode = "SHAKE1234";
        String testProduct = "Wiertarka Bosch";
        String testUserId = "test_user_id";

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("promo_date", todayDate)
                .putString("promo_code", testCode)
                .putString("promo_product", testProduct)
                .putString("promo_user_id", testUserId)
                .commit();

        try (ActivityScenario<AccelerometerActivity> scenario =
                     ActivityScenario.launch(AccelerometerActivity.class)) {
            scenario.onActivity(activity -> {
                SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                assertEquals(testCode, prefs.getString("promo_code", ""));
                assertEquals(testProduct, prefs.getString("promo_product", ""));
                assertEquals(todayDate, prefs.getString("promo_date", ""));
                assertEquals(testUserId, prefs.getString("promo_user_id", ""));
            });
        }
    }

    /**
     * Gdy data w SharedPreferences jest stara (wczoraj), layoutBefore
     * powinien pozostać widoczny — promocja wygasła.
     */
    @Test
    public void testCheckExistingPromotion_withYesterdayPromo_layoutBeforeStaysVisible() {
        Context context = ApplicationProvider.getApplicationContext();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("promo_date", yesterdayDate)
                .putString("promo_code", "SHAKE9999")
                .putString("promo_product", "Stary produkt")
                .putString("promo_user_id", "some_user")
                .commit();

        try (ActivityScenario<AccelerometerActivity> ignored =
                     ActivityScenario.launch(AccelerometerActivity.class)) {
            onView(withId(R.id.layoutBeforeShake))
                    .check(matches(isDisplayed()));
        }
    }

    // =========================================================================
    // TESTY SYMULACJI SHAKE — simulateShakeForTesting()
    // =========================================================================

    /**
     * Symulacja shake'a nie powinna rzucać wyjątku.
     */
    @Test
    public void testSimulateShake_doesNotThrow() {
        activityRule.getScenario().onActivity(activity -> {
            try {
                activity.simulateShakeForTesting();
            } catch (Exception e) {
                fail("simulateShakeForTesting() rzucił wyjątek: " + e.getMessage());
            }
        });
    }

    /**
     * Progres po shake'ach nigdy nie przekracza 100.
     */
    @Test
    public void testProgressBar_doesNotExceed100() throws InterruptedException {
        activityRule.getScenario().onActivity(activity -> {
            for (int i = 0; i < 10; i++) {
                activity.simulateShakeForTesting();
                try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            }
            ProgressBar progressBar = activity.findViewById(R.id.shakeBar);
            assertTrue(
                    "Progres nie powinien przekraczać 100, jest: " + progressBar.getProgress(),
                    progressBar.getProgress() <= 100
            );
        });
    }

    /**
     * Symulacja shake'a zwiększa lub utrzymuje wartość progresu.
     */
    @Test
    public void testSimulateShake_progressIsNonDecreasing() throws InterruptedException {
        activityRule.getScenario().onActivity(activity -> {
            ProgressBar progressBar = activity.findViewById(R.id.shakeBar);
            int before = progressBar.getProgress();
            activity.simulateShakeForTesting();
            try { Thread.sleep(600); } catch (InterruptedException ignored) {}
            int after = progressBar.getProgress();
            assertTrue("Progres powinien być >= poprzedniej wartości", after >= before);
        });
    }

    // =========================================================================
    // TESTY CYKLU ŻYCIA Activity
    // =========================================================================

    /**
     * Activity uruchamia się bez błędu.
     */
    @Test
    public void testActivity_startsSuccessfully() {
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
            assertFalse(activity.isFinishing());
        });
    }

    /**
     * Activity odtwarza się po zmianie konfiguracji (np. obrót ekranu).
     */
    @Test
    public void testActivityRecreation_doesNotCrash() {
        activityRule.getScenario().recreate();
        onView(withId(R.id.layoutBeforeShake))
                .check(matches(isDisplayed()));
    }
}
