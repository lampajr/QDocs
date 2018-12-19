package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.polimi.proj.qdocs.R;

import java.io.File;

public class FileActivity extends AppCompatActivity {
    private static final String TAG = "FILE_ACT";
    private static final int EX_PER = 10 ;

    private static final int IMG_PRV=100;
    private static final int AUD_PRV = 200;
    private static final int VID_PRV = 300;

    private Button signOutButton;
    private Button addButton;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);


        getPermission();

        signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                final Intent login = new Intent(FileActivity.this, LoginActivity.class);
                login.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
            }
        });

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        storageRef.child("*");


        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click add button");
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), IMG_PRV);
                //startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI), AUD_PRV);
                //startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), VID_PRV);

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
                FirebaseAuth.getInstance().signOut();
                final Intent login = new Intent(FileActivity.this, LoginActivity.class);
                login.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(login);
                break;
        }
        return false;
    }

    private void getPermission() {
        ActivityCompat.requestPermissions(FileActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EX_PER);
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
                if (ContextCompat.checkSelfPermission(FileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                if (ContextCompat.checkSelfPermission(FileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
                if (ContextCompat.checkSelfPermission(FileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
        StorageReference imgRef = storageRef.child(file.getLastPathSegment());
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
        StorageReference imgRef = storageRef.child(file.getLastPathSegment());
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
        StorageReference imgRef = storageRef.child(file.getLastPathSegment());
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


}
