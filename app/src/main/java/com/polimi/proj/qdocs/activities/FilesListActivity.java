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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.support.MyFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

    private FirebaseUser user;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        getPermission();

        user = FirebaseAuth.getInstance().getCurrentUser();

        loadFiles();
        showFiles();


        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click add button");
                final Dialog d=new Dialog(FilesListActivity.this);
                d.setTitle("Login");
                d.setCancelable(true);
                d.setContentView(R.layout.chooser_file_type_dialog);

                ImageView gallery_img = d.findViewById(R.id.image_image);
                gallery_img.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View arg0)
                    {
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), VID_PRV);
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

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar_widget);
        setSupportActionBar(toolbar);
    }

    /**
     * show the list of all personal files
     */
    private void showFiles() {
        //TODO: show list of files
    }

    /**
     * load all the files from the Firebase Realtime Database
     */
    private void loadFiles() {
        files = new ArrayList<>();
        dbRef = FirebaseDatabase.getInstance().getReference()
                .child(BASE_REFERENCE).child(user.getUid());
        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MyFile file = dataSnapshot.getValue(MyFile.class);
                if (file != null) {
                    Log.d(TAG, "found file: " + file.getFilename() + "; " + file.getSize() + "; " + file.getFormat());
                    files.add(file);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                MyFile file = dataSnapshot.getValue(MyFile.class);
                assert file != null;
                Log.d(TAG, "removed file: " + file.getFilename());
                files.remove(file);
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

    private void getPermission() {
        ActivityCompat.requestPermissions(FilesListActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EX_PER);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==EX_PER){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "permission read external storage ok");
            }
            else{

                Log.d(TAG, "permission read external storage denied");
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

    private void uploadImage(Intent data){
        Uri picturez = data.getData();
        Context context = getBaseContext();
        Cursor cursor = getContentResolver().query(picturez, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String absoluteFilePath = cursor.getString(idx);

        Uri file = Uri.fromFile(new File(absoluteFilePath));
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
            }
        });
    }

    // TODO: create personalized ADAPTER for showing the files of the user
}
