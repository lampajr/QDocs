package com.polimi.proj.qdocs.fragments;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.dialogs.QrCodeDialog;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.StorageElement;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Activity that will show the list of user's offline files
 *
 * @see ListFragment
 */
//TODO: show qrcode saved locally
public class OfflineFilesFragment extends ListFragment {
    private final String TAG = "OFFLINE_FILES_FRAGMENT";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static OfflineFilesFragment newInstance() {
        return new OfflineFilesFragment();
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
                parentActivity.onRightOfflineSwipe();
            }

            @Override
            public void onSwipeRight() {
                parentActivity.onLeftOfflineSwipe();
            }

            @Override
            public void onSwipeTop() {
                Log.d(TAG, "swipe top");
            }
        };
    }

    @Override
    MenuItem.OnMenuItemClickListener getOnItemMenuClickListener(final MyFile file) {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_option:
                        deleteOfflineFile(file);
                        break;

                    case R.id.get_qrcode_option:
                        showQrCode(file);
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
    int getMenuId() {
        return R.menu.offline_file_settings_menu;
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
                        file.setStReference(storageReference);
                        file.setDbReference(ref);
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
     * Deletes the specific file from the local directory on the smartphone
     * @param file file to delete
     */
    //TODO: rewrite the code in a better way :) you fucking nigger
    private void deleteOfflineFile(final MyFile file) {
        Log.d(TAG, "Deleting offline file: " + file.getFilename());
        File directory = PathResolver.getPublicDocFileDir(context);
        Log.d(TAG, "list of files under directory: " + Arrays.toString(directory.list()));
        File toDelete;
        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String pathname = f.getAbsolutePath();
                Log.d(TAG, "absolute path: " + f.getAbsolutePath());
                Log.d(TAG, "substring: " + pathname.substring(pathname.lastIndexOf("/") + 1));
                Log.d(TAG, "filename: " + file.getFilename());
                return pathname.substring(pathname.lastIndexOf("/") + 1).split("\\.")[0].equals(file.getFilename().split("\\.")[0]);
            }
        });
        if (files.length != 0) {
            toDelete = files[0];
            if (toDelete.exists()) {
                Log.d(TAG, "the file exists, removing it...");
                boolean result = toDelete.delete();
                if (result) {
                    Log.d(TAG, "File deleted successfully");
                    fbHelper.updateOfflineAttribute(file.getKey(), false);
                    Toast.makeText(context, "Local file removed!!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d(TAG, "The file doesn't exist");
            }
        }
        else {
            Log.d(TAG, "file not present");
        }
    }

    /**
     * Generates a new qrcode bitmap and show it through a dialog
     * where the user can save it locally
     * @param file file which key has to be encoded
     */
    private void showQrCode(final MyFile file) {
        Log.d(TAG, "Showing QR code");
        QrCodeDialog dialog = new QrCodeDialog(context, null, file);
        dialog.show();
    }

    /**
     * interface that has to be implemented by the main activity in order to handle
     * the swipe gesture on the OfflineFilesFragment
     */
    public interface OnOfflineFilesFragmentSwipe {
        void onRightOfflineSwipe();
        void onLeftOfflineSwipe();
    }
}
