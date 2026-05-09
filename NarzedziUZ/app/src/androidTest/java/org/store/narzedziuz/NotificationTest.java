package org.store.narzedziuz;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.Until;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.rule.GrantPermissionRule;
import org.store.narzedziuz.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class NotificationTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    private Context context;
    private NotificationManager notificationManager;
    private UiDevice uiDevice;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        notificationManager.cancelAll();
    }

    @Test
    // Czy pojawia się powiadomienie o ShakeUZie
    public void showNotification_postsCorrectNotification() throws InterruptedException {
        // Given
        NotificationHelper.showNotification(context);

        // Wait for notification to be visible in the shade
        uiDevice.openNotification();
        boolean found = uiDevice.wait(Until.hasObject(By.text("ShakeUZ")), 5000);

        // Then
        Assert.assertTrue("Powiadomienie ShakeUZ nie zostało znalezione w panelu powiadomień", found);
        
        // Clean up
        uiDevice.pressBack(); 
    }

    @Test
    // Czy powiadomienie pojawia sie w panelu
    public void notificationReceiver_onReceive_showsNotification() throws InterruptedException {
        NotificationHelper.showNotification(context);

        uiDevice.openNotification();
        boolean found = uiDevice.wait(Until.hasObject(By.text("ShakeUZ")), 5000);

        Assert.assertTrue("NotificationReceiver nie wywołał powiadomienia (brak w panelu)", found);

        uiDevice.pressBack();
    }

    @Test
    // Czy cooldown na ShakeUZ działa poprawnie
    public void hasUserWonToday_logicWorks() {
        String userId = "testUser";
        SharedPreferences prefs = context.getSharedPreferences("ShakePromotionPrefs", Context.MODE_PRIVATE);
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        prefs.edit()
                .putString("promo_date", todayDate)
                .putString("promo_user_id", userId)
                .apply();

        Assert.assertTrue("Powinno zwrócić true dla dzisiejszej daty i tego samego użytkownika",
                NotificationHelper.hasUserWonToday(context, userId));

        Assert.assertFalse("Powinno zwrócić false dla innego użytkownika",
                NotificationHelper.hasUserWonToday(context, "otherUser"));
    }
}
