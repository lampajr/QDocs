package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.fragments.HomeFragment;
import com.polimi.proj.qdocs.fragments.OfflineFilesFragment;
import com.polimi.proj.qdocs.fragments.RecentFilesFragment;
import com.polimi.proj.qdocs.fragments.ScannerFragment;
import com.polimi.proj.qdocs.fragments.StorageFragment;
import com.polimi.proj.qdocs.support.Utility;

import java.util.ArrayList;
import java.util.List;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";

    public static final String SECRET_FILE = ".010secret.txt";

    private static final int REQUEST_CAMERA_PERMISSION = 10;
    private static final int REQUEST_FILES_PERMISSION = 20;
    private boolean cameraPermissionGranted;
    private boolean filesPermissionGranted;

    private static final int HOME_ID = 0, OFFLINE_ID = 1, SCANNER_ID = 2,
            RECENT_ID = 3, STORAGE_ID = 4;

    private AHBottomNavigation navigationBar;
    private AHBottomNavigationAdapter navigationAdapter;
    private int prevFragmentIdx = -1;

    private ViewPager pager;

    private List<Fragment> fragments;

    private int offlineCount = 0;

    private boolean isWiFi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupConnectionListener();

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
                if (prevFragmentIdx==STORAGE_ID) {
                    Log.d(TAG, "Removing file from storage view");
                    StorageFragment fr = (StorageFragment) fragments.get(STORAGE_ID);
                    fr.onDeleteFromFile(filename);
                }
                if (prevFragmentIdx==RECENT_ID) {
                    Log.d(TAG, "Removing file from recent files view");
                    RecentFilesFragment fr = (RecentFilesFragment) fragments.get(RECENT_ID);
                    fr.onDeleteFromFile(filename);
                }
                else if (prevFragmentIdx==OFFLINE_ID) {
                    // OfflineFragment
                    Log.d(TAG, "Removing file from offline files view");
                    OfflineFilesFragment fr = (OfflineFilesFragment) fragments.get(OFFLINE_ID);
                    fr.onDeleteFromFile(filename);
                }
                else if (prevFragmentIdx==SCANNER_ID) {
                    // ScannerFragment
                    Log.d(TAG, "Removing file from offline files view");
                    ScannerFragment fr = (ScannerFragment) fragments.get(SCANNER_ID);
                    fr.onDeleteFromFile(filename);
                }
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupConnectionListener() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {
                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                        if (networkInfo.isConnected())
                            isWiFi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                    }

                    @Override
                    public void onLost(Network network) {
                        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                        isWiFi = !(networkInfo.getType() == ConnectivityManager.TYPE_WIFI);

                        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected())
                            Toast.makeText(MainActivity.this, getString(R.string.connection_lost), Toast.LENGTH_SHORT).show();
                    }
                }
        );
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
                navigationBar.setCurrentItem(i);
                navigationBar.restoreBottomNavigation();
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
     * set the notification to a specific item
     * @param count notification count
     * @param position item position
     */
    public void setNotification(int count, int position) {
        offlineCount += count;
        navigationBar.setNotification(offlineCount + "", position);
    }

    public void resetNotification(int position) {
        offlineCount = 0;
        navigationBar.setNotification("", position);
    }

    /**
     * setup the bottom navigation bar that allows user to switch
     * between scanner layout and files list layout
     */
    private void setupNavigationBar() {

        navigationBar.setForceTint(true);

        navigationBar.setDefaultBackgroundColor(getColor(R.color.colorPrimary));
        navigationBar.setAccentColor(getColor(R.color.colorAccent));
        navigationBar.setInactiveColor(getColor(R.color.white));

        navigationBar.setBehaviorTranslationEnabled(true);

        navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.main_navigation_bar_menu);
        navigationAdapter.setupWithBottomNavigation(navigationBar);

        navigationBar.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                switch (navigationAdapter.getMenuItem(position).getItemId()) {
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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

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
            }
            else {
                Log.d(TAG, "Files permission denied.. you cannot access files list page!");
                filesPermissionGranted = false;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Hides the Bottom Navigation View
     */
    public void hideBottomNavigationBar() {
        navigationBar.hideBottomNavigation();
    }

    /**
     * Restore the Bottom Navigation View
     */
    public void restoreBottomNavigationBar() {
        navigationBar.restoreBottomNavigation();
    }

    /**
     * Check the state of the Bottom Navigation View
     * @return true if it is hidden, false otherwise
     */
    public boolean navigationBarIsHidden() {
        return navigationBar.isHidden();
    }


    public boolean isWiFiAvailable() {
        return isWiFi;
    }

    /**
     *  View Pager Adapter used for swiping the screens
     */
    private class SwipePagerAdapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragmentList = fragments;

        SwipePagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @NonNull
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
