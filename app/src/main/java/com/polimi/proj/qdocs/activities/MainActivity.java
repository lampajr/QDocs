package com.polimi.proj.qdocs.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.polimi.proj.qdocs.R;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView navigationBar;
    private Fragment currentFragment;
    private FrameLayout mainFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();

        mainFrame = findViewById(R.id.main_frame);

        // get navigation view
        navigationBar = findViewById(R.id.main_navigation_bar);
        setupNavigationBar();

        //TODO: as first create scanner fragment
    }

    /**
     * setup the topmost toolbar
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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
                        //TODO: creates files fragment
                        break;
                    case R.id.scanner_item:
                        //TODO: creates scanner fragment
                        break;
                }
                return false;
            }
        });
    }
}
