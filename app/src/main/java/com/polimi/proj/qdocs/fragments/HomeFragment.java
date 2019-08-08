package com.polimi.proj.qdocs.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.LoginActivity;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.AboutDialog;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.model.MyFile;
import com.polimi.proj.qdocs.model.StorageElement;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Copyright 2018-2019 Lamparelli Andrea & Chittò Pietro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";

    private static final String EN = "en", IT = "it";

    private Context context;
    private MainActivity parentActivity;

    private FirebaseUser user;
    private FirebaseHelper fbHelper;

    private CircleImageView profileImage;
    private TextView displayName, personalEmail;
    private TextView logoutOption, aboutOption;

    private RadioButton italianButton, englishButton;
    private String prevLang = null;

    private TextView totalSpaceView, storedFilesView;
    private int storedFiles = 0;
    private long totalSpace = 0;

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
    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = FirebaseAuth.getInstance().getCurrentUser();

        fbHelper = new FirebaseHelper();
        prevLang = prevLang == null ? Locale.getDefault().getLanguage() : prevLang;

        // retain this fragment
        setRetainInstance(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RelativeLayout mainLayout = view.findViewById(R.id.main_layout);

        profileImage = view.findViewById(R.id.profile_image);
        displayName = view.findViewById(R.id.display_name);
        personalEmail = view.findViewById(R.id.personal_email);

        logoutOption = view.findViewById(R.id.logout);
        aboutOption = view.findViewById(R.id.about);

        totalSpaceView = view.findViewById(R.id.space_used_text_view);
        storedFilesView = view.findViewById(R.id.stored_files_text_view);

        italianButton = view.findViewById(R.id.italian_button);
        englishButton = view.findViewById(R.id.english_button);

        if (prevLang.equals(IT))
            italianButton.setChecked(true);
        else englishButton.setChecked(true);

        RelativeLayout titlebar = view.findViewById(R.id.titlebar);
        TextView titleText = titlebar.findViewById(R.id.title);
        titleText.setText(getString(R.string.home));

        setupLanguageOption();

        setupProfile();

        setupAboutOption();
        setupLogoutOption();

        computeInfos();

        return view;
    }

    /**
     * Updates the total space used and the number of stored files information
     */
    private void computeInfos() {
        Log.d(TAG, "Setting value event listener on the Firebase db");
        totalSpace = 0;
        storedFiles = 0;

        final DatabaseReference ref = fbHelper.getDatabaseReference();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadAllFiles(dataSnapshot);

                String size = "";
                if (totalSpace > 1000) {
                    long sizeMb = totalSpace / 1000;
                    size = sizeMb + " Mb";
                }
                else {
                    size = totalSpace + " Kb";
                }

                String space = totalSpaceView.getText().toString().substring(0, totalSpaceView.getText().toString().lastIndexOf(":") + 1) + " " + size;
                totalSpaceView.setText(space);

                String number = storedFilesView.getText().toString().substring(0, storedFilesView.getText().toString().lastIndexOf(":") + 1) + " " + storedFiles;
                storedFilesView.setText(number);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    /**
     * load all the files in the current datasnapshot
     * @param dataSnapshot current datasnapshot analysed
     */
    private void loadAllFiles(DataSnapshot dataSnapshot) {
        for (DataSnapshot ds: dataSnapshot.getChildren()) {
            if (StorageElement.isFile(ds)) {
                MyFile f = ds.getValue(MyFile.class);
                if (f != null && f.getFilename() != null && !f.getFilename().equals(MainActivity.SECRET_FILE)) {
                    // increase the number of stored files by one
                    storedFiles += 1;

                    // increase the space used
                    if (!f.getSize().equals(""))
                        totalSpace += Long.parseLong(f.getSize()) / 1000;
                }
            }
            else {
                loadAllFiles(ds);
            }
        }
    }

    private void setupAboutOption() {
        aboutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AboutDialog(context, null).show();
            }
        });

        aboutOption.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(parentActivity, R.drawable.ic_info_black_24dp),
                null, null, null);

        aboutOption.setText(getString(R.string.about));
    }

    private void setupLanguageOption() {
        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, Locale.getDefault().getDisplayLanguage().toLowerCase());
                if (englishButton.isChecked() && !prevLang.equals(EN)) {
                    prevLang = EN;
                    updateLanguage(EN);
                    parentActivity.recreate();
                }
            }
        });

        italianButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, Locale.getDefault().getDisplayLanguage().toLowerCase());
                if (italianButton.isChecked() && !prevLang.equals(IT)) {
                    prevLang = IT;
                    updateLanguage(IT);
                    parentActivity.recreate();
                }
            }
        });
    }

    private void updateLanguage(String language) {
        Resources res = context.getResources();
        // Change locale settings in the app.
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(language.toLowerCase()));
        res.updateConfiguration(conf, dm);
    }

    /**
     * setup the logout option
     */
    private void setupLogoutOption() {
        logoutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.logout();
                startLoginActivity();
            }
        });

        logoutOption.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(parentActivity, R.drawable.ic_logout),
                null, null, null);

        logoutOption.setText(getString(R.string.logout_string));

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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.parentActivity = (MainActivity) context;
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

        if (photoUri != null) {
            Log.d(TAG, "profile photo uri = " + photoUri);

            SetupProfileImageTask task = new SetupProfileImageTask();
            task.setListener(new SetupProfileImageTask.Listener() {
                @Override
                public void onSuccess(Bitmap profileBitmap) {
                    profileImage.setImageBitmap(profileBitmap);
                }
            });

            task.execute(photoUri.toString());
        }
        else {
            profileImage.setImageDrawable(context.getDrawable(R.drawable.ic_001_account));
        }

        displayName.setText(user.getDisplayName());
        personalEmail.setText(user.getEmail());
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
}
