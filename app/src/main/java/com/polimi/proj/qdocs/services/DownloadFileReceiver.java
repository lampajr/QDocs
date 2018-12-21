package com.polimi.proj.qdocs.services;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class DownloadFileReceiver extends ResultReceiver {

    private static final String TAG = "DOWNLOAD_FILE_RECEIVER";

    private Context context;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param parentContext context of the activity that call this result receiver
     * @param handler
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
        }
        else {
            // something goes wrong: resultCode == DOWNLOAD_ERROR
            Log.e(TAG, "Results received from DownloadFileService: ERROR" + DownloadFileService.DOWNLOAD_ERROR);

            // TODO: implement error handling
        }
    }
}
