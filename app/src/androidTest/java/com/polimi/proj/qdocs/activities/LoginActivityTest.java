package com.polimi.proj.qdocs.activities;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.auth.FirebaseAuth;
import com.polimi.proj.qdocs.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public IntentsTestRule<RegistrationActivity> mActivityRule = new IntentsTestRule<>(RegistrationActivity.class);

    //@Rule
    //public GrantPermissionRule mGrantPermissionRule =
    //       GrantPermissionRule.grant(
    //                "android.permission.CAMERA",
    //                "android.permission.READ_EXTERNAL_STORAGE",
    //                "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void setUp(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        System.out.println("TEST: Check if there is an user logged");
        if(mAuth.getCurrentUser() != null) {
            //finish();
            mAuth.signOut();
            System.out.println("TEST: perform signOut");
        }
        else
            System.out.println("TEST: No user logged");

    }

    @Test
    public void clickSignUpButton_OpenRegistrationUI() throws Exception{
        onView(withId(R.id.sign_up)).check(matches(isDisplayed()));
        onView(withId(R.id.sign_up)).perform(click());
        intended(hasComponent(RegistrationActivity.class.getName()));

        //onView(withId(R.id.submit_button)).check(matches(isDisplayed()));

    }

    @Test
    public void correct_signin() throws  Exception{
        String email = "tester@qdocs.it";
        String psw = "pippo5";

        onView(withId(R.id.email_text)).check(matches(isDisplayed())).perform(replaceText(email));
        onView(withId(R.id.password_text)).check(matches(isDisplayed())).perform(replaceText(psw));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click());


    }

    @Test
    public void wrong_signin() throws  Exception{
        String email = "tester@qdocs.it";
        String psw = "pippo";

        onView(withId(R.id.email_text)).check(matches(isDisplayed())).perform(replaceText(email));
        onView(withId(R.id.password_text)).check(matches(isDisplayed())).perform(replaceText(psw));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click());

    }




    /*
    @Test
    public void loginActivityTest() {

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

        ViewInteraction button = onView(
                allOf(withId(R.id.submit_button),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        button.check(matches(isDisplayed()));

        ViewInteraction button2 = onView(
                allOf(withId(R.id.submit_button),
                        childAtPosition(
                                allOf(withId(R.id.error_label),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        button2.check(matches(isDisplayed()));
    }*/

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
