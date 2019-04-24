package com.polimi.proj.qdocs.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.ortiz.touchview.TouchImageView;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadFileService;
import java.io.IOException;
import java.util.Objects;

public class ShowImageActivity extends AppCompatActivity {

    public static final int DELETE_CODE = 100;
    public static final String FILE_NAME = "fileName";
    private final String TAG = "IMAGE FRAGMENT";
    private BitmapDrawable bitmapDrowalbe = null;
    private Uri fileUri = null;
    private String mimeType = null;
    private String fileName = null;
    private String extension = null;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        //Take the bitmap from the bundle
        Bundle bundle = getIntent().getExtras();
        readParameter(bundle);
        checkParameter();
        createBitmap();
        Log.d(TAG, "bitmap received");
        setImage();
        setupToolbar();
    }

    @Override
    protected void onStop() {
        super.onStop();
        toolbar.getNavigationIcon().setTint(getResources().getColor(R.color.black));

    }

    private void readParameter(Bundle bundle) {
        if (bundle != null) {
            fileUri = (Uri) bundle.get(DownloadFileService.RESULT_KEY_URI);
            mimeType = bundle.getString(DownloadFileService.RESULT_KEY_MIME_TYPE);
            fileName = bundle.getString(DownloadFileService.RESULT_KEY_FILENAME);
            extension = bundle.getString(DownloadFileService.RESULT_KEY_EXTENSION);
        }

        Log.d(TAG, "file uri: " + fileUri);
        Log.d(TAG, "mimeType: " + mimeType);
    }

    /**
     * set the bitmap recived on the tuchImageView
     */
    private void setImage() {
        //set the bitmap to the tuchImageView
        TouchImageView tuchImageView = findViewById(R.id.touch_image);
        tuchImageView.setImageDrawable(bitmapDrowalbe);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.black));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Objects.requireNonNull(toolbar.getNavigationIcon()).setTint(getResources().getColor(R.color.white));
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
            Log.d(TAG, "extension recived: " + extension);
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
            if(extension == null){
                Log.e(TAG,"The extension recived is NULL");
            }

            //TODO: print the error

            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.logout_menu:
                LoginActivity.logout();
                startLoginActivity();
                break;

            case R.id.delete_option:
                Intent data = new Intent();
                data.putExtra(FILE_NAME, fileName+"."+extension);
                setResult(DELETE_CODE, data);
                Log.d(TAG, "file deleted: " + fileName);
                finish();
        }
        return false;
    }

    /**
     * start the login Activity
     */
    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }
}
