package com.polimi.proj.qdocs.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileViewer extends AppCompatActivity {

    // TODO: convert to SERVICE!!!! and in according to the file start the appropriate activity
    // TODO: copy into RetrieveFile

    private static final String TAG = "FILE_VIEWER";

    private DatabaseReference dbRef;
    private FirebaseUser user;
    private Bundle bundle;
    private String wholeFilename;
    private String filename;
    private String extension;
    private StorageReference storageRef;
    private File localFile;
    private static final int REQUEST_SHOW_FILE = 10;

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
            Log.d(TAG, "Cache dir: " + getCacheDir());
            localFile = File.createTempFile(filename, extension, getCacheDir());

            if (localFile != null) Log.d(TAG, "Local file created");

            // download file
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "File downloaded successfully");
                    showFile();
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

    /**
     * show the file downloaded in the temporary location
     */
    private void showFile() {
        Intent showIntent = new Intent(Intent.ACTION_VIEW);

        Uri fileUri = FileProvider.getUriForFile(getApplicationContext(),
                "com.polimi.proj.qdocs.fileprovider",
                localFile);

        // grant the permission
        grantUriPermission("com.polimi.proj.qdocs", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // get the extension type
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        Log.d(TAG, "Showing file: " + fileUri + " -- " + "." + mimeType);
        //showIntent.setAction(Intent.ACTION_VIEW);
        showIntent.setDataAndType(fileUri, mimeType);
        startActivityForResult(showIntent, REQUEST_SHOW_FILE);
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
