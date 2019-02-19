package com.polimi.proj.qdocs.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
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
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.FilesListActivity;

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
 * @see FilesListActivity
 */

public class DownloadFileService extends IntentService {

    private final static String TAG ="DOWNLOAD_FILE_SERVICE";

    // Actions that can be performed by this service
    public static final String ACTION_DOWNLOAD_TMP_FILE =
            "com.polimi.proj.qdocs.services.action.ACTION_DOWNLOAD_TMP_FILE";

    // parameters
    public static final String EXTRA_PARAM_FILENAME =
            "com.polimi.proj.qdocs.services.extra.EXTRA_PARAM_FILENAME";
    public static final String EXTRA_PARAM_RECEIVER =
            "com.polimi.proj.qdocs.services.extra.EXTRA_PARAM_RECEIVER";

    // results data
    public static final String RESULT_KEY_URI =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_URI";
    public static final String RESULT_KEY_EXTENSION =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_EXTENSION";
    public static final String RESULT_KEY_FILENAME =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_FILENAME";

    // results
    public static final int DOWNLOAD_OK = 1;
    public static final int DOWNLOAD_ERROR = -1;

    private FirebaseUser user;
    private File localFile;
    private ResultReceiver receiver;

    public DownloadFileService() {
        super("DownloadFileService");
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            receiver = intent.getParcelableExtra(EXTRA_PARAM_RECEIVER);
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_TMP_FILE.equals(action)) {
                final String filename = intent.getStringExtra(EXTRA_PARAM_FILENAME);
                downloadTmpFile(filename);
            } else {
                Log.e(TAG, "Action CODE wrong, get: " + action);
            }
        }
    }

    /**
     * Create a temporary file in the internal directory
     * @param filename name of the file
     */
    private void downloadTmpFile(final String filename) {

        String[] elements = filename.split("\\.");
        final String name = elements[0];  // name of the file without extension
        final String extension = elements[1]; // get the extension from the whole filename

        // TODO: checks if the fil already exists in the personal directory
        File storageFile = new File(getPublicDocStorageDir().getAbsolutePath(), filename);
        if(!storageFile.exists()) {
            try {
                localFile = File.createTempFile(name, extension, getCacheDir());
                downloadFileFromFilename(filename, extension);
            } catch (IOException e) {
                Log.d(TAG, "Error creating temporary file.");
                e.printStackTrace();
            }
        }
        else {
            Log.d(TAG,"File already stored in " + storageFile.getAbsolutePath());
            localFile = storageFile;
            getBackResults(filename, extension);
        }
    }

    /**
     * Returns the personal directory if exists, null otherwise
     * @return File obj
     */
    private File getPublicDocStorageDir() {
        // Get the directory for the user's public pictures directory.
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), getApplicationContext().getString(R.string.app_name));
    }


    /**
     * Download the file from the FirebaseStorage and stores it into the localFile
     * previously created.
     * @param filename name of the file
     * @param extension extension of the file
     */
    private void downloadFileFromFilename(final String filename, final String extension) {
        Log.d(TAG, "Downloading file: " + filename);

        // TODO: handle FirebaseStorage exceptions

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(user.getUid()).child(filename);

        if (localFile != null) {
            Log.d(TAG, "Local file created: " + localFile.getAbsolutePath());

            // download file
            storageRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG, "MyFile downloaded successfully");
                            getBackResults(filename, extension);
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.e(TAG, "Error occurred during download of " + filename + " from FirebaseStorage");
                }
            });
        }
        else {
            Log.e(TAG, "Temporary file didn't created!");
            sendError();
        }

    }

    private void sendError() {
        receiver.send(DOWNLOAD_ERROR, null);
    }

    /**
     * get back the results to the activity that call it
     * @param extension of the file created
     * result -> fileUri uri of the temporary file created and its MimeType extension
     */
    private void getBackResults(final String filename, final String extension) {
        //Uri fileUri = FileProvider.getUriForFile(getApplicationContext(),
        //        "com.polimi.proj.qdocs.fileprovider",
        //        localFile);

        Uri fileUri = Uri.fromFile(localFile);

        // grant the permission
        grantUriPermission("com.polimi.proj.qdocs", fileUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // get the extension MimeType
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        Bundle resultBundle = new Bundle();
        resultBundle.putParcelable(RESULT_KEY_URI, fileUri);
        resultBundle.putString(RESULT_KEY_FILENAME, filename);
        resultBundle.putString(RESULT_KEY_EXTENSION, mimeType);

        // call the Result Receiver
        receiver.send(DOWNLOAD_OK, resultBundle);
    }
}
