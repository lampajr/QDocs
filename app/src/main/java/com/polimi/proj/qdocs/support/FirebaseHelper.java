package com.polimi.proj.qdocs.support;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;

import java.util.Calendar;

public class FirebaseHelper {

    public static final String BASE_REFERENCE = "documents";
    public static final String TAG = "FIREBASE_HELPER";

    private FirebaseUser user;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    public FirebaseHelper() {
        user = FirebaseAuth.getInstance().getCurrentUser();
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
                .addOnCompleteListener(onCompleteListener);
    }

    /**
     * delete a directory specified by the path, if null it identifies a directory
     * in the current directory, applying recursively deletePersonalFile to every files
     * @param path path of the directory
     * @param onFailureListener listener on Failure
     * @param onCompleteListener listener on Complete
     */
    public void deletePersonalDirectory(final String path,
                                        final OnFailureListener onFailureListener,
                                        final OnCompleteListener<Void> onCompleteListener) {
        DatabaseReference ref = path == null ? databaseReference
                : databaseReference.child(path);

        // starts removing element from the current directory
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (StorageElement.isFile(dataSnapshot)) {
                    // if it is a file delete it
                    MyFile f = dataSnapshot.getValue(MyFile.class);
                    StorageReference sr = path == null ? null : storageReference.child(path);
                    deletePersonalFile(sr, f.getFilename(), onFailureListener, onCompleteListener);
                }
                else {
                    // if it is a directory recursively call this function
                    String newPath = path == null ? dataSnapshot.getKey() : path + "/" + dataSnapshot.getKey();
                    deletePersonalDirectory(newPath, onFailureListener, onCompleteListener);
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
