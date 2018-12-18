package com.polimi.proj.qdocs.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;

import com.polimi.proj.qdocs.activities.ScannerActivity;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class QRCodeDetectionService extends IntentService {
    public static final String DECODE_ACTION = "com.polimi.proj.qdocs.services.action.DECODE_ACTION";
    public static final String ACTION_BAZ = "com.polimi.proj.qdocs.services.action.BAZ";

    // TODO: Rename parameters
    public static final String DETECTION_RESULT = "com.polimi.proj.qdocs.services.DETECTION_RESULT";
    public static final String IMAGE_TO_DECODE = "com.polimi.proj.qdocs.services.extra.IMAGE_TO_DECODE";

    public QRCodeDetectionService() {
        super("QRCodeDetectionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (DECODE_ACTION.equals(action)) {
                final byte[] bytes = (byte[]) intent.getExtras().get(IMAGE_TO_DECODE);
                decodeImage(bytes);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void decodeImage(byte[] bytes) {
        // TODO: implement detection

        sendResults("24732987423 PROVA");
    }

    /**
     * send the result to the broadcast receiver in charge to handle it
     * @param key key detected from the qr code
     */
    private void sendResults(final String key) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ScannerActivity.DetectorReceiver.DETECTION_RECEIVER);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(DETECTION_RESULT, key);
        sendBroadcast(broadcastIntent);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
