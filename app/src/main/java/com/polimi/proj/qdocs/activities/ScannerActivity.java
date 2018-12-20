package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.polimi.proj.qdocs.support.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    /**
     * SCANNER ACTIVITY that implements a custom camera that allows user
     * to capture the image (qr code) and then decode it
     */

    private static final String TAG = "SCANNER_ACTIVITY";

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_LOGIN = 2;

    public static final String LOGIN_MODE_KEY = "com.polimi.proj.qdocs.activities.LOGIN_MODE_KEY";
    public static final String FILENAME_KEY = "com.polimi.proj.qdocs.activities.FILENAME_KEY";

    private static final String BASE_LINKAGE_REFERENCE = "linkage";

    // scanner data
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    // swipe data
    private double previousX=0.0, previousY=0.0;
    private double offset = 20;

    // authentication
    private FirebaseAuth firebaseAuth;
    private User.LoginMode loginMode = User.LoginMode.UNKNOWN;

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

            verifyKey(result.getText());
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
        startActivityForResult(loginIntent, REQUEST_LOGIN);
    }

    /**
     * start the file Activity
     */
    private void startFileActivity() {
        Intent filesIntent = new Intent(this, FileActivity.class);
        startActivity(filesIntent);
    }

    /**
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startFileViewer(String filename) {
        Intent viewerIntent = new Intent(this, FileViewer.class);
        viewerIntent.putExtra(FILENAME_KEY, filename);
        startActivity(viewerIntent);
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

    public void triggerScan(View view) {
        barcodeView.decodeSingle(barcodeCallback);
    }

    /**
     * check the status of the user, if it is not logged in
     * force it to do so
     */
    private void checkUserStatus() {
        if (firebaseAuth.getCurrentUser() == null) {
            // you must login
            startLoginActivity();
        }
        else {
            // user already logged in
            User.createUser(firebaseAuth.getCurrentUser(), loginMode);
        }
    }


    /**
     * check whether there is a filename associated with this key
     * @param key key detected by the barcode scanner
     */
    private void verifyKey(String key) {
        String reference = BASE_LINKAGE_REFERENCE + "/" + User.getUser().getUid() + "/" + key;
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(reference);
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String filename = (String) dataSnapshot.getValue();
                if (filename == null) {
                    // there are no files associated with this key
                }
                else {
                    // launch FileViewer with the filename
                    startFileViewer(filename);
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
                FirebaseAuth.getInstance().signOut();
                startLoginActivity();
                break;
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermission();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume!");
        barcodeView.resume();
        checkPermission();
        checkUserStatus();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_LOGIN) {
            // the user has been logged in
            if (data != null) {
                loginMode = (User.LoginMode) data.getExtras().get(LOGIN_MODE_KEY);
                User.createUser(firebaseAuth.getCurrentUser(), loginMode);
            }
            else {
                // the user was get back without logging in
                // finish the app
                Log.d(TAG, "Close the app!");
                finish();
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

