package com.polimi.proj.qdocs.activities;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.polimi.proj.qdocs.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class MainActivityTest {

    private static FirebaseAuth mAuth;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE");


    /**
     * The test user is logged in
     */
    @BeforeClass
    public static void setUp(){
        String email = "tester@qdocs.it";
        String password = "pippo5";
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                    Log.d("TEST", "tester logged");
                else
                    Log.d("TEST", "login fail");
            }
        });
        sleep(5000);
    }

    /**
     * The test user is logged out
     */
    @AfterClass
    public static void tearDown(){
        mAuth.signOut();
        sleep(3000);
        if(mAuth.getCurrentUser() == null)
            Log.d("TEST", "tester logged out");
        else
            Log.d("TEST", "logout fail");

    }

    @Before
    public void before(){
        sleep(1000);
    }


    /**
     * when the bottom navigation's home button is clicked, the home fragment is displayed
     */
    @Test
    public void navigation_bar_home_fragment(){

        click_home_nb();

        sleep(1000);

        onView(allOf(withId(R.id.title), withText(R.string.home))).check(matches(isDisplayed()));
    }


    /**
     * when the bottom navigation's offline button is clicked, the offline fragment is displayed
     */
    @Test
    public void navigation_bar_offline_fragment(){

        click_offline_nb();

        sleep(1000);

        onView(allOf(withId(R.id.title), withText((R.string.offline_string)))).check(matches(isDisplayed()));
    }


    /**
     * when the bottom navigation's scanner button is clicked, the scanner fragment is displayed
     */
    @Test
    public void navigation_bar_scanner_fragment(){

        click_scanner_nb();

        sleep(1000);

        onView(withId(R.id.barcode_view)).check(matches(isDisplayed()));
    }


    /**
     * when the bottom navigation's recent button is clicked, the recent fragment is displayed
     */
    @Test
    public void navigation_bar_recent_fragment(){

        click_recent_nb();

        sleep(1000);

        onView(allOf(withId(R.id.title), withText((R.string.recent_string)))).check(matches(isDisplayed()));
    }


    /**
     * when the bottom navigation's storage button is clicked, the storage fragment is displayed
     */
    @Test
    public void navigation_bar_storage_fragment(){

        click_storage_nb();

        sleep(1000);

        onView(allOf(withId(R.id.title), withText((R.string.storage)))).check(matches(isDisplayed()));
    }


    private static void sleep(int millisecond){
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * perform a click on the home button in navigation bar
     */
    private static void click_home_nb(){

        onView(allOf(withId(R.id.bottom_navigation_small_container),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.main_navigation_bar),
                                1),
                        0),
                isDisplayed()))
                .perform(click());

    }

    /**
     * perform a click on the offline button in navigation bar
     */
    private static void click_offline_nb(){

        onView(allOf(withId(R.id.bottom_navigation_small_container),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.main_navigation_bar),
                                1),
                        1),
                isDisplayed()))
                .perform(click());

    }

    /**
     * perform a click on the scanner button in navigation bar
     */
    private static void click_scanner_nb(){

        onView(allOf(withId(R.id.bottom_navigation_small_container),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.main_navigation_bar),
                                1),
                        2),
                isDisplayed()))
                .perform(click());

    }

    /**
     * perform a click on the recent button in navigation bar
     */
    private static void click_recent_nb(){

        onView(allOf(withId(R.id.bottom_navigation_small_container),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.main_navigation_bar),
                                1),
                        3),
                isDisplayed()))
                .perform(click());

    }

    /**
     * perform a click on the home button in navigation bar
     */
    private static void click_storage_nb(){

        onView(allOf(withId(R.id.bottom_navigation_small_container),
                childAtPosition(
                        childAtPosition(
                                withId(R.id.main_navigation_bar),
                                1),
                        4),
                isDisplayed()))
                .perform(click());

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