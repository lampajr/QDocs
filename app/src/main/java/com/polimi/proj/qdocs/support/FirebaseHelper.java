package com.polimi.proj.qdocs.support;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.model.MyFile;
import com.polimi.proj.qdocs.model.StorageElement;

import java.util.Calendar;

/**
 * Copyright 2018-2019 Lamparelli Andrea & Chittò Pietro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Helper class that handles all the interaction between the application and the Firebase database
 * and storage.
 */

public class FirebaseHelper {

    private static final String BASE_REFERENCE = "documents";
    public static final String TAG = "FIREBASE_HELPER";

    public static final int TIMEOUT = 2000;

    private FirebaseUser user;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(TIMEOUT);
        FirebaseStorage.getInstance().setMaxDownloadRetryTimeMillis(TIMEOUT);
        storageReference = FirebaseStorage.getInstance().getReference().child(user.getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(BASE_REFERENCE)
                .child(user.getUid());
    }

    ///////////////////// GETTER /////////////////////////

    public FirebaseUser getUser() {
        return user;
    }

    public String getUserId() {
        return user.getUid();
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

    ///////////////////////////////////////////////////////

    public void updateDatabaseReference(String child) {
        databaseReference = databaseReference.child(child);
    }

    public void updateStorageReference(String child) {
        storageReference = storageReference.child(child);
    }

    public void backwardDatabaseDirectory() {
        databaseReference = databaseReference.getParent();
    }

    public void backwardStorageDirectory() {
        storageReference = storageReference.getParent();
    }

    /**
     * tells if the user is in the root directory or not
     * @return true if root dir, false otherwise
     */
    public boolean isAtRoot() {
        return databaseReference.getKey().equals(getUserId());
    }

    /**
     * set the offline attribute to true for the file that has the specific key
     * @param key key of the file
     */
    public void madeOfflineFile(final String key) {
        updateOfflineAttribute(key, true);
    }

    public void logout(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {

                LoginManager.getInstance().logOut();

            }
        }).executeAsync();
    }

    /**
     * set the offline attribute to true for the file that has the specific key
     * @param key key of the file
     */
    public void updateOfflineAttribute(final String key, final boolean newValue) {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(key)) {
                    // i've found the File to update
                    dataSnapshot.child(MyFile.OFFLINE).getRef()
                            .setValue(newValue);
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
     * updates the lastAccess attribute for the file which have the following key
     * @param key file's key
     */
    public void updateLastAccessAttribute(final String key) {
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals(key)) {
                    // i've found the File to update
                    dataSnapshot.child(MyFile.LAST_ACCESS).getRef()
                            .setValue(Calendar.getInstance().getTimeInMillis());
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
     * deletes a single file from the specific directory
     * @param ref if null remove file from current directory
     * @param filename name of the file to remove
     * @param onFailureListener on Failure listener
     * @param onCompleteListener on Complete listener
     */
    public void deletePersonalFile(StorageReference ref, final String filename,
                                   OnFailureListener onFailureListener,
                                   OnCompleteListener<Void> onCompleteListener) {
        if (ref == null) {
            // remove the file from the current directory
            ref = storageReference;
        }
        Log.d(TAG, "Removing file at " + ref.getPath() + "/" + filename);
        ref.child(filename).delete().addOnFailureListener(onFailureListener)
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Log.w(TAG, "Deletion canceled");
                    }
                })
                .addOnCompleteListener(onCompleteListener);
    }

    /**
     * delete a directory specified by the path, if null it identifies a directory
     * in the current directory, applying recursively deletePersonalFile to every files
     * @param dataSnapshot default DataSnapshot obj
     * @param path current path
     * @param onFailureListener listener on Failure
     * @param onCompleteListener listener on Complete
     */
    private void deleteDirectory(final DataSnapshot dataSnapshot,
                                 String path,
                                 final OnFailureListener onFailureListener,
                                 final OnCompleteListener<Void> onCompleteListener) {
        path = path == null ? dataSnapshot.getKey() : path + "/" + dataSnapshot.getKey();
        for (DataSnapshot ds : dataSnapshot.getChildren()) {
            if (StorageElement.isFile(ds)) {
                MyFile f = ds.getValue(MyFile.class);
                deletePersonalFile(storageReference.child(path), f.getFilename(), onFailureListener, onCompleteListener);
            } else {
                // if it is a directory recursively call this function
                deleteDirectory(ds, path, onFailureListener, onCompleteListener);
            }
        }
    }


    /**
     * delete a directory specified by the name, if null it identifies a directory
     * in the current directory, applying recursively deletePersonalFile to every files
     * @param name name of the directory
     * @param onFailureListener listener on Failure
     * @param onCompleteListener listener on Complete
     */
    public void deletePersonalDirectory(final String name,
                                        final OnFailureListener onFailureListener,
                                        final OnCompleteListener<Void> onCompleteListener) {
        databaseReference.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deleteDirectory(dataSnapshot, null, onFailureListener, onCompleteListener);
                Log.d(TAG, "Directory " + name + " correctly removed!!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Return the current path, from the root to the current directory
     * @return the path string
     */
    public String getCurrentPath(DatabaseReference ref) {
        if (ref.getKey().equals(getUserId()))
            return "";
        else
            return getCurrentPath(ref.getParent()) + "/" + ref.getKey();
    }
}
