package com.polimi.proj.qdocs.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageElement;

import java.util.Collections;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Activity that will show the list of user's recent files
 *
 * @see ListFragment
 */
public class RecentFilesFragment extends ListFragment {
    public final String TAG = "RECENT_FILES_FRAGMENT";
    private static final int N_RECENT_FILES = 5; // number of recent files to show

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static RecentFilesFragment newInstance() {
        return new RecentFilesFragment();
    }


    @Override
    void setupListener() {
        onItemSwipeListener = new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeBottom() {
                Log.d(TAG, "swipe bottom");
            }

            @Override
            public void onSwipeLeft() {
                parentActivity.onRightRecentSwipe();
            }

            @Override
            public void onSwipeRight() {
                parentActivity.onLeftRecentSwipe();
            }

            @Override
            public void onSwipeTop() {
                Log.d(TAG, "swipe top");
            }
        };
    }

    /**
     * Loads all the user's recent files
     * @param ref reference from which retrieve files
     */
    @Override
    void loadFiles(final DatabaseReference ref, final StorageReference storageReference) {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (StorageElement.isFile(dataSnapshot)) {
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null &&
                            StorageElement.retrieveFileByName(file.getFilename(), files) == null) {
                        Log.d(TAG, "new offline file found: " + storageReference.toString() + "/" + file.getFilename());
                        file.setReference(storageReference);
                        addFileInOrder(file);
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
     * if the size of files is less than N_RECENT_FILES then the new file is simply added
     * else the new files is added at specific index such that the list is ordered by
     * lastAccess attribute of the files, then if the size is greater than N_RECENT_FILES remove
     * the last file
     * @param file new file to be added if recent
     */
    private void addFileInOrder(MyFile file) {
        Log.d(TAG, "new file to add: " + file.getLastAccess());
        files.add(file);
        Collections.sort(files);
        Collections.reverse(files);
        if (files.size() > N_RECENT_FILES) {
            // remove last file
            files.remove(files.size() - 1);
        }
    }

    /**
     * interface that has to be implemented by the main activity in order to handle
     * the swipe gesture on the OfflineFilesFragment
     */
    public interface OnRecentFilesFragmentSwipe {
        // TODO: Update argument type and name
        void onRightRecentSwipe();
        void onLeftRecentSwipe();
    }
}
