package com.polimi.proj.qdocs.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.AreYouSureDialog;
import com.polimi.proj.qdocs.listeners.OnYesListener;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.services.ShowFileReceiver;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

import java.util.ArrayList;
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

    private final HashMap<String, MyFile> filesMap = new HashMap<>();

    private FirebaseHelper fbHelper;

    // scan data
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

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

        // authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        fbHelper = new FirebaseHelper();

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
        setupBarcodeScanner();

        setFilesEventListener();

        return scannerView;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (barcodeView != null) {
            if (isVisibleToUser) {
                Log.d(TAG, "Scanning resumed");
                barcodeView.decodeContinuous(barcodeCallback);
                lastText = "";
                setFilesEventListener();
            }
            else {
                Log.d(TAG, "Scanning paused");
                barcodeView.decodeContinuous(emptyCallback);
                lastText = "";
            }
        }
    }

    /**
     * Setup the barcode adding its OnClick listener that will hide/restore
     * the Bottom Navigation Bar
     */
    private void setupBarcodeScanner() {
        barcodeView.setStatusText("");
        barcodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity parent = (MainActivity) context;
                if (parent.navigationBarIsHidden()) {
                    parent.restoreBottomNavigationBar();
                }
                else {
                    parent.hideBottomNavigationBar();
                }
            }
        });
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


    private void setFilesEventListener() {
        Log.d(TAG, "Setting value event listener on the Firebase db");
        filesMap.clear();

        fbHelper.getDatabaseReference().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                loadAllFiles(dataSnapshot, null);
                Log.d(TAG, "Files loaded");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadAllFiles(DataSnapshot dataSnapshot, String folder) {
        for (DataSnapshot ds: dataSnapshot.getChildren()) {
            if (StorageElement.isFile(ds)) {
                MyFile f = ds.getValue(MyFile.class);
                if (f != null) {
                    if (folder != null)
                        f.setPathname(folder + "/" + f.getFilename());
                    else f.setPathname(f.getFilename());

                    filesMap.put(f.getKey(), f);
                }
            }
            else {
                String pathFolder = folder == null ? ds.getKey()
                        : folder + "/" + ds.getKey();
                loadAllFiles(ds, pathFolder);
            }
        }
    }

    public void onDeleteFromFile(final String filename) {
        Log.d(TAG, "Removing file: " + filename);

        final MyFile f = StorageElement.retrieveFileByName(filename, new ArrayList<StorageElement>(filesMap.values()));
        if (f != null) {
            new AreYouSureDialog(context, new OnYesListener() {
                @Override
                public void onYes() {
                    fbHelper.deletePersonalFile(null, f.getPathname(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Failure occurred during file removing");
                            Toast.makeText(context, getString(R.string.delition_failed), Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "File correctly removed!");
                        }
                    });
                }
            }).show();
        }
        else {
            Toast.makeText(context, "Unable to find " + filename, Toast.LENGTH_SHORT).show();
        }
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
                MyFile f = (MyFile) entry.getValue();
                Utility.startShowFileService(context, f.getPathname(), f.getContentType());
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
