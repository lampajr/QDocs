package com.polimi.proj.qdocs.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;

import java.io.File;
import java.io.IOException;

public class FileViewer extends AppCompatActivity {

    private static final String TAG = "FILE_VIEWER";

    private DatabaseReference dbRef;
    private FirebaseUser user;
    private Bundle bundle;
    private String wholeFilename;
    private String filename;
    private String extension;
    private StorageReference storageRef;
    private File localFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        bundle = getIntent().getExtras();
        wholeFilename = bundle.getString(ScannerActivity.FILENAME_KEY);
    }

    private void downloadFile() {
        Log.d(TAG, "Starting download of the following file: " + wholeFilename);
        String[] elements = wholeFilename.split("\\.");
        filename = elements[0];  // name of the file
        extension = elements[1]; // get the extension from the whole wholeFilename
        try {
            storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid())
                    .child(wholeFilename);
            localFile = File.createTempFile(filename, extension);

            // download file
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "File downloaded successfully");
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.e(TAG, "Error occurred during the download of " + wholeFilename);
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        downloadFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
