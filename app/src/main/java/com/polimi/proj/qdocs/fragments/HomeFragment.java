package com.polimi.proj.qdocs.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.LoginActivity;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";

    private Context context;
    private MainActivity parentActivity;
    private OnHomeFragmentSwipe mSwipeListener;

    private FirebaseUser user;

    private RelativeLayout mainLayout;

    private CircleImageView profileImage;
    private TextView displayName, personalEmail;
    private LinearLayout logoutOption, aboutOption;

    private OnSwipeTouchListener onSwipeTouchListener;


    /**
     * Required empty public constructor
     */
    public HomeFragment() { }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();

        // retain this fragment
        setRetainInstance(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mainLayout = view.findViewById(R.id.main_layout);

        profileImage = view.findViewById(R.id.profile_image);
        displayName = view.findViewById(R.id.display_name);
        personalEmail = view.findViewById(R.id.personal_email);

        logoutOption = view.findViewById(R.id.logout_option);
        setupLogout();
        aboutOption = view.findViewById(R.id.about_option);
        setupAbout();

        setupProfile();
        setupList();
        setupSwipeListener();

        return view;
    }

    private void setupAbout() {
        //TODO: implement about option
    }

    /**
     * setup the logout option
     */
    private void setupLogout() {
        logoutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.logout();
                startLoginActivity();
            }
        });
        ImageView img = logoutOption.findViewById(R.id.option_image);
        //img.setImageResource(R.drawable.ic_logout_24dp);
        TextView textOption = logoutOption.findViewById(R.id.option_text);
        textOption.setText(getString(R.string.logout_string));
    }

    /**
     * start the login Activity
     */
    private void startLoginActivity() {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        startActivity(loginIntent);
        parentActivity.finish();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.parentActivity = (MainActivity) context;
        this.mSwipeListener = (OnHomeFragmentSwipe) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * setup the user profile
     */
    private void setupProfile() {
        final Uri photoUri = user.getPhotoUrl();
        Log.d(TAG, "profile photo uri = " + photoUri);

        SetupProfileImageTask task = new SetupProfileImageTask();
        task.setListener(new SetupProfileImageTask.Listener() {
            @Override
            public void onSuccess(Bitmap profileBitmap) {
                profileImage.setImageBitmap(profileBitmap);
            }
        });
        task.execute(photoUri.toString());

        displayName.setText(user.getDisplayName());
        personalEmail.setText(user.getEmail());
    }

    /**
     * setup the swipe listener in order to change the current fragment
     */
    private void setupSwipeListener() {
        onSwipeTouchListener = new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeBottom() {
                Log.d(TAG, "swipe bottom");
            }

            @Override
            public void onSwipeLeft() {
                mSwipeListener.onHomeSwipe();
            }

            @Override
            public void onSwipeRight() {
                Log.d(TAG, "swipe right");
            }

            @Override
            public void onSwipeTop() {
                Log.d(TAG, "swipe top");
            }
        };

        // TODO: re-add swipe listener
        //mainLayout.setOnTouchListener(onSwipeTouchListener);
    }

    /**
     * setup the action list
     */
    private void setupList() {
        //TODO: create list
    }

    private static class SetupProfileImageTask extends AsyncTask<String, Void, Bitmap> {
        private Listener listener;

        @Override
        protected Bitmap doInBackground(String... uris) {
            return downloadBitmap(uris[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (listener!=null) listener.onSuccess(bitmap);
        }

        /**
         * set listener onSuccess
         * @param listener listener to setup
         */
        void setListener(Listener listener) {
            this.listener = listener;
        }

        /**
         * download the bitmap from uri
         * @param uri from which get the image
         * @return bitmap version of image
         */
        private Bitmap downloadBitmap(String uri)
        {
            Bitmap bm = null;
            InputStream is = null;
            BufferedInputStream bis = null;
            try
            {
                URLConnection conn = new URL(uri).openConnection();
                conn.connect();
                is = conn.getInputStream();
                bis = new BufferedInputStream(is, 8192);
                bm = BitmapFactory.decodeStream(bis);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if (bis != null)
                {
                    try
                    {
                        bis.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            return bm;
        }

        /**
         * represents a listener called on postExecute of the task
         */
        interface Listener {
            void onSuccess(Bitmap profileBitmap);
        }
    }

    /**
     * interface that has to be implemented by the main activity in order to handle
     * the swipe gesture on the HomeFragment
     */
    public interface OnHomeFragmentSwipe {
        void onHomeSwipe();
    }
}
