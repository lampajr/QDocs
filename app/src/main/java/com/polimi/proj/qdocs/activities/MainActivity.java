package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.fragments.FilesListFragment;
import com.polimi.proj.qdocs.fragments.HomeFragment;
import com.polimi.proj.qdocs.fragments.ScannerFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomeFragment.OnHomeFragmentSwipe, FilesListFragment.OnFilesFragmentSwipe, ScannerFragment.OnScannerFragmentSwipe {

    private static final String TAG = "MAIN_ACTIVITY";

    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private static final int REQUEST_FILES_PERMISSION = 20;
    private boolean cameraPermissionGranted;
    private boolean filesPermissionGranted;

    private static final int HOME_ID = 0, SCANNER_ID = 1, FILES_ID = 2;
    private static final String SCANNER_TAG = "scanner", FILES_TAG = "files", HOME_TAG = "home";

    private BottomNavigationView navigationBar;
    private Fragment currentFragment;
    private FrameLayout mainFrame;

    private List<Fragment> fragments;

    private int currentFragmentId;

    private FirebaseUser user;

    //TODO: create fragments only once!!!!!!!!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFrame = findViewById(R.id.main_frame);

        setupFragments();

        //checks whether there is already a fragment in the transaction
        Fragment pastFrag = getPastFragment();
        if (pastFrag != null) {
            Log.d(TAG, "there already exists a fragment.. load it");
            currentFragment = pastFrag;
        }

        // get navigation view
        navigationBar = findViewById(R.id.main_navigation_bar);
        setupNavigationBar();

        cameraPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        filesPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupFragments() {
        fragments = new ArrayList<>();
        Fragment homeFrag = HomeFragment.newInstance();
        fragments.add(homeFrag);
        Fragment scannerFrag = ScannerFragment.newInstance(getIntent());
        fragments.add(scannerFrag);
        Fragment filesFrag = FilesListFragment.newInstance();
        fragments.add(filesFrag);
    }

    /**
     * checks whether in the restarted activity there already exists a fragment
     * if yer retrieve it and return
     * @return the fragment or null
     */
    private Fragment getPastFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment res;
        if ((res=manager.findFragmentByTag(FILES_TAG)) != null) return res;
        if ((res=manager.findFragmentByTag(HOME_TAG)) != null) return res;
        if ((res=manager.findFragmentByTag(SCANNER_TAG)) != null) return res;
        return null;
    }

    /**
     * setup the bottom navigation bar that allows user to switch
     * between scanner layout and files list layout
     */
    private void setupNavigationBar() {
        navigationBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.files_item:
                        if (filesPermissionGranted)
                            applyFragment(FILES_ID, FILES_TAG);
                        break;
                    case R.id.scanner_item:
                        applyFragment(SCANNER_ID, SCANNER_TAG);
                        break;
                    case R.id.home_item:
                        applyFragment(HOME_ID, HOME_TAG);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * apply the fragment to the main layout
     */
    private void applyFragment(int id, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // get the corresponding fragment
        Fragment fr = fragments.get(id);
        if (fr != null && id != currentFragmentId) {
            if (currentFragment != null) {
                transaction.remove(currentFragment);
            }
            transaction.add(R.id.main_frame, fr, tag);
            transaction.commit();
            currentFragmentId = id;
            currentFragment = fr;
        }
    }

    /**
     * initialize the activity, creating the user and checking the permission
     * and if all goes well, it applies the scanner fragment as first fragment
     */
    private void initialization() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // if the user is not yet authenticated
            startLoginActivity();
        }

        if (cameraPermissionGranted && filesPermissionGranted) {
            // as first fragment create the scanner fragment
            if (currentFragment == null)
                applyFragment(SCANNER_ID, SCANNER_TAG);
        }
        else if (!cameraPermissionGranted)requestCameraPermission();
        else requestFilesPermission();
    }

    /**
     * request the camera permission
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    /**
     * request read
     */
    private void requestFilesPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_FILES_PERMISSION);
    }

    /**
     * start the login Activity
     */
    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialization();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                cameraPermissionGranted = true;
                initialization();
            }
            else {
                Log.d(TAG, "Camera permission denied.. exit application");
                cameraPermissionGranted = false;
                finish();
            }
        }
        else if (requestCode == REQUEST_FILES_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Files permission granted");
                filesPermissionGranted = true;
                //TODO: do something if needed
            }
            else {
                Log.d(TAG, "Files permission denied.. you cannot access files list page!");
                filesPermissionGranted = false;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onFilesSwipe() {
        //TODO: open Scanner fragment
        applyFragment(SCANNER_ID, SCANNER_TAG);
        navigationBar.setSelectedItemId(R.id.scanner_item);
    }

    @Override
    public void onScannerSwipeLeft() {
        //TODO: open Home fragment
        applyFragment(HOME_ID, HOME_TAG);
        navigationBar.setSelectedItemId(R.id.home_item);
    }

    public void onScannerSwipeRight() {
        //TODO: open FilesList fragment
        applyFragment(FILES_ID, FILES_TAG);
        navigationBar.setSelectedItemId(R.id.files_item);
    }

    @Override
    public void onHomeSwipe() {
        //TODO: open Scanner fragment
        applyFragment(SCANNER_ID, SCANNER_TAG);
        navigationBar.setSelectedItemId(R.id.scanner_item);
    }
}
