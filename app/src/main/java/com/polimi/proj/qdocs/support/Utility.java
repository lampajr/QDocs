package com.polimi.proj.qdocs.support;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

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
 * @author Lamparelli Andrea
 * @author Chittò Pietro
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
     * Generate a new BottomSheet menu given the following parameters
     * @param activity parent activity that will show the menu
     * @param title title of the menu
     * @param menuId resource id of the menu to inflate
     * @param listener listener on the menu items click
     * @return new object, NB: need to be showed
     */
    /*public static BottomSheet.Builder generateBottomSheetMenu(AppCompatActivity activity,
                                                              String title,
                                                              int menuId,
                                                              MenuItem.OnMenuItemClickListener listener) {
        return new BottomSheet.Builder(activity, R.style.bottom_sheet_style)
                .title(title)
                .sheet(menuId)
                .listener(listener);
    }*/

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
    public static void showFile(Context context, Uri fileUri, String filename, String mimeType, String extension) {

        String type = "";
        if (mimeType != null) {
            type = mimeType.split("/")[0];
        }
        Intent showFileIntent;

        switch (type){

            case IMAGE:
                Log.d(TAG, "Instantiating 'show image' activity...");
                showFileIntent = new Intent(context, ShowImageActivity.class);
                break;

            case AUDIO:
                Log.d(TAG, "Instantiating 'play audio' activity...");
                showFileIntent = new Intent(context, PlayAudioActivity.class);
                break;

            default:
                Log.d(TAG, "Instantiating 'generic' activity...");
                showFileIntent = new Intent(context, GenericFileActivity.class);
                break;

        }

        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_URI, fileUri);
        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_MIME_TYPE, mimeType);
        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_EXTENSION, extension);
        showFileIntent.putExtra(DownloadFileService.RESULT_KEY_FILENAME, filename);

        ((MainActivity) context).startActivityForResult(showFileIntent, ShowImageActivity.DELETE_CODE);

    }

    /**
     * Show the local file getting it from the local public directory
     * @param context activity's context
     * @param filename name of the file to show
     */
    public static void showLocalFile(Context context, final String filename) {

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
}
