package com.polimi.proj.qdocs.activities;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class RegistrationActivityTest {

    private FirebaseAuth mAuth;
    private String newemail = "test1@qdocs.it";
    private String newpassword = "tester1";

    @Rule
    public ActivityTestRule<RegistrationActivity> mActivityTestRule = new ActivityTestRule<>(RegistrationActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.CAMERA",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE");


    /*
    If the new user is been created, then the setup delete it from firebase
     */
    @Before
    public void setup(){
        AuthCredential credential = EmailAuthProvider.getCredential(newemail, newpassword);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("TEST", user.toString());
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Log.d("TEST", "user deleted");
                            }
                        });
                    }
                    else

                        Log.d("TEST", "user is null");

                }
            });
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /*
    Delete the user that is been created from tests
     */
    @After
    public void teardown(){
        AuthCredential credential = EmailAuthProvider.getCredential(newemail, newpassword);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Log.d("TEST", user.toString());
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Log.d("TEST", "user deleted");
                            }
                        });
                    }
                    else

                        Log.d("TEST", "user is null");

                }
            });
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void correct_registration(){


        onView(withId(R.id.email_text)).perform(replaceText(newemail));
        onView(withId(R.id.password_text)).perform(replaceText(newpassword));
        onView(withId(R.id.submit_button)).perform(click());

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction viewGroup = onView(withId(R.id.main_navigation_bar));
        viewGroup.check(matches(isDisplayed()));

    }


    @Test
    public void invalid_email(){

        String invalidEmail = "12345";
        onView(withId(R.id.email_text)).perform(replaceText(invalidEmail));
        onView(withId(R.id.password_text)).perform(replaceText(newpassword));
        onView(withId(R.id.submit_button)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.label_error)).check(matches(withText(R.string.error_invalid_email)));

    }

    @Test
    public  void email_already_used(){

        String oldemail = "tester@qdocs.it";
        onView(withId(R.id.email_text)).perform(replaceText(oldemail));
        onView(withId(R.id.password_text)).perform(replaceText(newpassword));
        onView(withId(R.id.submit_button)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.label_error)).check(matches(withText(R.string.email_already_used)));

    }


    @Test
    public  void invalid_password() {

        onView(withId(R.id.email_text)).perform(replaceText(newemail));
        String invalidpassword = "123";
        onView(withId(R.id.password_text)).perform(replaceText(invalidpassword));
        onView(withId(R.id.submit_button)).perform(click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.label_error)).check(matches(withText(R.string.invalid_password_registration)));
    }


}