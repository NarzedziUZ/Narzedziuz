package org.store.narzedziuz.utils;

package org.store.narzedziuz.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import org.store.narzedziuz.R;
import org.store.narzedziuz.repositories.CartRepository;
import org.store.narzedziuz.callbacks.OnCartLoaded;
import org.store.narzedziuz.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class CartWidgetProvider extends AppWidgetProvider {

    public static void updateWidget(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(
                new ComponentName(context, CartWidgetProvider.class)
        );

        if (ids != null && ids.length > 0) {
            new CartWidgetProvider().onUpdate(context, manager, ids);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        for (int appWidgetId : appWidgetIds) {

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_cart);

            if (userId == null) {
                views.setTextViewText(R.id.widgetText, "NarzedziUZ przypomina: Największy wybór narzedzi w najlepszych cenach!");
                appWidgetManager.updateAppWidget(appWidgetId, views);
                continue;
            }

            CartRepository.getInstance().getCart(userId, new OnCartLoaded() {
                @Override
                public void onSuccess(List<CartItem> items) {

                    int totalItems = 0;
                    for (CartItem item : items) {
                        totalItems += item.getQuantity();
                    }

                    String text;
                    if (totalItems == 0) {
                        text = "NarzedziUZ przypomina: Wytrząśnij swój rabat z ShakeUZem!";
                    } else {
                        text = "NarzedziUZ przypomina: Ilość produktów w Twoim koszyku to: " + totalItems + ", nie zwlekaj i dokończ zakupy!";
                    }

                    views.setTextViewText(R.id.widgetText, text);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }

                @Override
                public void onFailure(Exception e) {
                    views.setTextViewText(R.id.widgetText, "Błąd ładowania koszyka");
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }
            });
        }
    }
}
