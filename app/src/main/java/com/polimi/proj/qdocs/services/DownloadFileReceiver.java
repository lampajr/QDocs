package com.polimi.proj.qdocs.services;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.polimi.proj.qdocs.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrea Lamparelli
 *
 * ResultReceiver that has to receive the results provided by the DownloadFile Service
 * and in according to the results has to dispatch the correct activity in order to show the
 * download file.
 *
 * @see ResultReceiver
 * @see com.polimi.proj.qdocs.activities.ScannerActivity
 * @see com.polimi.proj.qdocs.activities.FileActivity
 * @see DownloadFileService
 */

public class DownloadFileReceiver extends ResultReceiver {

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
     *                 running handler if given, or an arbitrary one otherwise
     */
    public DownloadFileReceiver(Context parentContext, Handler handler) {
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
            String mimeType = resultData.getString(DownloadFileService.RESULT_KEY_EXTENSION);
            Log.d(TAG, "EXTENSION received: " + mimeType);


            // TODO: implement dispatcher in according to the extension
            dispatch(fileUri, mimeType);
        }
        else {
            // something goes wrong: resultCode == DOWNLOAD_ERROR
            Log.e(TAG, "Results received from DownloadFileService: ERROR" +
                    DownloadFileService.DOWNLOAD_ERROR);

            // TODO: implement error handling
        }
    }

    /**
     * Activities dispatcher, in according to the extension (mimeType)
     * starts the appropriate activity
     * @param fileUri uri of the file, has to be passed to next activity
     * @param mimeType extension in mimeType format
     */
    private void dispatch(Uri fileUri, String mimeType) {
        String type = mimeType.split("/")[0];
        if (AUDIO_FORMATS.contains(mimeType)) {
            // TODO: starting play audio activity
        }
        else if (IMAGE_FORMATS.contains(mimeType)) {
            // TODO: starting show image activity
        }
        else if (TEXT_FORMATS.contains(mimeType)) {
            // TODO: startinf show text file activity
        }
        else {
            Log.e(TAG, "File format handling not yet implemented!!");
            Toast.makeText(context, context.getString(R.string.format_not_recognized),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
