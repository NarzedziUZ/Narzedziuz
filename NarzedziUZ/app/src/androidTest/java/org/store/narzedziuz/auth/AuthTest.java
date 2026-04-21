package org.store.narzedziuz.auth;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.store.narzedziuz.R;
import org.store.narzedziuz.activities.LoginActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AuthTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    // Sprawdzenie czy pojawiają się błędy przy pustych polach
    public void login_emptyFields_showsErrors() {
        onView(withId(R.id.btn_login)).perform(click());

        onView(withId(R.id.et_email)).check(matches(hasErrorText("Podaj adres e-mail")));

        onView(withId(R.id.et_email)).perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(click());

        onView(withId(R.id.et_password)).check(matches(hasErrorText("Podaj hasło")));
    }

    @Test
    // Przejście do ekranu rejestracji i sprawdzenie czy puste pola wyrzucają błąd
    public void navigateToRegister_andCheckEmptyFields() {
        onView(withId(R.id.tv_register)).perform(click());

        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_register)).perform(click());

        onView(withId(R.id.et_first_name)).check(matches(hasErrorText("Imię jest wymagane")));
    }

    @Test
    // Sprawdzenie niezgodności haseł przy rejestracji
    public void register_passwordsDoNotMatch_showsError() {
        onView(withId(R.id.tv_register)).perform(click());

        onView(withId(R.id.et_first_name)).perform(typeText("Jan"), closeSoftKeyboard());
        onView(withId(R.id.et_last_name)).perform(typeText("Kowalski"), closeSoftKeyboard());
        onView(withId(R.id.et_email)).perform(typeText("jan@kowalski.pl"), closeSoftKeyboard());
        onView(withId(R.id.et_password)).perform(typeText("Haslo123!"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password)).perform(typeText("InneHaslo123!"), closeSoftKeyboard());

        onView(withId(R.id.btn_register)).perform(click());

        onView(withId(R.id.et_confirm_password)).check(matches(hasErrorText("Hasła się nie zgadzają")));
    }
}
