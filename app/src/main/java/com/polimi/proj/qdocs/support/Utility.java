package com.polimi.proj.qdocs.support;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.cocosw.bottomsheet.BottomSheet;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polimi.proj.qdocs.R;
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

    /**
     * Generate a new BottomSheet menu given the following parameters
     * @param activity parent activity that will show the menu
     * @param title title of the menu
     * @param menuId resource id of the menu to inflate
     * @param listener listener on the menu items click
     * @return new object, NB: need to be showed
     */
    public static BottomSheet.Builder generateBottomSheetMenu(AppCompatActivity activity,
                                                       String title,
                                                       int menuId,
                                                       MenuItem.OnMenuItemClickListener listener) {
        return new BottomSheet.Builder(activity, R.style.bottom_sheet_style)
                .title(title)
                .sheet(menuId)
                .listener(listener);
    }

    /**
     * Downloads the specific file, if not yet download, and saves it locally
     * @param context activity's context
     * @param pathname pathname of the file to save
     */
    public static void saveFile(Context context, String pathname) {
        Intent viewerIntentService = new Intent(context, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        SaveFileReceiver receiver = new SaveFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, pathname);
        context.startService(viewerIntentService);
    }

    /**
     * Download the specific file in a temporary one and show it
     * @param context activity's context
     * @param pathname pathname of the file to show
     */
    public static void showFile(Context context, final String pathname) {
        Intent viewerIntentService = new Intent(context, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        ShowFileReceiver receiver = new ShowFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, pathname);
        context.startService(viewerIntentService);
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
