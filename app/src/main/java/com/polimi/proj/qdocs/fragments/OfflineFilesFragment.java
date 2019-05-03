package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.QrCodeDialog;
import com.polimi.proj.qdocs.support.Directory;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.support.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Activity that will show the list of user's offline files
 *
 * @see ListFragment
 */
//TODO: show qrcode saved locally
public class OfflineFilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "OFFLINE_FILES_FRAGMENT";


    private Context context;
    private FirebaseHelper fbHelper;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storageView;
    private StorageAdapter myStorageAdapter;

    private List<StorageElement> files;

    /**
     * Use this factory method to create a new instance of
     * OfflineFilesFragment class
     */
    public static OfflineFilesFragment newInstance() {
        return new OfflineFilesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        files = new ArrayList<>();
        fbHelper = new FirebaseHelper();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offline_files, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        storageView = view.findViewById(R.id.storage_view);

        setupStorageView();
        setupSwipeRefresh();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        onRefresh();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * Setup the RecyclerView storing the most recent used files
     */
    private void setupStorageView() {
        Log.d(TAG, "Setting up the storage view");

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));

        myStorageAdapter = new StorageAdapter(context, files, FirebaseStorage.getInstance().getReference()) {
            @Override
            public void onFileClick(MyFile file) {
                Log.d(TAG, "File " + file.getFilename() + " clicked!");
                showFile(file);
            }

            @Override
            public void onFileOptionClick(MyFile file) {
                Log.d(TAG, "Options of file " + file.getFilename() + " clicked!");
                showFileSettingsMenu(file);
            }

            @Override
            public void onDirectoryClick(Directory dir) {
                // do nothing, no directories are showed in this list
            }

            @Override
            public void onDirectoryOptionClick(Directory dir) {
                // do nothing, no directories are showed in this list
            }
        };

        storageView.setAdapter(myStorageAdapter);
    }

    /**
     * Setup the OnSwipeRefresh listenere of the storage view
     */
    private void setupSwipeRefresh() {
        Log.d(TAG, "Setting up swipe refresh listener");
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadFiles(fbHelper.getDatabaseReference(), fbHelper.getStorageReference());
                notifyAdapter();
            }
        });
    }

    /**
     * It will shoe the file using the utility method
     * @param file file obj to show
     */
    private void showFile(MyFile file) {
        Log.d(TAG, "Showing file " + file.getFilename());
        fbHelper.updateLastAccessAttribute(file.getKey());
        Utility.showFile(context, fbHelper.getCurrentPath(file.getDbReference()) + "/" + file.getFilename());
    }

    /**
     * This method will show the settings menu of a specific file
     *
     */
    private void showFileSettingsMenu(MyFile file) {
        Log.d(TAG, "Showing file settings menu");
        Utility.generateBottomSheetMenu((MainActivity)context, "SETTINGS", R.menu.offline_file_settings_menu, getOnItemMenuClickListener(file)).show();
    }

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

    /**
     * Loads all the user's offline files
     * @param ref reference from which retrieve files
     */
    // TODO: reimplement considering parsing local files rather than online files, see method below
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
                        notifyAdapter();
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

    private void notifyAdapter() {
        myStorageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        files.clear();
        loadFiles(fbHelper.getDatabaseReference(), fbHelper.getStorageReference());
    }
}
