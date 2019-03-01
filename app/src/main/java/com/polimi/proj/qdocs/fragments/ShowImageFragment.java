package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ortiz.touchview.TouchImageView;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.ShowFileFragmentActivity;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.io.IOException;
import java.util.Objects;

/**
 * A simple AppCompactActivity that display an image given its uri, type, filename
 */
public class ShowImageFragment extends AppCompatActivity {

    private static final String TAG = "IMAGE FRAGMENT";
    private BitmapDrawable bitmapDrowalbe = null;
    private Uri fileUri = null;
    private String mimeType = null;
    private String fileName = null;

    public ShowImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ShowImageFragment.
     */
    public static ShowImageFragment newInstance() {
        return new ShowImageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_show_image);
        //Take the bitmap from the bundle
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            fileUri = (Uri) bundle.get(DownloadFileService.RESULT_KEY_URI);
            mimeType = bundle.getString(DownloadFileService.RESULT_KEY_EXTENSION);
            fileName = bundle.getString(DownloadFileService.RESULT_KEY_FILENAME);
        }

        Log.d(TAG, "file uri: " + fileUri);
        Log.d(TAG, "mimeType: " + mimeType);

        checkParameter();
        createBitmap();
        Log.d(TAG, "bitmap received");

        //set the bitmap to the tuchImageView
        TouchImageView tuchImageView = findViewById(R.id.touch_image);
        tuchImageView.setImageDrawable(bitmapDrowalbe);


    }

    private void createBitmap() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            bitmapDrowalbe = new BitmapDrawable(getResources(), bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error in showing the image");
        }
    }

    private void checkParameter() {
        if(fileUri != null && mimeType != null && fileName!= null){
            Log.d(TAG, "file URI recived: " + fileUri);
            Log.d(TAG, "mime type recived: " + mimeType);
            Log.d(TAG, "filename type recived: " + fileName);
        }
        else {

            if (fileUri == null) {
                Log.e(TAG, "URI recived is NULL");
            }
            if (mimeType == null) {
                Log.e(TAG, "mime type recived is NULL");
            }
            if (fileName == null){
                Log.d(TAG, "filename recived is NULL ");
            }

            //TODO: handle error

            finish();
        }
    }

}
