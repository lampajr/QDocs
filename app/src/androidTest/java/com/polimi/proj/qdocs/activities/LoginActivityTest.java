package com.polimi.proj.qdocs.activities;


import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.auth.FirebaseAuth;
import com.polimi.proj.qdocs.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private FirebaseAuth mAuth;

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);


    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE");

    @Before
    public void setUp() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            //finish();
            mAuth.signOut();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @After
    public void end() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            //finish();
            mAuth.signOut();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void clickSignUpButton_OpenRegistrationUI() throws Exception {

        onView(withId(R.id.sign_up)).check(matches(isDisplayed())).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.submit_button)).check(matches(isDisplayed()));

    }

    @Test
    public void correct_signin() throws Exception {
        //System.out.println("TEST correct signin");
        String email = "tester@qdocs.it";
        String psw = "pippo5";

        onView(withId(R.id.email_text)).check(matches(isDisplayed())).perform(replaceText(email));
        onView(withId(R.id.password_text)).check(matches(isDisplayed())).perform(replaceText(psw));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click());

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction viewGroup = onView(withId(R.id.main_navigation_bar));
        viewGroup.check(matches(isDisplayed()));

    }

    @Test
    public void wrong_signin() throws Exception {
        String email = "tester@qdocs.it";
        String psw = "pippo";

        onView(withId(R.id.email_text)).check(matches(isDisplayed())).perform(replaceText(email));
        onView(withId(R.id.password_text)).check(matches(isDisplayed())).perform(replaceText(psw));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed())).perform(click());

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.email_text)).check(matches(isDisplayed()));
        onView(withId(R.id.password_text)).check(matches(isDisplayed()));
        onView(withId(R.id.submit_button)).check(matches(isDisplayed()));

    }
}