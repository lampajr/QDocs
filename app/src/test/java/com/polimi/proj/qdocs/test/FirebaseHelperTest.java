package com.polimi.proj.qdocs.test;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.support.FirebaseHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FirebaseHelperTest {

    private FirebaseAuth auth;
    private String email, pwd;
    private FirebaseHelper fbHelper;
    private DatabaseReference dbRef;
    private StorageReference stRef;


    @Before
    public void setUp() throws Exception {
        // setup the firebase authentication
        auth = FirebaseAuth.getInstance();
        email = "tester@qdocs.it";
        pwd = "pippo5";
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                fbHelper = new FirebaseHelper();
                stRef = FirebaseStorage.getInstance().getReference();
                dbRef = FirebaseDatabase.getInstance().getReference();
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        auth.signOut();
    }

    @Test
    public void getUser() {
        FirebaseUser user = fbHelper.getUser();
        assertNotNull(user);
    }

    @Test
    public void getUserId() {
        String uid = fbHelper.getUser().getUid();
        assertEquals("User ID", uid, fbHelper.getUserId());
    }

    @Test
    public void getStorageReference() {

    }

    @Test
    public void getDatabaseReference() {
    }

    @Test
    public void updateDatabaseReference() {
    }

    @Test
    public void updateStorageReference() {
    }

    @Test
    public void backwardDatabaseDirectory() {
    }

    @Test
    public void backwardStorageDirectory() {
    }

    @Test
    public void isAtRoot() {
    }

    @Test
    public void madeOfflineFile() {
    }

    @Test
    public void updateOfflineAttribute() {
    }

    @Test
    public void updateLastAccessAttribute() {
    }

    @Test
    public void deletePersonalFile() {
    }

    @Test
    public void deletePersonalDirectory() {
    }

    @Test
    public void getCurrentPath() {
    }
}