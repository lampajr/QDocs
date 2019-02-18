package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadStorageFileReceiver;
import com.polimi.proj.qdocs.services.DownloadTmpFileReceiver;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.support.MyFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * @author Andrea Lamparelli
 * @author Pietro Chittò
 *
 * This activity represents the main page of the user, can be seen as folder of all the documents
 * owned by the current user.
 * The activity shows the list of all files and allow the user to interact with them:
 *      - show file
 *      - delete file
 *      - save file
 * the user can also upload new files on the storage
 *
 * @see AppCompatActivity
 * @see DownloadStorageFileReceiver
 * @see DownloadTmpFileReceiver
 * @see DownloadFileService
 */

public class FilesListActivity extends AppCompatActivity {

    private static final String TAG = "FILES_LIST_ACTIVITY";

    private static final String BASE_REFERENCE = "files";
    private static final String FILENAME_KEY = "filename";
    private static final String SIZE_KEY = "size";
    private static final String FORMAT_KEY = "format";

    private static final int EX_PER = 10 ;

    private static final int IMG_PRV = 100;
    private static final int AUD_PRV = 200;
    private static final int VID_PRV = 300;

    private FloatingActionButton addButton;
    private StorageReference storageRef;
    private List<MyFile> files;
    private FilesAdapter filesAdapter;

    private FirebaseUser user;
    private DatabaseReference dbRef;

    // swipe data
    private double previousX=0.0, previousY=0.0;
    private double offset = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        getPermission();

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference();

        loadFiles();
        initFilesList();

        initAddFileButton();

        Toolbar toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);

        setupSwipeListener();
    }


    /**
     * set the swipe listener on the view such that the user
     * swiping from right to left can access the login activity
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListener() {
        ConstraintLayout mainLayout = findViewById(R.id.files_main_layout);
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
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
                    if (previousX < currentX - offset) {
                        startScannerActivity();
                    }
                }
                return true;
            }
        });
    }

    /**
     * Start the ScannerActivity
     */
    private void startScannerActivity() {
        Intent scannerIntent = new Intent(this, ScannerActivity.class);
        finish();
        startActivity(scannerIntent);
        overridePendingTransition(R.anim.left_to_right, R.anim.exit_l2r);
    }

    /**
     * initialize the add file button (+)
     */
    private void initAddFileButton() {
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click add button");
                final Dialog d=new Dialog(FilesListActivity.this);
                d.setTitle("Add new file");
                d.setCancelable(true);
                d.setContentView(R.layout.chooser_file_type_dialog);

                ImageView gallery_img = d.findViewById(R.id.image_image);
                gallery_img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View arg0)
                    {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), IMG_PRV);
                        d.dismiss();
                    }
                });

                ImageView audio_img = d.findViewById(R.id.audio_image);
                audio_img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View arg0)
                    {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI), AUD_PRV);
                        d.dismiss();
                    }
                });

                ImageView file_img = d.findViewById(R.id.file_image);
                file_img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View arg0)
                    {
                        Toast.makeText(FilesListActivity.this, "To implement", Toast.LENGTH_LONG).show();
                        d.dismiss();
                    }
                });
                d.show();
            }
        });
    }

    /**
     * initialize the List View that will show the list of all user's files stored in the Firebase
     * Storage, it will add the listener on the items
     */
    private void initFilesList() {
        Log.d(TAG, "Creating files adapter");
        ListView listView = findViewById(R.id.list_view);
        // TODO: add event listener on the item of the list view
        filesAdapter = new FilesAdapter(this, R.layout.item_file, files);
        listView.setAdapter(filesAdapter);
    }

    /**
     * load all the files from the Firebase Realtime Database
     * and store them into the files attribute.
     * implements the callback method from the realtime database
     * in order to react in case of db operation.
     */
    private void loadFiles() {
        files = new ArrayList<>();

        // get firebase database reference
        dbRef = FirebaseDatabase.getInstance().getReference()
                .child(BASE_REFERENCE).child(user.getUid());
        // retrieve
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyFile file = dataSnapshot.getValue(MyFile.class);
                if (file != null) {
                    file.setKey(dataSnapshot.getKey());
                    Log.d(TAG, "Found new file: " + file.getKey() + "; " + file.getFilename() + "; " + file.getSize() +
                            "; " + file.getFormat());
                    files.add(file);
                    filesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyFile file = dataSnapshot.getValue(MyFile.class);
                if (file != null) {
                    Log.d(TAG, "Found new updated file: " + file.getFilename() + "; " + file.getSize() +
                            "; " + file.getFormat());
                    files.add(file);
                    filesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                MyFile file = retrieveFileByKey(dataSnapshot.getKey());
                assert file != null;
                Log.d(TAG, "removed file: " + file.getFilename());
                files.remove(file);
                filesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "database error occurred: onCanceled");
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "Creating menu");
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.file_menu_layout, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id=item.getItemId();
        switch(id)
        {
            case R.id.logout_menu:
                LoginActivity.logout();
                Log.d(TAG, "Log out");

                final Intent scanner = new Intent(FilesListActivity.this, ScannerActivity.class);
                scanner.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(scanner);
                break;
        }
        return false;
    }

    /**
     * retrieve the permission for reading external storage
     */
    private void getPermission() {
        ActivityCompat.requestPermissions(FilesListActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, EX_PER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==EX_PER){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "permissions read/write external storage ok");
            }
            else{

                Log.d(TAG, "permissions read/write external storage denied");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_PRV){
            Log.d(TAG, "On result: image");
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(FilesListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"Permission denied for external storage");
                }else {
                    uploadFile(data, MediaStore.Images.ImageColumns.DATA, "image");
                }
            }
        }

        if(requestCode == AUD_PRV) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "On result: audio");
                if (ContextCompat.checkSelfPermission(FilesListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission denied for external storage");
                } else {
                    uploadFile(data, MediaStore.Audio.AudioColumns.DATA, "audio");
                }
            }
        }

        if(requestCode == VID_PRV) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "On result: video");
                if (ContextCompat.checkSelfPermission(FilesListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission denied for external storage");
                } else {
                    uploadFile(data, MediaStore.Video.VideoColumns.DATA, "image");
                }
            }
        }
    }

    /**
     * Add a new item on the db, it generates a qrocode image, it saves that
     * and then add the corresponding element on the Firebase Database
     * @param filename name of the file
     */
    private void addFileOnDb(final String filename, final String format, long size) {
        Log.d(TAG, "Adding new file on the realtime firebase database..");
        String code = generateCode(); // generate a new code

        //TODO: generate the qrcode and show/save it

        MyFile f = new MyFile(filename, format, size);
        f.setKey(code);
        try {
            dbRef.child(code).setValue(f);
            Log.d(TAG, "New file added");
        }
        catch (DatabaseException ex) {
            Toast.makeText(this, "Invalid name: Firebase Database paths must not contain '.', '#', '$', '[', or ']'", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Database path incorrect -> file wasn't added");
        }
    }

    /**
     * Generate a new code from which provide a new qrcode to
     * associate to a new file
     * @return the code
     */
    private String generateCode() {
        long time = Calendar.getInstance().getTimeInMillis();
        String code = time + "" + new Random().nextLong();
        Log.d(TAG, "new code: " + code);
        return code;
    }

    private Bitmap generateQrCode(String key) {
        try {
            Log.d(TAG, "encoding " + key + " into a new QR Code");
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmapQrCode = barcodeEncoder.encodeBitmap(key, BarcodeFormat.QR_CODE, 400, 400);
            Log.d(TAG, "QR Code generated!");
            return bitmapQrCode;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve a MyFile object matching the filename passed as paramater
     * @param filename name of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public MyFile retrieveFileByName(String filename) {
        for(MyFile f : files) {
            if (f.getFilename().equals(filename)) return f;
        }
        return null;
    }

    /**
     * Retrieve a MyFile object matching the key passed as paramater
     * @param key key of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public MyFile retrieveFileByKey(String key) {
        for(MyFile f : files) {
            if (f.getKey().equals(key)) return f;
        }
        return null;
    }

    /**
     * Upload a new file on the FirebaseStorage given the Uri provided by external Activities
     * @param data data of the file to upload
     * @param mediaStoreData MediaStore index
     * @param format format of the file: image, text, audio, video.
     */
    private void uploadFile(Intent data, String mediaStoreData, final String format) {
        Uri fileUri = data.getData();
        Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(mediaStoreData);
        String absoluteFilePath = cursor.getString(idx);

        final Uri file = Uri.fromFile(new File(absoluteFilePath));
        StorageReference fileRef = storageRef.child(user.getUid()).child(file.getLastPathSegment());

        UploadTask uploadTask = fileRef.putFile(file);

        final long size = 0L; //TODO: retrieve true size of file

        Log.d(TAG, "starting uploading");
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload complete");
                addFileOnDb(file.getLastPathSegment(), format, size);
            }
        });
    }


    /**
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startDownloadTmpFileService(String filename) {
        Intent viewerIntentService = new Intent(this, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        DownloadTmpFileReceiver receiver = new DownloadTmpFileReceiver(this, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        startService(viewerIntentService);
    }

    /**
     * Start the service that will store the file on the public storage
     * @param filename name of the file
     */
    private void startDownloadStorageFileService(String filename) {
        Intent viewerIntentService = new Intent(this, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLAOD_SAVE_FILE);

        // create the result receiver for the IntentService
        DownloadStorageFileReceiver receiver = new DownloadStorageFileReceiver(this, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        startService(viewerIntentService);
    }
    // TODO: improve the quality of the adapter
    // TODO: improve the quality of the xml related to the single item
    private class FilesAdapter extends ArrayAdapter<MyFile> {


        FilesAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<MyFile> objects) {
            super(context, textViewResourceId, objects);
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // load the view of a single row (a single file)
            convertView = inflater.inflate(R.layout.item_file, null);


            final TextView filename = convertView.findViewById(R.id.filename);
            TextView fileDescription = convertView.findViewById(R.id.file_description);

            MyFile f = getItem(position);

            final String name = f.getFilename();
            filename.setText(name);

            String format = f.getFormat();
            fileDescription.setText(format);

            ImageView imageView = convertView.findViewById(R.id.file_image);
            imageView.setImageResource(R.drawable.file_image);

            Button saveButton = convertView.findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "saving file " + name);
                    startDownloadStorageFileService(name);
                    //TODO: implement download event
                }
            });

            Button deleteButton = convertView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "deleting file: " + name);
                    MyFile fileToDelete = retrieveFileByName(name);
                    dbRef.child(fileToDelete.getKey()).removeValue();
                    // TODO: implement "are you sure?" dialog
                }
            });

            Button getQrcodeButton = convertView.findViewById(R.id.get_qrcode_button);
            getQrcodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "getting qrcode");
                    MyFile f = retrieveFileByName(name);
                    Bitmap qrCode = generateQrCode(f.getKey());
                    if (qrCode != null) {
                        //TODO: save/show the qrcode generated
                    }
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "showing file " + name);
                    startDownloadTmpFileService(name);
                }
            });

            return convertView;
        }

    }
}
