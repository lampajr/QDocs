package com.polimi.proj.qdocs.activities;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.polimi.proj.qdocs.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class RegistrationActivity {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void registrationActivity() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.sign_up), withText("SignUp"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatButton.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.email_text),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.email_text),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("cia"), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.email_text), withText("cia"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("ciao"));

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.email_text), withText("ciao"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText4.perform(closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.submit_button), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.label_error), withText("Please insert a password"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        textView.check(matches(withText("Please insert a password")));

        ViewInteraction appCompatEditText5 = onView(
                allOf(withId(R.id.email_text), withText("ciao"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText5.perform(click());

        ViewInteraction appCompatEditText6 = onView(
                allOf(withId(R.id.email_text), withText("ciao"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText6.perform(click());

        ViewInteraction appCompatEditText7 = onView(
                allOf(withId(R.id.email_text), withText("ciao"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText7.perform(replaceText(""));

        ViewInteraction appCompatEditText8 = onView(
                allOf(withId(R.id.email_text),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText8.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText9 = onView(
                allOf(withId(R.id.email_text),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText9.perform(click());

        pressBack();

        ViewInteraction appCompatEditText10 = onView(
                allOf(withId(R.id.password_text),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText10.perform(replaceText("dgik"), closeSoftKeyboard());

        ViewInteraction appCompatEditText11 = onView(
                allOf(withId(R.id.password_text), withText("dgik"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText11.perform(pressImeActionButton());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(R.id.submit_button), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.label_error), withText("Please insert the email"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        textView2.check(matches(withText("Please insert the email")));

        ViewInteraction appCompatEditText12 = onView(
                allOf(withId(R.id.email_text),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText12.perform(replaceText("tester@qqdocs"), closeSoftKeyboard());

        ViewInteraction appCompatEditText13 = onView(
                allOf(withId(R.id.email_text), withText("tester@qqdocs"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText13.perform(click());

        ViewInteraction appCompatEditText14 = onView(
                allOf(withId(R.id.email_text), withText("tester@qqdocs"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText14.perform(replaceText("tester@qdocs"));

        ViewInteraction appCompatEditText15 = onView(
                allOf(withId(R.id.email_text), withText("tester@qdocs"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText15.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText16 = onView(
                allOf(withId(R.id.email_text), withText("tester@qdocs"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText16.perform(click());

        ViewInteraction appCompatEditText17 = onView(
                allOf(withId(R.id.email_text), withText("tester@qdocs"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText17.perform(replaceText("tester@qdocs.it"));

        ViewInteraction appCompatEditText18 = onView(
                allOf(withId(R.id.email_text), withText("tester@qdocs.it"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText18.perform(closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatButton4 = onView(
                allOf(withId(R.id.submit_button), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton4.perform(click());

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.label_error), withText("The password must be at least 6 characters"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        textView3.check(matches(withText("The password must be at least 6 characters")));

        ViewInteraction appCompatEditText19 = onView(
                allOf(withId(R.id.password_text), withText("dgik"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText19.perform(replaceText("dgik123"));

        ViewInteraction appCompatEditText20 = onView(
                allOf(withId(R.id.password_text), withText("dgik123"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText20.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText21 = onView(
                allOf(withId(R.id.password_text), withText("dgik123"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText21.perform(pressImeActionButton());

        ViewInteraction appCompatButton5 = onView(
                allOf(withId(R.id.submit_button), withText("Register"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        appCompatButton5.perform(click());

        ViewInteraction textView4 = onView(
                allOf(withId(R.id.label_error), withText("email already in use"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                2),
                        isDisplayed()));
        textView4.check(matches(withText("email already in use")));

        ViewInteraction appCompatEditText22 = onView(
                allOf(withId(R.id.password_text), withText("dgik123"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText22.perform(click());

        ViewInteraction appCompatEditText23 = onView(
                allOf(withId(R.id.password_text), withText("dgik123"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText23.perform(replaceText("dgoooo"));

        ViewInteraction appCompatEditText24 = onView(
                allOf(withId(R.id.password_text), withText("dgoooo"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText24.perform(closeSoftKeyboard());

        ViewInteraction appCompatEditText25 = onView(
                allOf(withId(R.id.password_text), withText("dgoooo"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        appCompatEditText25.perform(pressImeActionButton());

        ViewInteraction appCompatEditText26 = onView(
                allOf(withId(R.id.email_text), withText("tester@qdocs.it"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText26.perform(replaceText("tester@qdocs.comcom\n"));

        ViewInteraction appCompatEditText27 = onView(
                allOf(withId(R.id.email_text), withText("tester@qdocs.comcom\n"),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatEditText27.perform(closeSoftKeyboard());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
