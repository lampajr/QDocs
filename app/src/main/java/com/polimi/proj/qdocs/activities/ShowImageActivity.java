package com.polimi.proj.qdocs.activities;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ortiz.touchview.TouchImageView;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.io.IOException;

public class ShowImageActivity extends AppCompatActivity {

    private static final String TAG = "IMAGE FRAGMENT";
    private BitmapDrawable bitmapDrowalbe = null;
    private Uri fileUri = null;
    private String mimeType = null;
    private String fileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

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

        setImage();

        createToolbar();
    }

    /**
     * set the bitmap recived on the tuchImageView
     */
    private void setImage() {
        //set the bitmap to the tuchImageView
        TouchImageView tuchImageView = findViewById(R.id.touch_image);
        tuchImageView.setImageDrawable(bitmapDrowalbe);
    }

    private void createToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);
    }

    /**
     * Creation of the bitmap from the uri
     */
    private void createBitmap() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
            bitmapDrowalbe = new BitmapDrawable(getResources(), bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Error in showing the image");
        }
    }


    /**
     * Check if the parameter fileUri, fileName and mimeType is null. If it is, then the activity is closed
     */
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

            //TODO: print the error

            finish();
        }
    }
}
