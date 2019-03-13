package com.polimi.proj.qdocs.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

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
    int getMenuId() {
        return R.menu.file_settings_menu;
    }

    @Override
    MenuItem.OnMenuItemClickListener getOnItemMenuClickListener(final MyFile file) {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_option:
                        deletePersonalFile(file);
                        break;

                    case R.id.save_option:
                        saveFile(file);
                        break;

                    case R.id.get_qrcode_option:
                        //showQrCode(name);
                        break;

                    case R.id.info_option:
                        //TODO: show dialog about file infos
                        break;
                }
                return false;
            }
        };
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
                        file.setStReference(storageReference);
                        file.setDbReference(ref);
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
     * Deletes a file from list
     * @param file file to delete
     */
    private void deletePersonalFile(final MyFile file) {
        //TODO: implement "are you sure?" dialog
        Log.d(TAG, "Deleting file: " + file.getFilename());
        fbHelper.deletePersonalFile(file.getStReference(), file.getFilename(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure occurred during file removing");
                Toast.makeText(context, getString(R.string.delition_failed), Toast.LENGTH_SHORT)
                        .show();
            }
        }, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "File correctly removed!");
            }
        });
    }

    /**
     * Start the service that will store the file on the public storage
     * @param file file to save
     */
    private void saveFile(final MyFile file) {
        Log.d(TAG, "Saving file: " + file.getFilename());
        fbHelper.updateLastAccessAttribute(StorageElement.retrieveFileByName(file.getFilename(), files).getKey());
        fbHelper.updateOfflineAttribute(StorageElement.retrieveFileByName(file.getFilename(), files).getKey());

        Utility.saveFile(context,
                fbHelper.getCurrentPath(file.getDbReference()) + "/" + file.getFilename());
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
