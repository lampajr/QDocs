package com.polimi.proj.qdocs.services;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.FilesListActivity;
import com.polimi.proj.qdocs.support.PathResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrea Lamparelli
 *
 * ResultReceiver that has to receive the results provided by the DownloadFile Service
 * and in according to the results has to saveFile the correct activity in order to show the
 * download file.
 *
 * @see ResultReceiver
 * @see com.polimi.proj.qdocs.activities.ScannerActivity
 * @see FilesListActivity
 * @see DownloadFileService
 */

public class SaveFileReceiver extends ResultReceiver {

    private static final String TAG = "DOWNLOAD_FILE_RECEIVER";

    // formats
    private static final List<String> AUDIO_FORMATS =
            new ArrayList<>(Arrays.asList("audio/wav", "audio/mp3"));
    private static final List<String> IMAGE_FORMATS =
            new ArrayList<>(Arrays.asList("image/jpeg", "image/png"));
    private static final List<String> TEXT_FORMATS =
            new ArrayList<>(Arrays.asList("application/pdf", "text/plain"));

    // file types
    private static final String IMAGE = "image";
    private static final String AUDIO = "audio";
    private static final String TEXT = "application";

    // parent context
    private Context context;

    /**
     * Create a new ResultReceive to receive results from the DownloadFileService
     *
     * @param parentContext context of the activity that call this result receiver
     * @param handler  {@link #onReceiveResult} method will be called from the thread
     *                 running handler if given, or from an arbitrary one if null
     */
    public SaveFileReceiver(Context parentContext, Handler handler) {
        super(handler);
        this.context = parentContext;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DownloadFileService.DOWNLOAD_OK && resultData != null) {
            // all goes well
            Log.d(TAG, "Results received from DownloadFileService: OK");
            Uri fileUri = (Uri) resultData.get(DownloadFileService.RESULT_KEY_URI);
            Log.d(TAG, "URI received: " + fileUri.toString());
            String filename = resultData.getString(DownloadFileService.RESULT_KEY_FILENAME);
            Log.d(TAG, "FILENAME received: " + filename);
            String mimeType = resultData.getString(DownloadFileService.RESULT_KEY_EXTENSION);
            Log.d(TAG, "EXTENSION received: " + mimeType);

            // TODO: implement dialog to tell whether user want to see the file or not
            saveFile(fileUri, filename + "." + mimeType.split("/")[1]);
        }
        else {
            // something goes wrong: resultCode == DOWNLOAD_ERROR
            Log.e(TAG, "Results received from DownloadFileService: ERROR" +
                    DownloadFileService.DOWNLOAD_ERROR);

            // TODO: implement error handling
        }
    }

    /**
     * Save the tmp file in the public local directory
     * @param fileUri uri of the file, has to be passed to next activity
     * @param filename name of the file
     */
    private void saveFile(Uri fileUri, String filename) {
        File dst = new File(PathResolver.createPublicDocStorageDir(context).getAbsolutePath(), filename);
        if (!dst.exists()) {
            File src = new File(context.getCacheDir().getAbsolutePath() + "/" + fileUri.getLastPathSegment());
            Log.d(TAG, "exists? -> " + src.exists() + "; path : " + context.getCacheDir().getAbsolutePath() + "/" + fileUri.getLastPathSegment());

            try (InputStream in = new FileInputStream(src)) {
                try (OutputStream out = new FileOutputStream(dst)) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found!! - " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Error during copy of the file!! - " + e.getMessage());
            }
        }
        else {
            Log.d(TAG, "A file with this name already exists.");
            Toast.makeText(context, context.getString(R.string.file_already_stored), Toast.LENGTH_SHORT).show();
        }
    }
}

