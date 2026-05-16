package org.store.narzedziuz.utils;

import android.content.Intent;
import android.net.Uri;

public final class MapsIntentFactory {

    private MapsIntentFactory() {
    }

    public static Intent createGoogleMapsIntent(String query) {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        return intent;
    }

    public static Intent createFallbackIntent(String query) {
        Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
        return new Intent(Intent.ACTION_VIEW, uri);
    }
}
