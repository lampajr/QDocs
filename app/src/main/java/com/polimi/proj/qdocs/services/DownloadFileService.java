package com.polimi.proj.qdocs.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.dialogs.ProgressBarDialog;
import com.polimi.proj.qdocs.fragments.StorageFragment;
import com.polimi.proj.qdocs.support.PathResolver;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrea Lamparelli
 * @author Chittò Pietro
 *
 * IntentService that is able to retrieve the file (downloading it from the firebase cloud
 * storage) save it into the cache directory, retrieve its Uri and notify it to the parent
 * Activity which will send it to appropriate activity that will show it.
 *
 * @see android.app.IntentService
 * @see com.polimi.proj.qdocs.fragments.ScannerFragment
 * @see StorageFragment
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
    public static final String EXTRA_PARAM_CONTENT =
            "com.polimi.proj.qdocs.services.extra.EXTRA_PARAM_CONTENT";

    // results data
    public static final String RESULT_KEY_URI =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_URI";
    public static final String RESULT_KEY_MIME_TYPE =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_MIME_TYPE";
    public static final String RESULT_KEY_FILENAME =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_FILENAME";
    public static final String RESULT_KEY_EXTENSION =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_EXTENSION";
    public static final String RESULT_KEY_PROGRESS =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_PROGRESS";
    public static final String RESULT_KEY_TITLE =
            "com.polimi.proj.qdocs.services.extra.RESULT_KEY_TITLE";

    // results
    public static final int DOWNLOAD_OK = 1;
    public static final int DOWNLOAD_ERROR = -1;
    public static final int START_DOWNLOAD = 2;
    public static final int SET_PROGRESS = 3;

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
                final String contentType = intent.getStringExtra(EXTRA_PARAM_CONTENT);
                downloadTmpFile(filename, contentType);
            } else {
                Log.e(TAG, "Action CODE wrong, get: " + action);
            }
        }
    }

    /**
     * Create a temporary file in the internal directory
     * @param pathname name of the file
     */
    private void downloadTmpFile(final String pathname, final String contentType) {
        Log.w(TAG, pathname);
        String[] pathElements = pathname.split("/");
        String[] elements = pathElements[pathElements.length-1].split("\\.");
        final String filename = elements[0];  // filename of the file without extension
        String extension = contentType.split("/")[1];
        /*
        if (elements.length == 2) {
            extension = elements[1]; // get the extension from the whole pathname
        }
        else {
            extension = contentType.split("/")[1];
        }*/

        // TODO: checks if the fil already exists in the personal directory
        File storageFile = new File(PathResolver.getPublicDocFileDir(getApplicationContext()).getAbsolutePath(), filename + "." + extension);
        if(!storageFile.exists()) {
            try {
                localFile = File.createTempFile(filename, extension, getCacheDir());
                downloadFileFromFilename(pathname, filename, extension);
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
     * Download the file from the FirebaseStorage and stores it into the localFile
     * previously created.
     * @param pathname name of the file
     * @param extension extension of the file
     */
    private void downloadFileFromFilename(final String pathname, final String filename, final String extension) {
        Log.d(TAG, "Downloading file: " + pathname);

        // TODO: handle FirebaseStorage exceptions

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child(user.getUid()).child(pathname);

        if (localFile != null) {
            Log.d(TAG, "Local file created: " + localFile.getAbsolutePath());

            Bundle resultBundle = new Bundle();
            resultBundle.putString(RESULT_KEY_TITLE, "Downlaoding file..");
            receiver.send(START_DOWNLOAD, resultBundle);

            // download file
            storageRef.getFile(localFile).addOnSuccessListener(
                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bundle resultBundle = new Bundle();
                            resultBundle.putFloat(RESULT_KEY_PROGRESS, 100f);
                            receiver.send(SET_PROGRESS, resultBundle);
                            Log.d(TAG, "MyFile downloaded successfully: onSuccess");
                            getBackResults(filename, extension);
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                @Override
                public void onCanceled() {
                    Log.e(TAG, "Error occurred during download of " + pathname + " from FirebaseStorage");
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    final int progress = (int)((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                    Log.d(TAG, "PROGRESS -> " + progress);
                    Bundle resultBundle = new Bundle();
                    resultBundle.putFloat(RESULT_KEY_PROGRESS, progress);
                    receiver.send(SET_PROGRESS, resultBundle);
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
        resultBundle.putString(RESULT_KEY_EXTENSION, extension);
        resultBundle.putString(RESULT_KEY_MIME_TYPE, mimeType);

        // call the Result Receiver
        receiver.send(DOWNLOAD_OK, resultBundle);
    }
}
