package org.store.narzedziuz;

import android.content.Intent;
import android.net.Uri;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLooper;
import org.store.narzedziuz.activities.ProductDetailActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ProductDetailActivityTest {

    private ProductDetailActivity activity;

    @Before
    public void setUp() {
        Intent intent = new Intent(RuntimeEnvironment.getApplication(), ProductDetailActivity.class);
        intent.putExtra("productId", "testProductId");
        activity = Robolectric.buildActivity(ProductDetailActivity.class, intent)
                .create()
                .start()
                .resume()
                .get();
    }

    @Test
    public void testFindInStoresButton_launchesMapsIntent() {
        // Since loadProduct loads async or might just require the loop to idle
        ShadowLooper.idleMainLooper();

        Button btnFindInStores = activity.findViewById(R.id.btn_find_in_stores);
        assertNotNull("Find in stores button should not be null", btnFindInStores);

        btnFindInStores.performClick();

        // Let the main looper process the Intent request
        ShadowLooper.idleMainLooper();

        // Get the shadow activity to inspect launched intents
        ShadowActivity shadowActivity = Shadows.shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        // Assert that an intent was launched
        assertNotNull("Should launch an intent for maps", startedIntent);

        // Assert the intent action and URI
        assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
        Uri data = startedIntent.getData();
        assertNotNull("Intent data should not be null", data);
        assertTrue("URI should be a geo URI", data.toString().startsWith("geo:0,0?q="));
    }
}
