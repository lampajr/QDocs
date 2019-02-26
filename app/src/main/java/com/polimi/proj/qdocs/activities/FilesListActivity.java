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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.SaveFileReceiver;
import com.polimi.proj.qdocs.services.ShowFileReceiver;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.support.Directory;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Andrea Lamparelli
 * @author Pietro Chittò
 *
 * This activity represents the main page of the user, can be seen as folder of all the documents
 * owned by the current user.
 * The activity shows the list of all filesList and allow the user to interact with them:
 *      - show file
 *      - delete file
 *      - save file
 * the user can also upload new filesList on the storage
 *
 * @see AppCompatActivity
 * @see SaveFileReceiver
 * @see ShowFileReceiver
 * @see DownloadFileService
 */

public class FilesListActivity extends AppCompatActivity{

    private static final String TAG = "FILES_LIST_ACTIVITY";

    private static final String BASE_REFERENCE = "documents";
    private static final String KEY_METADATA = "key_metadata";
    private static final String UID_METADATA = "uid_metadata";

    private static final int PERMISSION_CODE = 10 ;

    private static final int IMG_PRV = 100;
    private static final int AUD_PRV = 200;
    private static final int FILE_PRV = 300;

    private StorageReference storageRef;

    private final List<MyFile> files = new ArrayList<>();
    private final List<Directory> directories = new ArrayList<>();
    private FilesListAdapter filesListAdapter;
    private DirectoriesListAdapter directoriesListAdapter;
    private RecyclerView filesView;
    private RecyclerView directoriesView;

    private FirebaseUser user;
    private DatabaseReference dbRef;

    // swipe data
    private double previousX=0.0, previousY=0.0;
    private double offset = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_list);

        getPermission();

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid());

        dbRef = FirebaseDatabase.getInstance().getReference()
                .child(BASE_REFERENCE).child(user.getUid());

        // RecyclerView for files
        filesView = findViewById(R.id.files_view);
        directoriesView = findViewById(R.id.directories_view);
        loadStorageElements();
        initFilesList();
        initDirectoriesList();

        setupUploadFileFloatingButton();
        setupSwipeListener();
        setupToolbar();

    }

    /**
     * setup the toolbar functionality
     */
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBackDirectory();
            }
        });
    }

    /**
     * Initialize the Floating Action Button Menu that is in charge to
     * upload new filesList, picking them from gallery or 'my filesList' section
     * of the mobile phone
     */
    private void setupUploadFileFloatingButton() {
        FloatingActionButton uploadGenericFileFloatingButton = findViewById(R.id.upload_file_button);

        // upload image button
        SubActionButton uploadImageButton = generateSubActionButton(R.drawable.galley);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), IMG_PRV);
            }
        });

        // upload file button
        SubActionButton uploadFileButton = generateSubActionButton(R.drawable.file_image);
        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_PRV);
            }
        });

        // upload audio button
        SubActionButton uploadAudioButton = generateSubActionButton(R.drawable.audio_image);
        uploadAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI), AUD_PRV);
            }
        });

        FloatingActionMenu floatingMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(uploadImageButton)
                .addSubActionView(uploadAudioButton)
                .addSubActionView(uploadFileButton)
                .attachTo(uploadGenericFileFloatingButton)
                .build();
    }

    /**
     * Generates a SubActionButton that has to be added to the floating action men Button
     * @param resId image resource id
     * @return SubActionButton object
     */
    private SubActionButton generateSubActionButton(@DrawableRes int resId) {
        //TODO: change the button dimension
        SubActionButton.Builder subActionBuilder = new SubActionButton.Builder(this);
        ImageView contentImage = new ImageView(this);
        contentImage.setImageResource(resId);
        return subActionBuilder.setContentView(contentImage).build();
    }

    /**
     * set the swipe listener on the view such that the user
     * swiping from right to left can access the login activity
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListener() {
        RelativeLayout mainLayout = findViewById(R.id.files_main_layout);
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

        final EditText pathnameText = d.findViewById(R.id.pathname_text);

        Button confirmButton = d.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathname = filterPathname(pathnameText.getText().toString());
                Log.d(TAG, "pathname inserted : " + pathname);
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
        Log.d(TAG, "Creating filesList adapter");

        filesView.setHasFixedSize(true);
        filesView.setLayoutManager(new LinearLayoutManager(this));
        filesListAdapter = new FilesListAdapter(this, files);
        // set the adapter for the files
        filesView.setAdapter(filesListAdapter);
    }

    /**
     * initialize the List View that will show the list of all user's directories stored in the Firebase
     * Storage, it will add the listener on the items
     */
    private void initDirectoriesList() {
        Log.d(TAG, "Creating directoriesList adapter");

        directoriesView.setHasFixedSize(true);
        directoriesView.setLayoutManager(new LinearLayoutManager(this));
        directoriesListAdapter = new DirectoriesListAdapter(this, directories);
        // set the adapter for the directories
        directoriesView.setAdapter(directoriesListAdapter);
    }

    /**
     * load all the filesList from the Firebase Realtime Database
     * and store them into the filesList attribute.
     * implements the callback method from the realtime database
     * in order to react in case of db operation.
     */
    private void loadStorageElements() {

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded : key found -> " + dataSnapshot.getKey());
                if (dataSnapshot.getKey().matches("\\d+")) {
                    // the element is a file
                    Log.d(TAG, "adding new file..");
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null) {
                        Log.d(TAG, "Found new file: " + file.getKey() + "; " + file.getFilename() +
                                "; " + file.getContentType() + "; " + file.getSize() +
                                "; " + file.getTime());
                        files.add(file);
                    }
                }
                else {
                    // the element is a directory
                    Log.d(TAG, "adding new folder..");
                    Directory dir = new Directory(dataSnapshot.getKey());
                    directories.add(dir);
                }
                notifyAdapters();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TODO: implement onChildChanged listener on db
                Log.d(TAG, "onChildChanged");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");
                if (dataSnapshot.getKey().matches("\\d+")) {
                    // the element to remove is a file
                    Log.d(TAG, "removing new file..");
                    MyFile file = retrieveFileByKey(dataSnapshot.getValue(MyFile.class).getKey());
                    if (file != null)
                        files.remove(file);
                }
                else {
                    // the element to remove is a directory
                    Log.d(TAG, "removing new folder..");
                    Directory dir = retrieveDirectoryByName(dataSnapshot.getKey());
                    if (dir != null)
                        directories.remove(dir);
                }
                notifyAdapters();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TODO: implement onChildMoved
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
        Log.d(TAG, "creating menu");
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.app_settings_menu, menu);

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

        String absoluteFilePath = PathResolver.getPathFromUri(this, fileUri);

        final Uri file = Uri.fromFile(new File(absoluteFilePath));

        StorageReference fileRef = !pathname.equals("") ?
                storageRef.child(pathname).child(file.getLastPathSegment())
                : storageRef.child(file.getLastPathSegment());

        // file information
        final String contentType = getContentResolver().getType(fileUri);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata(KEY_METADATA, generateCode())
                .setCustomMetadata(UID_METADATA, user.getUid())
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
                //String filename = pathname.equals("") ? file.getLastPathSegment()
                //        : pathname + "/" + file.getLastPathSegment();
                //addFileOnDb(filename);
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.e(TAG, "Upload canceled");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Upload paused");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //TODO: implement ViewStub progress bar in overlay
            }
        });
    }

    /**
     * Generate a new code from which provide a new qrcode to
     * associate to a new file
     * @return the code
     */
    private String generateCode() {
        long time = Calendar.getInstance().getTimeInMillis();
        String code = time + "";// + "" + new Random().nextLong();
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
            if (f != null && f.getKey() != null
                    && f.getKey().equals(key)) return f;
        }
        return null;
    }

    /**
     * Retrieve a Directory object from the filesList attribute
     * @param name name to check, is unique for folders
     * @return the Directory obj
     */
    public Directory retrieveDirectoryByName(String name) {
        for(Directory d : directories) {
            if (d != null && d.getDirectoryName() != null
                    && d.getDirectoryName().equals(name)) return d;
        }
        return null;
    }


    /**
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startShowingFileService(String filename) {
        Log.d(TAG, "showing file " + filename);
        Intent viewerIntentService = new Intent(this, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        filename = getCurrentPath(dbRef) + "/" + filename;

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

        filename = getCurrentPath(dbRef) + "/" + filename;

        // create the result receiver for the IntentService
        SaveFileReceiver receiver = new SaveFileReceiver(this, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        startService(viewerIntentService);
    }

    /**
     * Return the current path, from the root to the current directory
     * @return the path string
     */
    private String getCurrentPath(DatabaseReference ref) {
        if (ref.getKey().equals(user.getUid()))
            return "";
        else
            return getCurrentPath(ref.getParent()) + "/" + ref.getKey();
    }

    /**
     * Deletes a file from list
     * @param filename name of the file to delete
     */
    private void deletePersonalFile(final String filename) {
        //TODO: implement "are you sure?" dialog

        Log.d(TAG, "deleting file: " + filename);
        storageRef.child(filename).delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure occurred during file removing");
                Toast.makeText(FilesListActivity.this, getString(R.string.delition_failed), Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "File correctly removed!");
            }
        });
    }

    /**
     * Generates a new qrcode bitmap
     * @param filename text to encode
     */
    private void showQrCode(final String filename) {
        Log.d(TAG, "getting qrcode");
        MyFile f = retrieveFileByName(filename);
        final Bitmap qrCode = generateQrCode(f.getKey());
        if (qrCode != null) {
            Log.d(TAG, "showing qrcode dialog..");
            final Dialog d = new Dialog(FilesListActivity.this);
            d.setTitle(getString(R.string.pathname_chooser));
            d.setCancelable(true);
            d.setContentView(R.layout.show_qrcode_dialog);
            ImageView qrcodeImage = d.findViewById(R.id.qrcode_iamge);
            qrcodeImage.setImageBitmap(qrCode);
            Button saveButton = d.findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File dst = new File(PathResolver.createPublicDocStorageDir(FilesListActivity.this).getAbsolutePath(),
                                    "QRCode-" + filename);

                            //File dst = new File(PathResolver.getPublicDocFileDir(FilesListActivity.this).getAbsolutePath(), "QRCode-" + filename);
                            if (!dst.exists()) {
                                //TODO: 2 filesList with same name but different folder cannot produce 2 qr code different
                                try (FileOutputStream out = new FileOutputStream(dst)) {
                                    qrCode.compress(Bitmap.CompressFormat.PNG, 100, out); // qrCode is the Bitmap instance
                                    // PNG is a lossless format, the compression factor (100) is ignored
                                    d.dismiss();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Log.d(TAG, "QRCode already saved");
                                Toast.makeText(FilesListActivity.this,
                                        getString(R.string.file_already_stored),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
            d.show();
        }
    }

    /**
     * get back to the previous directory if any.
     */
    private void getBackDirectory() {
        if (!dbRef.getKey().equals(user.getUid())) {
            Log.d(TAG, "Removing a directory level");
            dbRef = dbRef.getParent();
            storageRef = storageRef.getParent();
            files.clear();
            directories.clear();
            notifyAdapters();
            loadStorageElements();
        }
        else {
            Log.d(TAG, "you are already at root");
            Toast.makeText(FilesListActivity.this, getString(R.string.already_at_root_level),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * change the default directory, updating the filesList to show
     * @param folderName name of the folder to which move to
     */
    private void openDirectory(final String folderName) {
        Log.d(TAG, "changing directory, going to " + folderName);
        dbRef = dbRef.child(folderName);
        storageRef = storageRef.child(folderName);
        files.clear();
        directories.clear();
        notifyAdapters();
        loadStorageElements();
    }

    /**
     * Notifies the adapters that data has been changed
     */
    public void notifyAdapters() {
        filesListAdapter.notifyDataSetChanged();
        directoriesListAdapter.notifyDataSetChanged();
    }

    //TODO: creates RecyclerView Adapter for directories
    /**
     * RecyclerView adapter for the directories' list
     */
    private class DirectoriesListAdapter extends RecyclerView.Adapter<DirectoriesListAdapter.DirectoryDataViewHolder> {

        private LayoutInflater inflater;
        private List<Directory> directories;
        private Context context;

        DirectoriesListAdapter(Context context, List<Directory> directories) {
            this.inflater = LayoutInflater.from(context);
            this.directories = directories;
            this.context = context;
        }

        @NonNull
        @Override
        public DirectoryDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_directory, parent, false);
            return new DirectoryDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DirectoryDataViewHolder holder, int position) {
            holder.bindData(directories.get(position));
        }

        @Override
        public int getItemCount() {
            return directories.size();
        }

        /**
         * Holder for the directory element
         */
        class DirectoryDataViewHolder extends RecyclerView.ViewHolder {

            TextView directoryNameView;
            CardView directoryCard;

            DirectoryDataViewHolder(@NonNull View itemView) {
                super(itemView);
                directoryNameView = itemView.findViewById(R.id.directory_name);
                directoryCard = itemView.findViewById(R.id.directory_card);
            }

            void bindData(final Directory directory) {
                directoryNameView.setText(directory.getDirectoryName());

                directoryCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openDirectory(directory.getDirectoryName());
                    }
                });
            }
        }
    }

    /**
     * RecyclerView adapter for the files' list
     */
    private class FilesListAdapter extends RecyclerView.Adapter<FilesListAdapter.FileDataViewHolder> {
        private LayoutInflater inflater;
        private List<MyFile> files;
        private Context context;

        FilesListAdapter(Context context, List<MyFile> files) {
            this.inflater = LayoutInflater.from(context);
            this.files = files;
            this.context = context;
        }

        @NonNull
        @Override
        public FileDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //TODO: rearrange item file layout
            View view = inflater.inflate(R.layout.item_file, parent, false);
            return new FileDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final FileDataViewHolder holder, int position) {
            holder.bindData(files.get(position));
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        /**
         * Holder for the File element
         */
        class FileDataViewHolder extends RecyclerView.ViewHolder {
            // Item-row elements
            TextView filenameView, fileDescriptionView, fileOptionView;
            CardView fileCard;

            FileDataViewHolder(@NonNull View itemView) {
                super(itemView);
                filenameView = itemView.findViewById(R.id.file_name);
                fileDescriptionView = itemView.findViewById(R.id.file_description);
                fileOptionView = itemView.findViewById(R.id.file_options);
                fileCard = itemView.findViewById(R.id.file_card);
            }

            void bindData(final MyFile file) {
                filenameView.setText(file.getFilename());
                fileDescriptionView.setText(file.getContentType());

                fileCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startShowingFileService(file.getFilename());
                    }
                });

                fileOptionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // will show popup menu here
                        PopupMenu fileSettingsPopup = new PopupMenu(context, fileOptionView);
                        fileSettingsPopup.inflate(R.menu.file_settings_menu);

                        fileSettingsPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                String name = file.getFilename();
                                switch (menuItem.getItemId()) {
                                    case R.id.delete_option:
                                        deletePersonalFile(name);
                                        break;

                                    case R.id.save_option:
                                        startingSavingFileService(name);
                                        break;

                                    case R.id.get_qrcode_option:
                                        showQrCode(name);
                                        break;
                                }
                                return false;
                            }
                        });

                        fileSettingsPopup.show();
                    }
                });
            }
        }
    }

}
