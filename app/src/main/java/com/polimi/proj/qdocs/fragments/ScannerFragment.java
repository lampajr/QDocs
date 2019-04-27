package com.polimi.proj.qdocs.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.services.ShowFileReceiver;
import com.polimi.proj.qdocs.support.MyFile;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrea Lamparelli
 * @author Pietro Chittò
 *
 * Fragment that provide a custom QR-code detector that for each key detected from qrcode checks
 * whether the current user has a file (on the storage) associated with this key, if yes it calls
 * an IntentService that will be in charge to download it and provide its Uri, then this activity
 * after get back the result has to invoke the appropriate activity that will show the file.
 *
 * @see Fragment
 * @see com.journeyapps.barcodescanner.BarcodeView
 */
public class ScannerFragment extends Fragment {
    private static final String TAG = "SCANNER_ACTIVITY";
    private static final String ARG_INTENT = "param_intent";

    private Intent mIntent;


    private Context context;
    private MainActivity parentActivity;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private static final String BASE_REFERENCE = "documents";
    private static final String FILENAME_KEY = "filename";

    private final HashMap<String, String> filesMap = new HashMap<>();

    // scan data
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    // swipe data
    private double previousX=0.0, previousY=0.0;
    private double offset = 20;

    // authentication
    private FirebaseUser user;

    // database reference
    private DatabaseReference dbRef;

    // callback on the barcode, listening on results
    private BarcodeCallback barcodeCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            //barcodeView.setStatusText(result.getText());

            beepManager.playBeepSoundAndVibrate();

            checkQrCode(result.getText());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    // empty callback used for stopping scanning when scanner is not visible to the user
    private BarcodeCallback emptyCallback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            // do nothing
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // do nothing
        }
    };


    /**
     * Required empty public constructor
     */
    public ScannerFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param intent Parameter 1.
     * @return A new instance of fragment ScannerFragment.
     */
    public static ScannerFragment newInstance(Intent intent) {
        ScannerFragment fragment = new ScannerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_INTENT, intent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mIntent = (Intent) getArguments().get(ARG_INTENT);
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child(BASE_REFERENCE);

        // retain this fragment
        setRetainInstance(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View scannerView = inflater.inflate(R.layout.fragment_scanner, container, false);

        // get the barcode view
        barcodeView = scannerView.findViewById(R.id.barcode_view);
        // TODO: re-add swipe listener
        //barcodeView.setOnTouchListener(onSwipeTouchListener);
        barcodeView.setStatusText("");

        return scannerView;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (barcodeView != null) {
            if (isVisibleToUser) {
                Log.d(TAG, "Scanning resumed");
                barcodeView.decodeContinuous(barcodeCallback);
            }
            else {
                Log.d(TAG, "Scanning paused");
                barcodeView.decodeContinuous(emptyCallback);
            }
        }
    }

    /**
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startRetrieveFileService(String filename) {
        Intent viewerIntentService = new Intent(context, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        ShowFileReceiver receiver = new ShowFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        context.startService(viewerIntentService);
    }

    /**
     * start the barcode scanner, called only whether there are
     * the required Camera permission
     */
    private void startBarcodeScanner() {
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(mIntent);
        barcodeView.decodeContinuous(barcodeCallback);

        beepManager = new BeepManager(parentActivity);
    }

    /**
     * load all the files into an HashMap obj <keycode, pathname>
     * @param folder nested folder, null if it the root
     */
    private void loadFiles(final String folder) {
        DatabaseReference reference = folder == null ? dbRef.child(user.getUid())
                : dbRef.child(user.getUid()).child(folder);
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().matches("\\d+")) {
                    // the element is a file
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    String pathname = folder == null ? file.getFilename()
                            : folder + "/" + file.getFilename();
                    filesMap.put(file.getKey(), pathname);
                }
                else {
                    String pathFolder = folder == null ? dataSnapshot.getKey()
                            : folder + "/" + dataSnapshot.getKey();
                    loadFiles(pathFolder);
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

    /**
     * check whether there is a filename associated with this code
     * @param code code detected by the barcode scanner
     */
    private void checkQrCode(@NonNull final String code) {
        for (Object o : filesMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (entry.getKey().equals(code)) {
                Log.d(TAG, "QRCode's associated file found!");
                startRetrieveFileService((String) entry.getValue());
                return;
            }
        }
        Toast.makeText(context, getString(R.string.no_files_associated),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
        this.parentActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "On Start!");
        lastText = "";
        filesMap.clear();
        loadFiles(null);
        Log.d(TAG, "Starting barcode scanner!");
        startBarcodeScanner();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume!");
        barcodeView.resume();
        lastText = "";
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "On Pause!");
        barcodeView.pause();
    }
}
