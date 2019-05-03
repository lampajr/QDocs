package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.fragments.StorageFragment;
import com.polimi.proj.qdocs.fragments.HomeFragment;
import com.polimi.proj.qdocs.fragments.OfflineFilesFragment;
import com.polimi.proj.qdocs.fragments.RecentFilesFragment;
import com.polimi.proj.qdocs.fragments.ScannerFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private static final int REQUEST_FILES_PERMISSION = 20;
    private boolean cameraPermissionGranted;
    private boolean filesPermissionGranted;

    private static final int HOME_ID = 0, OFFLINE_ID = 1, SCANNER_ID = 2,
            RECENT_ID = 3, STORAGE_ID = 4;

    /*
    private static final String SCANNER_TAG = "scanner", STORAGE_TAG = "files",
            HOME_TAG = "home", OFFLINE_TAG = "offline", RECENT_TAG = "recent";

    private Map<String, Integer> fragmentsMap = new HashMap<String, Integer>()
    {{
        put(HOME_TAG, HOME_ID);
        put(OFFLINE_TAG, OFFLINE_ID);
        put(SCANNER_TAG, SCANNER_ID);
        put(RECENT_TAG, RECENT_ID);
        put(STORAGE_TAG, STORAGE_ID);
    }};*/

    private BottomNavigationView navigationBar;
    private int prevFragmentIdx = -1;

    private ViewPager pager;

    private List<Fragment> fragments;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get Pager
        pager = findViewById(R.id.main_frame);
        // get Navigation view
        navigationBar = findViewById(R.id.main_navigation_bar);

        setupFragments();
        setupPager();
        setupNavigationBar();

        cameraPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        filesPermissionGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "Request code received: " + requestCode);
        if (requestCode == ShowImageActivity.DELETE_CODE) {
            if (data != null) {
                Log.d(TAG, "Delete operation received from file");
                String filename = data.getStringExtra(ShowImageActivity.FILE_NAME);
                StorageFragment fr = (StorageFragment) fragments.get(STORAGE_ID);
                fr.onDeleteFromFile(filename);
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Setup the fragments used by the MainActivity:
     * - Home
     * - Offline Files
     * - Scanner
     * - Recent Files
     * - Storage
     */
    private void setupFragments() {
        fragments = new ArrayList<>();
        Fragment homeFrag = HomeFragment.newInstance();
        fragments.add(homeFrag);
        Fragment offlineFrag = OfflineFilesFragment.newInstance();
        fragments.add(offlineFrag);
        Fragment scannerFrag = ScannerFragment.newInstance(getIntent());
        fragments.add(scannerFrag);
        Fragment recentFrag = RecentFilesFragment.newInstance();
        fragments.add(recentFrag);
        Fragment filesFrag = StorageFragment.newInstance();
        fragments.add(filesFrag);
    }

    /**
     * Setup the ViewPager adding its own PagerAdapter
     */
    private void setupPager() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                prevFragmentIdx = i;
                navigationBar.getMenu().getItem(i).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        SwipePagerAdapter pagerAdapter = new SwipePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(5);
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
                    case R.id.home_item:
                        changePage(HOME_ID);
                        return true;
                    case R.id.offline_item:
                        if (filesPermissionGranted) {
                            changePage(OFFLINE_ID);
                            return true;
                        }
                        break;
                    case R.id.scanner_item:
                        changePage(SCANNER_ID);
                        return true;
                    case R.id.recent_item:
                        if (filesPermissionGranted) {
                            changePage(RECENT_ID);
                            return true;
                        }
                        break;
                    case R.id.files_item:
                        if (filesPermissionGranted) {
                            changePage(STORAGE_ID);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });
    }

    /**
     * Change the current page, saving this as previous one
     */
    private void changePage(int idx) {
        prevFragmentIdx = idx;
        pager.setCurrentItem(idx);
    }

    /**
     * initialize the activity, creating the user and checking the permission
     * and if all goes well, it applies the scanner fragment as first fragment,
     * otherwise it starts with the last fragment used
     */
    private void initialize() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // if the user is not yet authenticated
            startLoginActivity();
        }

        if (cameraPermissionGranted && filesPermissionGranted) {
            // as first fragment create the scanner fragment
            if (prevFragmentIdx == -1) {
                changePage(SCANNER_ID);
            }
            else changePage(prevFragmentIdx);
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
        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                cameraPermissionGranted = true;
                initialize();
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

    /**
     *  View Pager Adapter used for swiping the screens
     */
    private class SwipePagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = fragments;

        SwipePagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }
}
