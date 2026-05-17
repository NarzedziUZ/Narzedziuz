package org.store.narzedziuz;

import android.view.View;

import com.google.android.gms.ads.AdView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.store.narzedziuz.activities.MainActivity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class MainActivityAdsTest {

    private MainActivity activity;

    @Before
    public void setUp() {
        // Set up the activity without actually hitting network services for ads if possible,
        // but Robolectric supports basic view inflation for AdView
        activity = Robolectric.buildActivity(MainActivity.class)
                .create()
                .get();
    }

    @Test
    public void testAdView_isPresentAndConfigured() {
        AdView adView = activity.findViewById(R.id.adView);
        
        // Assert that the AdView exists in the layout
        assertNotNull("AdView should be present in MainActivity", adView);
        
        // Ensure it is visible by default (Ads typically load visible)
        assertEquals("AdView should be visible", View.VISIBLE, adView.getVisibility());
    }
}
