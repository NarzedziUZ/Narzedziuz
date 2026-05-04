package org.store.narzedziuz.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationHelper.scheduleDailyNotification(context);
        } else {
            NotificationHelper.showNotification(context);
            // Reschedule for next day
            NotificationHelper.scheduleDailyNotification(context);
        }
    }
}
