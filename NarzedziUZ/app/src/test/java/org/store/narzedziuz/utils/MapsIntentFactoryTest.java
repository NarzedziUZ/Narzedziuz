package org.store.narzedziuz.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.store.narzedziuz.R;

@RunWith(RobolectricTestRunner.class)
public class MapsIntentFactoryTest {

    @Test
    public void createGoogleMapsIntent_setsActionDataAndPackage() {
        String query = ApplicationProvider.getApplicationContext()
                .getString(R.string.stores_query_pattern);

        Intent intent = MapsIntentFactory.createGoogleMapsIntent(query);

        assertThat(intent.getAction(), is(Intent.ACTION_VIEW));
        assertThat(intent.getData(), is(Uri.parse("geo:0,0?q=" + Uri.encode(query))));
        assertThat(intent.getPackage(), is("com.google.android.apps.maps"));
    }

    @Test
    public void createFallbackIntent_setsActionAndDataWithoutPackage() {
        String query = ApplicationProvider.getApplicationContext()
                .getString(R.string.stores_query_pattern);

        Intent intent = MapsIntentFactory.createFallbackIntent(query);

        assertThat(intent.getAction(), is(Intent.ACTION_VIEW));
        assertThat(intent.getData(), is(Uri.parse("geo:0,0?q=" + Uri.encode(query))));
        assertThat(intent.getPackage(), is((String) null));
    }
}
