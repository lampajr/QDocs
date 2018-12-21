package com.polimi.proj.qdocs.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrea Lamparelli
 *
 * IntentService that is able to retrieve the file (downloading it from the firebase cloud
 * storage) save it into the cache directory, retrieve its Uri and notify it to the parent
 * Activity which will send it to appropriate activity that will show it.
 *
 * @see android.app.IntentService
 * @see com.polimi.proj.qdocs.activities.ScannerActivity
 * @see com.polimi.proj.qdocs.activities.FileActivity
 */

public class RetrieveFileService extends IntentService {

    private final static String TAG ="RETRIEVE_FILE_SERVICE";

    // Actions that can be performed by this service
    public static final String ACTION_GET_FILE_FROM_KEY = "com.polimi.proj.qdocs.services.action.ACTION_GET_FILE_FROM_KEY";
    public static final String ACTION_GET_FILE_FROM_FILENAME = "com.polimi.proj.qdocs.services.action.ACTION_GET_FILE_FROM_FILENAME";

    // parameters
    public static final String EXTRA_PARAM_KEY = "com.polimi.proj.qdocs.services.extra.EXTRA_PARAM_KEY";
    public static final String EXTRA_PARAM_FILENAME = "com.polimi.proj.qdocs.services.extra.EXTRA_PARAM_FILENAME";

    private FirebaseUser user;
    private File localFile;

    public RetrieveFileService() {
        super("RetrieveFileService");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_FILE_FROM_FILENAME.equals(action)) {
                final String filename = intent.getStringExtra(EXTRA_PARAM_FILENAME);
                getFileFromFilename(filename);
            } else {
                // TODO: handle get uri from filename
            }
        }
    }

    /**
     * Get the Uri of a file given its key detected from the qr code
     * @param key key associated to the file
     */
    private void getFileFromKey(String key) {
        // TODO: check if useful
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Get Uri of a file from its filename, found on the db
     * @param filename name of the file
     */
    private void getFileFromFilename(final String filename) {
        Log.d(TAG, "Starting download of the following file: " + filename);
        String[] elements = filename.split("\\.");
        final String name = elements[0];  // name of the file without extension
        final String extension = elements[1]; // get the extension from the whole filename

        try {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid())
                    .child(filename);
            Log.d(TAG, "Cache dir: " + getCacheDir());
            localFile = File.createTempFile(name, extension, getCacheDir());

            if (localFile != null) Log.d(TAG, "Local file created");

            // download file
            storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "File downloaded successfully");
                    getBackResults(extension);
                }
            }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.e(TAG, "Error occurred during the download of " + filename);
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get back the results to the activity that call it
     * @param extension of the file created
     * result -> fileUri uri of the temporary file created and its MimeType extension
     */
    private void getBackResults(final String extension) {
        Uri fileUri = FileProvider.getUriForFile(getApplicationContext(),
                "com.polimi.proj.qdocs.fileprovider",
                localFile);
        // grant the permission
        grantUriPermission("com.polimi.proj.qdocs", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // get the extension MimeType
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        // TODO: provide the results to the ResultReceiver
    }
}
