package com.polimi.proj.qdocs.services;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.dialogs.ProgressBarDialog;
import com.polimi.proj.qdocs.fragments.StorageFragment;
import com.polimi.proj.qdocs.support.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrea Lamparelli
 *
 * ResultReceiver that has to receive the results provided by the DownloadFile Service
 * and in according to the results has to showFile the correct activity in order to show the
 * download file.
 *
 * @see ResultReceiver
 * @see com.polimi.proj.qdocs.fragments.ScannerFragment
 * @see StorageFragment
 * @see DownloadFileService
 */

public class ShowFileReceiver extends ResultReceiver {

    private static final String TAG = "DOWNLOAD_FILE_RECEIVER";
    private final String AUTHORITY = "com.polimi.proj.qdocs.fileprovider";

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

    private ProgressBarDialog progressBar;

    /**
     * Create a new ResultReceive to receive results from the DownloadFileService
     *
     * @param parentContext context of the activity that call this result receiver
     * @param handler  {@link #onReceiveResult} method will be called from the thread
     *                 running handler if given, or from an arbitrary one if null
     */
    public ShowFileReceiver(Context parentContext, Handler handler) {
        super(handler);
        this.context = parentContext;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if ((resultCode == DownloadFileService.DOWNLOAD_OK || resultCode == DownloadFileService.ALREADY_STORED) && resultData != null) {
            // all goes well
            Log.d(TAG, "Results received from DownloadFileService: OK");
            Uri fileUri = (Uri) resultData.get(DownloadFileService.RESULT_KEY_URI);
            Log.d(TAG, "URI received: " + (fileUri != null ? fileUri.toString() : null));
            String filename = resultData.getString(DownloadFileService.RESULT_KEY_FILENAME);
            Log.d(TAG, "FILENAME received: " + filename);
            String mimeType = resultData.getString(DownloadFileService.RESULT_KEY_MIME_TYPE);
            Log.d(TAG, "MIME TYPE received: " + mimeType);
            String extension = resultData.getString(DownloadFileService.RESULT_KEY_EXTENSION);
            Log.d(TAG, "EXTENSION received: " + extension);


            if (mimeType != null) {
                Utility.showFile(context, fileUri, filename, mimeType, extension);
            }
            else {
                Toast.makeText(context, context.getString(R.string.unable_show_file), Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == DownloadFileService.START_DOWNLOAD && resultData != null) {
            progressBar = new ProgressBarDialog(context, null,
                    context.getString(resultData.getInt(DownloadFileService.RESULT_KEY_TITLE)));
            progressBar.show();
        }
        else if (resultCode == DownloadFileService.SET_PROGRESS && resultData != null) {
            float value = resultData.getFloat(DownloadFileService.RESULT_KEY_PROGRESS);
            progressBar.setProgress(value);
        }
        else if (resultCode == DownloadFileService.DOWNLOAD_ERROR){
            Log.d(TAG, "Failure occurred during download");
            Toast.makeText(context, context.getString(R.string.error_download), Toast.LENGTH_SHORT).show();
            if (progressBar.isShowing())
                progressBar.dismiss();
        }
    }
}
