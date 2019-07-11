package com.polimi.proj.qdocs.support;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polimi.proj.qdocs.activities.GenericFileActivity;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.activities.PlayAudioActivity;
import com.polimi.proj.qdocs.activities.ShowImageActivity;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.services.SaveFileReceiver;
import com.polimi.proj.qdocs.services.ShowFileReceiver;

import java.util.Calendar;


/**
 * Copyright 2018-2019 Lamparelli Andrea & Chittò Pietro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Utility class
 */
public class Utility {
    private static final String TAG = "UTILITY";

    // file types
    private static final String IMAGE = "image";
    private static final String AUDIO = "audio";
    private static final String TEXT = "application";

    /**
     * Downloads the specific file, if not yet download, and saves it locally
     * @param context activity's context
     * @param pathname pathname of the file to save
     * @param contentType contentType of the file
     */
    public static void startSaveFileService(Context context, String pathname, String contentType) {
        Intent viewerIntentService = new Intent(context, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        SaveFileReceiver receiver = new SaveFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, pathname);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_CONTENT, contentType);
        context.startService(viewerIntentService);
    }

    /**
     * Download the specific file in a temporary one and show it
     * @param context activity's context
     * @param pathname pathname of the file to show
     * @param contentType contentType of the file
     */
    public static void startShowFileService(Context context, final String pathname, String contentType) {
        Intent viewerIntentService = new Intent(context, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        ShowFileReceiver receiver = new ShowFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, pathname);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_CONTENT, contentType);
        context.startService(viewerIntentService);
    }

    /**
     * Trigger the activity that will show the file in the correct way
     * @param context activity's context
     * @param fileUri uri of the file, has to be passed to next activity
     * @param mimeType extension in mimeType format
     * @param extension extension in string format
     */
    public static void showFile(Context context, Uri fileUri, String filename, @NonNull String mimeType, String extension) {
        Intent showFileIntent;

        if (mimeType.contains(IMAGE)) {
            Log.d(TAG, "Instantiating 'show image' activity...");
            showFileIntent = new Intent(context, ShowImageActivity.class);
        }
        else if (mimeType.contains(AUDIO) || mimeType.contains("mpeg") || mimeType.contains("mp4")) {
            Log.d(TAG, "Instantiating 'play audio' activity...");
            showFileIntent = new Intent(context, PlayAudioActivity.class);
        }
        else {
            Log.d(TAG, "Instantiating 'generic' activity...");
            showFileIntent = new Intent(context, GenericFileActivity.class);
        }

        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_URI, fileUri);
        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_MIME_TYPE, mimeType);
        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_EXTENSION, extension);
        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_FILENAME, filename);

        ((MainActivity) context).startActivityForResult(showFileIntent, ShowImageActivity.DELETE_CODE);

    }

    /**
     * Generates a qr code given the key to encode
     * @param key key of the file
     * @return bitmap representing the file's qr code
     */
    public static Bitmap generateQrCode(final String key) {
        try {
            Log.d(TAG, "encoding " + key + " into a new QR Code");
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmapQrCode = barcodeEncoder.encodeBitmap(key, BarcodeFormat.QR_CODE, 400, 400);
            Log.d(TAG, "QR Code generated!");
            return bitmapQrCode;
        } catch (WriterException e) {
            Log.e(TAG, "Error generating qr code: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generate a new code from which provide a new qrcode to
     * associate to a new file
     * @return the code
     */
    public static String generateCode() {
        long time = Calendar.getInstance().getTimeInMillis();
        String code = time + "";// + "" + new Random().nextLong();
        Log.d(TAG, "new code: " + code);
        return code;
    }

    /**
     * Checks the availability of the network
     * @param context calling context
     * @return true if connected, false if not
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
