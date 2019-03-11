package com.polimi.proj.qdocs.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageElement;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Activity that will show the list of user's offline files
 *
 * @see ListFragment
 */
public class OfflineFilesFragment extends ListFragment {
    private final String TAG = "OFFLINE_FILES_FRAGMENT";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static OfflineFilesFragment newInstance() {
        return new OfflineFilesFragment();
    }

    /**
     * Loads all the user's offline files
     * @param ref reference from which retrieve files
     */
    @Override
    void loadFiles(final DatabaseReference ref, final StorageReference storageReference) {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (StorageElement.isFile(dataSnapshot)) {
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null && file.isOffline() &&
                            StorageElement.retrieveFileByName(file.getFilename(), files) == null) {
                        Log.d(TAG, "new offline file found: " + storageReference.toString());
                        file.setReference(storageReference);
                        files.add(file);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    // if it is a directory call recursively this method
                    if (dataSnapshot.getKey() != null) {
                        loadFiles(ref.child(dataSnapshot.getKey()), storageReference.child(dataSnapshot.getKey()));
                    }
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
     * interface that has to be implemented by the main activity in order to handle
     * the swipe gesture on the OfflineFilesFragment
     */
    public interface OnOfflineFilesFragmentSwipe {
        // TODO: Update argument type and name
        void onRightOfflineSwipe();
        void onLeftOfflineSwipe();
    }
}
