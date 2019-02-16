package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadStorageFileReceiver;
import com.polimi.proj.qdocs.services.DownloadTmpFileReceiver;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.support.MyFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                    Log.d(TAG, "Found new file: " + file.getFilename() + "; " + file.getSize() +
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
                MyFile file = dataSnapshot.getValue(MyFile.class);
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
        Log.d(TAG, "Creazione menu");
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
                    uploadImage(data);

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
                    uploadAudio(data);

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
                    uploadVideo(data);

                }
            }
        }
    }

    /**
     * Add a new item on the db, it generates a qrocode image, it saves that
     * and then add the corresponding element on the Firebase Database
     * @param filename name of the file
     */
    private void addFileOnDb(final String filename, final String format, Long size) {
        Log.d(TAG, "Adding new file on the firebase database..");
        String code = encode(filename); // find the new code

        //TODO: generate the qrcode and show/save it

        MyFile f = new MyFile(filename, format, size);
        dbRef.child(code).setValue(f);
        Log.d(TAG, "New file added");
    }

    /**
     * Encode a new code for the given filename
     * @param filename name
     * @return the code
     */
    private String encode(final String filename) {
        return "utxcvlbkj";
    }

    /**
     * upload into the Cloud Storage a video file
     * @param data
     */
    private void uploadVideo(Intent data) {
        Uri videoz = data.getData();
        Context context = getBaseContext();
        Cursor cursor = getContentResolver().query(videoz, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
        String absoluteFilePath = cursor.getString(idx);

        Uri file = Uri.fromFile(new File(absoluteFilePath));
        String userPath = ""+FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference imgRef = storageRef.child(userPath+"/"+file.getLastPathSegment());
        UploadTask uploadTask = imgRef.putFile(file);

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
            }
        });
    }

    /**
     * upload into the Cloud Storage an audio file
     * @param data
     */
    private void uploadAudio(Intent data) {
        Uri audioz = data.getData();
        Context context = getBaseContext();
        Cursor cursor = getContentResolver().query(audioz, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
        String absoluteFilePath = cursor.getString(idx);

        Uri file = Uri.fromFile(new File(absoluteFilePath));
        String userPath = ""+FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference imgRef = storageRef.child(userPath+"/"+file.getLastPathSegment());
        UploadTask uploadTask = imgRef.putFile(file);

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
            }
        });
    }

    /**
     * upload into the Cloud Storage an image file
     * @param data
     */
    private void uploadImage(Intent data){
        Uri picturez = data.getData();
        Context context = getBaseContext();
        Cursor cursor = getContentResolver().query(picturez, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String absoluteFilePath = cursor.getString(idx);

        final Uri file = Uri.fromFile(new File(absoluteFilePath));
        String userPath = ""+FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference imgRef = storageRef.child(userPath+"/"+file.getLastPathSegment());
        UploadTask uploadTask = imgRef.putFile(file);

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
                addFileOnDb(file.getLastPathSegment(), "image", 256L);
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

        public FilesAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<MyFile> objects) {
            super(context, textViewResourceId, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            Log.d(TAG, "showing file");

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
                    //TODO: implement downlaod event
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "showing file " + name);
                    startDownloadTmpFileService(name);
                    //TODO: implement show file event
                }
            });

            //TODO: add delete file button

            return convertView;
        }
    }
}