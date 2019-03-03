package com.polimi.proj.qdocs.fragments;

import android.content.Context;
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
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {
    private static final String TAG = "HOME_FRAGMENT";

    private Context context;

    private FirebaseUser user;

    private ListView personalList;
    private CircleImageView profileImage;
    private TextView displayName, personalEmail;



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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        personalList = view.findViewById(R.id.personal_list);
        profileImage = view.findViewById(R.id.profile_image);
        displayName = view.findViewById(R.id.display_name);
        personalEmail = view.findViewById(R.id.personal_email);

        setupProfile();
        setupList();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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
}
