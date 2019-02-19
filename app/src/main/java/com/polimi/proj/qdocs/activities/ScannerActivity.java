package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.fragments.GenericFileFragment;
import com.polimi.proj.qdocs.services.ShowFileReceiver;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Andrea Lamparelli
 * @author Pietro Chittò
 *
 * Activity that provide a custom QR-code detector that for each key detected from qrcode checks
 * whether the current user has a file (on the storage) associated with this key, if yes it calls
 * an IntentService that will be in charge to download it and provide its Uri, then this activity
 * after get back the result has to invoke the appropriate activity that will show the file.
 *
 * @see android.app.Activity
 * @see com.journeyapps.barcodescanner.BarcodeView
 * @see com.polimi.proj.qdocs.fragments.PlayAudioFragment
 * @see com.polimi.proj.qdocs.fragments.ShowImageFragment
 * @see GenericFileFragment
 */

public class ScannerActivity extends AppCompatActivity {

    /**
     * SCANNER ACTIVITY that implements a custom camera that allows user
     * to capture the image (qr code) and then decode it
     */

    private static final String TAG = "SCANNER_ACTIVITY";

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String BASE_REFERENCE = "files";
    private static final String FILENAME_KEY = "filename";

    // scanner data
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    // swipe data
    private double previousX=0.0, previousY=0.0;
    private double offset = 20;

    // authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    // callback on the barcode, listening on results
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

            verifyCode(result.getText());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);

        // get the barcode view
        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_view);

        firebaseAuth = FirebaseAuth.getInstance();

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);

        setupSwipeListener();
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
                        startFileActivity();
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
        startActivity(loginIntent);
    }

    /**
     * start the file Activity
     */
    private void startFileActivity() {
        Intent filesIntent = new Intent(this, FilesListActivity.class);
        startActivity(filesIntent);
        overridePendingTransition(R.anim.right_to_left, R.anim.exit_r2l);
    }

    /**
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startRetrieveFileService(String filename) {
        Intent viewerIntentService = new Intent(this, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        ShowFileReceiver receiver = new ShowFileReceiver(this, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        startService(viewerIntentService);
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
     * check to have the Camera permission
     */
    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // request the camera permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        else {
            startBarcodeScanner();
        }
    }

    /**
     * check the status of the user, if it is not logged in
     * force it to do so
     */
    private void checkUserStatus() {
        user = firebaseAuth.getCurrentUser();
        if (user == null) {
            // you must login
            startLoginActivity();
        }
    }


    /**
     * check whether there is a filename associated with this code
     * @param code code detected by the barcode scanner
     */
    private void verifyCode(@NonNull final String code) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child(BASE_REFERENCE)
                .child(user.getUid()).child(code);
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Object value = dataSnapshot.getValue();
                String key_field = dataSnapshot.getKey();
                assert key_field != null;
                if (key_field.equals(FILENAME_KEY)) {
                    String filename = (String) value;
                    Log.d(TAG, "filename found! : " + filename);
                    startRetrieveFileService(filename);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.logout_menu:
                LoginActivity.logout();
                startLoginActivity();
                break;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "On Start!");
        checkPermission();
        checkUserStatus();
        lastText = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume!");
        barcodeView.resume();
        lastText = "";
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
            // acquired the permission start the barcode scanner
            startBarcodeScanner();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}

