package com.polimi.proj.qdocs.activities;
/**
 * Activity that take a uri file and its type and display the correct fragment.
 * The fragment take care about showing the file
 */

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.fragments.PlayAudioFragment;
import com.polimi.proj.qdocs.fragments.ShowImageFragment;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.io.File;
import java.io.IOException;

public class ShowFileFragmentActivity extends FragmentActivity implements ShowImageFragment.OnFragmentInteractionListener, PlayAudioFragment.OnFragmentInteractionListener {

    private static final String TAG = "SHOW_FILE_FRAG_ACT";
    private static final String IMAGE = "image";
    private static final String AUDIO = "audio";

    private Uri fileUri;
    private String mimeType;
    private String fileName;
    private Fragment fragment;
    private BitmapDrawable bitmapDrawable;
    private Uri audioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        android.widget.Toolbar toolbar = findViewById(R.id.toolbar_widget);
        this.setActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.ic_toolbar_arrow);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        closeFragment();
                        finish();
                    }
                }

        );

        Bundle bundle = getIntent().getExtras();
        fileUri = (Uri) bundle.get(DownloadFileService.RESULT_KEY_URI);
        mimeType = bundle.getString(DownloadFileService.RESULT_KEY_EXTENSION);
        fileName = bundle.getString(DownloadFileService.RESULT_KEY_FILENAME);

        Log.d(TAG, "file uri: " + fileUri);
        Log.d(TAG, "mimeType: " + mimeType);
        
        checkParameter();

        setFragment();
    }

    /**
     * Choose the fragment to display
     */
    private void setFragment() {

        String type = mimeType.split("/")[0];

        switch (type){

            case IMAGE:
                Log.d(TAG, "Insantiating 'show image' fragment...");
                fragment = ShowImageFragment.newInstance();
                displayFragment(fragment);
                onShowImageFragmentInteraction(fileUri);
                break;

            case AUDIO:
                Log.d(TAG, "Insantiating 'play audio' fragment...");
                fragment = PlayAudioFragment.newInstance();
                displayFragment(fragment);
                onPlayAudioFragmentInteraction(fileUri);
                break;
        }

    }

    private void displayFragment(Fragment fragment) {
        //TODO: display the fragment
        // Get the FragmentManager and start a transaction.
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();

        // Add the Fragment.
        fragmentTransaction.add(R.id.fragment_container,
                fragment).addToBackStack(null).commit();

        Log.d(TAG,"Fragment changed");
    }

    public void closeFragment() {
        // Get the FragmentManager.
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Check to see if the fragment is already showing.
        Fragment fragment = (Fragment) fragmentManager
                .findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            // Create and commit the transaction to remove the fragment.
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment).commit();
        }
    }

    private void checkParameter() {
        if(fileUri != null && mimeType != null){
            Log.d(TAG, "file URI recived: " + fileUri);
            Log.d(TAG, "mime type recived: " + mimeType);
        }
        else {

            if (fileUri == null) {
                Log.e(TAG, "URI recived is NULL");
            }
            if (mimeType == null) {
                Log.e(TAG, "mime type recived is NULL");
            }

            //TODO: handle error

            finish();
        }
    }

    @Override
    public void onShowImageFragmentInteraction(Uri uri) {

        //File file = new File(uri.getPath());
        try {


            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

        } catch (IOException e) {
            Log.e(TAG, "Error in showing the image");
        }
    }

    public BitmapDrawable getImageResult(){
        return bitmapDrawable;
    }

    public Uri getAudioUri() {return audioUri;}

    public String getFileName() {return fileName;}

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "Creazione menu");
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.file_menu_layout, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.logout_menu:
                LoginActivity.logout();
                Log.d(TAG, "Log out");

                final Intent scanner = new Intent(ShowFileFragmentActivity.this, ScannerActivity.class);
                scanner.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(scanner);
                break;
        }
        return false;
    }

    @Override
    public void onBackPressed(){
        closeFragment();
        finish();
    }

    @Override
    public void onPlayAudioFragmentInteraction(Uri uri) {
        audioUri = fileUri;
    }
}
