package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.polimi.proj.qdocs.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    /**
     * SCANNER ACTIVITY that implements a custom camera that allows user
     * to capture the image (qr code) and then decode it
     */

    private static final String TAG = "SCANNER_ACTIVITY";

    public static final String ANONYMOUS_EXTRA = "com.polimi.proj.qdocs.activities.ANONYMOUS_EXTRA";
    private static final int REQUEST_CAMERA_PERMISSION = 50;

    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    private double previousX=0.0, previousY=0.0;
    private double offset = 20;

    private boolean loggedInAnonymously = false;

    private BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            barcodeView.setStatusText(result.getText());

            beepManager.playBeepSoundAndVibrate();

            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcode_preview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            // TODO: to reimplement in according to our application
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_view);

        setupSwipeListener();

        checkPermission();
    }

    /**
     * set the swipe listener on the view such that the user
     * swiping from right to left can access the login activity
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListener() {
        barcodeView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    previousX = event.getX();
                    previousY = event.getY();
                    Log.d(TAG, previousX + " " + previousY);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    double currentX = event.getX();
                    double currentY = event.getY();
                    if (previousX > currentX + offset) {
                        startLoginActivity();
                    }
                }
                return true;
            }
        });
    }

    /**
     * start the login Activity
     */
    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.putExtra(ANONYMOUS_EXTRA, loggedInAnonymously);
        startActivity(loginIntent);
    }

    /**
     * check to have the Camera permission
     */
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        else {
            startBarcodeScanner();
        }
    }

    /**
     * start the barcode scanner, called only whether there are
     * the required Camera permission
     */
    private void startBarcodeScanner() {
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(barcodeCallback);

        beepManager = new BeepManager(this);
    }

    /**
     * put barcode view on pause
     * @param view Pause button
     */
    public void pause(View view) {
        barcodeView.pause();
    }

    /**
     * resume the barcode view
     * @param view Resume button
     */
    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(barcodeCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume!");
        barcodeView.resume();
        checkPermission();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "On Pause!");
        barcodeView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startBarcodeScanner();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}

