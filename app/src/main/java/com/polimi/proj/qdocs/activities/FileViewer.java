package com.polimi.proj.qdocs.activities;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.polimi.proj.qdocs.R;

public class FileViewer extends AppCompatActivity {

    private DatabaseReference dbRef;
    private Bundle bundle;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);

        bundle = getIntent().getExtras();
        filename = bundle.getString(ScannerActivity.FILENAME_KEY);
    }
}
