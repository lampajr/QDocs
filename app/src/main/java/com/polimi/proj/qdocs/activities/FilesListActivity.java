package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.SaveFileReceiver;
import com.polimi.proj.qdocs.services.ShowFileReceiver;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;

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
 * @see SaveFileReceiver
 * @see ShowFileReceiver
 * @see DownloadFileService
 */

public class FilesListActivity extends AppCompatActivity {

    private static final String TAG = "FILES_LIST_ACTIVITY";

    private static final String BASE_REFERENCE = "files";
    private static final String FILENAME_KEY = "filename";
    private static final String SIZE_KEY = "size";
    private static final String FORMAT_KEY = "format";

    private static final int PERMISSION_CODE = 10 ;

    private static final int IMG_PRV = 100;
    private static final int AUD_PRV = 200;
    private static final int VID_PRV = 300;
    private static final int FILE_PRV = 400;

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
        storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid());

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
                d.setTitle(getString(R.string.upload_new_file));
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
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, FILE_PRV);
                        d.dismiss();
                    }
                });
                d.show();
            }
        });
    }

    /**
     * Show a dialog that will ask to the user the pathname of the file
     * that is going to be uploaded, the pathname consists of the folder,
     * even if it doesn't exist or null if the current folder is ok.
     * if user confirms then the file can be uploaded
     * @param data data to upload
     *
     */
    private void showPathnameChooserDialog(final Intent data) {
        Log.d(TAG, "starting pathname chooser dialog..");
        final Dialog d = new Dialog(FilesListActivity.this);
        d.setTitle(getString(R.string.pathname_chooser));
        d.setCancelable(true);
        d.setContentView(R.layout.pathname_chooser_dialog);

        EditText pathnameText = d.findViewById(R.id.pathname_text);
        final String pathname = filterPathname(pathnameText.getText().toString());

        Button confirmButton = d.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile(data, pathname);
                d.dismiss();
            }
        });

        Button cancelButton = d.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    /**
     * Filters the pathname removing illegal characters
     * @param pathname name to filter
     * @return the filtered pathname
     */
    private String filterPathname(String pathname) {
        //TODO: implement pathname filter
        return pathname;
    }

    /**
     * initialize the List View that will show the list of all user's files stored in the Firebase
     * Storage, it will add the listener on the items
     */
    private void initFilesList() {
        Log.d(TAG, "Creating files adapter");
        ListView listView = findViewById(R.id.list_view);
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
        //TODO: add hierarchy, allow user navigate among folders
        //TODO: add folder class and interface for dirs and files
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
                    Log.d(TAG, "Found new file: " + file.getKey() + "; " + file.getFilename() +
                            "; " + file.getContentType());
                    files.add(file);
                    filesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyFile file = dataSnapshot.getValue(MyFile.class);
                if (file != null) {
                    Log.d(TAG, "Found new updated file: " + file.getFilename() + "; " + file.getContentType());
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
        ActivityCompat.requestPermissions(FilesListActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PERMISSION_CODE){
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
        if (requestCode == IMG_PRV || requestCode == AUD_PRV || requestCode == FILE_PRV) {
            Log.d(TAG, "Picked a file");
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(FilesListActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"Permission denied for external storage");
                }else {
                    showPathnameChooserDialog(data);
                }
            }
        }
    }

    /**
     * Upload a new file on the FirebaseStorage given the Uri provided by external Activities
     * @param data data of the file to upload
     *
     */
    private void uploadFile(Intent data, final String pathname) {
        Uri fileUri = data.getData();
        //Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
        //cursor.moveToFirst();
        //int idx = cursor.getColumnIndex(mediaStoreData);
        //String absoluteFilePath = cursor.getString(idx);

        String absoluteFilePath = PathResolver.getPathFromUri(this, fileUri);

        final Uri file = Uri.fromFile(new File(absoluteFilePath));

        StorageReference fileRef = !pathname.equals("") ?
                storageRef.child(pathname).child(file.getLastPathSegment())
                : storageRef.child(file.getLastPathSegment());

        // file information
        final String contentType = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(fileUri));

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .build();

        UploadTask uploadTask = fileRef.putFile(file, metadata);


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
                addFileOnDb(file.getLastPathSegment());
            }
        });
    }

    /**
     * Add a new item on the db, it generates a qrocode image, it saves that
     * and then add the corresponding element on the Firebase Database
     * @param filename name of the file
     */
    private void addFileOnDb(final String filename) {
        Log.d(TAG, "Adding new file on the realtime firebase database..");
        String code = generateCode(); // generate a new code

        StorageReference fileStorage = storageRef.child(filename);
        String contentType = "";
        fileStorage.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String contentType = storageMetadata.getContentType();
            }
        });

        MyFile f = new MyFile(filename, contentType);
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

    /**
     * generates a new qrcode
     * @param key text to encode
     * @return the qr code bitmap
     */
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
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startDownloadTmpFileService(String filename) {
        Log.d(TAG, "showing file " + filename);
        Intent viewerIntentService = new Intent(this, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        ShowFileReceiver receiver = new ShowFileReceiver(this, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        startService(viewerIntentService);
    }

    /**
     * Start the service that will store the file on the public storage
     * @param filename name of the file
     */
    private void startingSavingFileService(String filename) {
        Intent viewerIntentService = new Intent(this, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        // create the result receiver for the IntentService
        SaveFileReceiver receiver = new SaveFileReceiver(this, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        startService(viewerIntentService);
    }

    /**
     * Deletes a file from list
     * @param filename name of the file to delete
     */
    private void deletePersonalFile(final String filename) {
        Log.d(TAG, "deleting file: " + filename);
        final DatabaseReference dbRefTmp = dbRef;
        storageRef.child(filename).delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure occurred during file deletion");
                Toast.makeText(FilesListActivity.this, getString(R.string.delition_failed), Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "File deletion completed");
                MyFile fileToDelete = retrieveFileByName(filename);
                dbRefTmp.child(fileToDelete.getKey()).removeValue();
            }
        });
        // TODO: implement "are you sure?" dialog
    }


    private void provideQrCode(final String filename) {
        Log.d(TAG, "getting qrcode");
        MyFile f = retrieveFileByName(filename);
        Bitmap qrCode = generateQrCode(f.getKey());
        if (qrCode != null) {
            //TODO: save/show the qrcode generated
        }
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

            String format = f.getContentType();
            fileDescription.setText(format);

            ImageView imageView = convertView.findViewById(R.id.file_image);
            imageView.setImageResource(R.drawable.file_image);

            Button saveButton = convertView.findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startingSavingFileService(name);
                }
            });

            Button deleteButton = convertView.findViewById(R.id.delete_button);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletePersonalFile(name);
                }
            });

            Button getQrcodeButton = convertView.findViewById(R.id.get_qrcode_button);
            getQrcodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    provideQrCode(name);
                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDownloadTmpFileService(name);
                }
            });

            return convertView;
        }
    }
}
