package com.polimi.proj.qdocs.activities;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.espressoAssertion.RecyclerViewItemCountAssertion;
import com.polimi.proj.qdocs.fragments.StorageFragment;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.support.Utility;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StorageFragmentTest {

    private static FirebaseAuth mAuth;
    private static StorageReference storageRef;
    private static final String KEY_METADATA = "key_metadata";
    private static final String UID_METADATA = "uid_metadata";
    private static FirebaseHelper fbHelper;

    @Rule
    public IntentsTestRule<MainActivity> mActivityTestRule = new IntentsTestRule<>(MainActivity.class);

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
        sleep(2000);
        storageRef = FirebaseStorage.getInstance().getReference()
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

        fbHelper = new FirebaseHelper();
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
        storageRef = null;
        fbHelper = null;

    }

    @Before
    public void before(){
        click_storage_nb();
        savePickedImage();
        sleep(3000);
    }

    @Test
    public void add_image(){
        intending(hasAction(Intent.ACTION_PICK)).respondWith(getImageResult());
        //intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(getImageResult());
        onView(withId(R.id.upload_button)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.upload_image)).check(matches(isDisplayed())).perform(click());
        sleep(5000);
        onView(allOf(withText("picked_image"), isCompletelyDisplayed())).check(matches(isDisplayed()));
        storageRef.child("picked_image.jpeg").delete();
    }

    @Test
    public void delete_file(){

        upload_file_on_fb();

        int list_lenght = getCountFromRecyclerView(R.id.storage_view);

        //click on the option of the picked_image file
        onView(allOf(withId(R.id.element_options),
                isCompletelyDisplayed(),
                withParent(allOf(withId(R.id.content),
                        withChild(allOf(withId(R.id.element_info),
                                withChild(allOf(withId(R.id.element_name),
                                        withText("picked_image")))
                                )))))).perform(click());

        //click on delete file in the bottom sheet menu
        onView(allOf(withId(R.id.delete_option),
                isCompletelyDisplayed()))
                .perform(click());

        //click on yes in the dialog
        onView(withId(R.id.yes_button)).perform(click());

        sleep(5000);

        //check the lenght of the ricycle view
        onView(allOf(withId(R.id.storage_view), isCompletelyDisplayed()))
                .check(new RecyclerViewItemCountAssertion((list_lenght - 1)));
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

    private Instrumentation.ActivityResult getImageResult(){
        Intent resultData = new Intent();
        File dir = mActivityTestRule.getActivity().getExternalCacheDir();
        File file = new File(dir.getPath(), "picked_image.jpeg");
        Uri uri = Uri.fromFile(file);
        assertNotNull(uri);
        //resultData.putExtra("uri", uri);
        resultData.setData(uri);
        Log.d("TEST", "result image intent created");
        assertNotNull(resultData);
        return new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
    }

    private void savePickedImage(){
        Bitmap bm = BitmapFactory.decodeResource(mActivityTestRule.getActivity().getResources(), R.drawable.image_fragment_background);
        assertNotNull(bm);
        File dir = mActivityTestRule.getActivity().getExternalCacheDir();
        File file = new File(dir.getPath(), "picked_image.jpeg");
        Log.d("TEST", "image created at " + file.getAbsolutePath());
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private void upload_file_on_fb(){
        File dir = mActivityTestRule.getActivity().getExternalCacheDir();
        File file = new File(dir.getPath(), "picked_image.jpeg");
        Uri uri = Uri.fromFile(file);
        Log.d("TEST", "file uri to delete: " + uri);

        String contentType = "image/jpeg";

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata(KEY_METADATA, Utility.generateCode())
                .setCustomMetadata(UID_METADATA, fbHelper.getUserId())
                .build();

        storageRef.child(uri.getLastPathSegment()).putFile(uri, metadata);
        sleep(10000);

    }

    public static int getCountFromRecyclerView(@IdRes int RecyclerViewId) {
        final int[] COUNT = {0};
        Matcher matcher = new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                COUNT[0] = ((RecyclerView) item).getAdapter().getItemCount();
                return true;
            }
            @Override
            public void describeTo(Description description) {
            }
        };
        onView(allOf(withId(RecyclerViewId),isDisplayed())).check(matches(matcher));
        return COUNT[0];
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
