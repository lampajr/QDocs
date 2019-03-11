package com.polimi.proj.qdocs.support;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {

    public static final String BASE_REFERENCE = "documents";

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
}
