package org.store.narzedziuz.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.store.narzedziuz.R;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityBarcodeTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Test sprawdza czy przycisk skanera jest klikalny.
     *
     */
    @Test
    public void testScannerButtonClick() {
        onView(withId(R.id.search_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.search_layout)).perform(click());
    }

    /**
     * Test symulujący zachowanie po udanym skanowaniu.
     */
    @Test
    public void testSearchFilteringLogic() {
        String mockScannedCode = "Product123";
        onView(withId(R.id.et_search))
                .perform(replaceText(mockScannedCode));
        onView(withId(R.id.et_search))
                .check(matches(withText(mockScannedCode)));
    }
}