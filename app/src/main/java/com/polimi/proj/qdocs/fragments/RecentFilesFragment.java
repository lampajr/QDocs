package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.AreYouSureDialog;
import com.polimi.proj.qdocs.dialogs.BottomSheetMenu;
import com.polimi.proj.qdocs.dialogs.InfoDialog;
import com.polimi.proj.qdocs.dialogs.QrCodeDialog;
import com.polimi.proj.qdocs.listeners.OnYesListener;
import com.polimi.proj.qdocs.support.DividerDecorator;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.support.MyDirectory;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.support.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Fragment that will show the list of user's recent files
 */
public class RecentFilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public final String TAG = "RECENT_FILES_FRAGMENT";
    private static final int N_RECENT_FILES = 10; // number of recent files to show

    // the more recent the more higher
    private boolean ascending = true;

    private Context context;
    private FirebaseHelper fbHelper;

    private LinearLayout titlebar;
    private TextView titleText;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storageView;
    private StorageAdapter myStorageAdapter;
    private BottomSheetMenu bsm;

    private List<StorageElement> files;

    /**
     * Use this factory method to create a new instance of
     * RecentFilesFragment class
     */
    public static RecentFilesFragment newInstance() {
        return new RecentFilesFragment();
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
        View view = inflater.inflate(R.layout.fragment_recent_files, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        storageView = view.findViewById(R.id.storage_view);

        titlebar = view.findViewById(R.id.titlebar);
        titleText = titlebar.findViewById(R.id.title);
        titleText.setText("RECENT");

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (myStorageAdapter != null) {
            if (isVisibleToUser) {
                Log.d(TAG, "Resumed");
                //notifyAdapter();
            }
            else {
                Log.d(TAG, "Paused");
            }
        }
    }

    /**
     * Setup the RecyclerView storing the most recent used files
     */
    private void setupStorageView() {
        Log.d(TAG, "Setting up the storage view");

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));

        RecyclerView.ItemDecoration dividerItemDecoration = new DividerDecorator(ContextCompat.getDrawable(context, R.drawable.divider), 0);
        storageView.addItemDecoration(dividerItemDecoration);

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
            public void onDirectoryClick(MyDirectory dir) {
                // do nothing, no directories are showed in this list
            }

            @Override
            public void onDirectoryOptionClick(MyDirectory dir) {
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
                setupFirebaseStorageListener(fbHelper.getDatabaseReference(), fbHelper.getStorageReference());
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
        Utility.startShowFileService(context, fbHelper.getCurrentPath(file.getDbReference()) + "/" + file.getFilename(), file.getContentType());
    }

    /**
     * This method will show the settings menu of a specific file
     *
     */
    private void showFileSettingsMenu(final MyFile file) {
        Log.d(TAG, "Showing file settings menu");

        bsm = BottomSheetMenu.getInstance(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile(file);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePersonalFile(file);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQrCode(file);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfos(file);
            }
        });
        bsm.show(((MainActivity)context).getSupportFragmentManager(), "file_settings_" + file.getFilename());
    }

    /**
     * Loads all the user's recent files
     * @param ref reference from which retrieve files
     */
    private void setupFirebaseStorageListener(final DatabaseReference ref, final StorageReference storageReference) {
        swipeRefreshLayout.setRefreshing(true);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (StorageElement.isFile(dataSnapshot)) {
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null &&
                            !file.getFilename().equals(MainActivity.SECRET_FILE) &&
                            StorageElement.retrieveFileByName(file.getFilename(), files) == null) {
                        Log.d(TAG, "New file found: " + storageReference.toString() + "/" + file.getFilename());
                        file.setStReference(storageReference);
                        file.setDbReference(ref);
                        addFileInOrder(file);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    // if it is a directory call recursively this method
                    if (dataSnapshot.getKey() != null) {
                        setupFirebaseStorageListener(ref.child(dataSnapshot.getKey()), storageReference.child(dataSnapshot.getKey()));
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (StorageElement.isFile(dataSnapshot)) {
                    MyFile f = dataSnapshot.getValue(MyFile.class);
                    files.remove(StorageElement.retrieveFileByKey(f.getKey(), files));
                    notifyAdapter();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Deletes a file from list
     * @param file file to delete
     */
    private void deletePersonalFile(final MyFile file) {
        Log.d(TAG, "Deleting file: " + file.getFilename());

        if (bsm != null)
            bsm.dismiss();

        new AreYouSureDialog(context, new OnYesListener() {
            @Override
            public void onYes() {
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
        }).show();
    }

    /**
     * Start the service that will store the file on the public storage
     * @param file file to save
     */
    private void saveFile(final MyFile file) {
        Log.d(TAG, "Saving file: " + file.getFilename());

        if (bsm != null)
            bsm.dismiss();

        fbHelper.updateLastAccessAttribute(file.getKey());
        fbHelper.madeOfflineFile(file.getKey());
        //fbHelper.updateLastAccessAttribute(StorageElement.retrieveFileByName(file.getFilename(), files).getKey());
        //fbHelper.madeOfflineFile(StorageElement.retrieveFileByName(file.getFilename(), files).getKey());

        Utility.startSaveFileService(context,
                fbHelper.getCurrentPath(file.getDbReference()) + "/" + file.getFilename(),
                file.getContentType());
    }

    /**
     * if the size of files is less than N_RECENT_FILES then the new file is simply added
     * else the new files is added at specific index such that the list is ordered by
     * lastAccess attribute of the files, then if the size is greater than N_RECENT_FILES remove
     * the last file
     * @param file new file to be added if recent
     */
    private void addFileInOrder(MyFile file) {
        if (files.size() >= N_RECENT_FILES) {
            Log.d(TAG, "Maximum number of files showed reached!");
        }
        else {
            Log.d(TAG, "New file added: " + file.getFilename() + "; lastAccess: " + file.getLastAccess());
            files.add(file);
            Collections.sort(files);
            if (ascending)
                Collections.reverse(files);
            notifyAdapter();
        }
    }

    /**
     * Generates a new qrcode bitmap and show it through a dialog
     * where the user can save it locally
     * @param file file which key has to be encoded
     */
    private void showQrCode(final MyFile file) {
        Log.d(TAG, "Showing QR code");

        if (bsm != null)
            bsm.dismiss();

        QrCodeDialog dialog = new QrCodeDialog(context, null, file);
        dialog.show();
    }

    /**
     * Show a new dialog containg all the stored information about the storage element
     * @param element StorageElement for which provide infos
     */
    private void showInfos(StorageElement element) {
        Log.d(TAG, "Showing infos");

        if (bsm != null)
            bsm.dismiss();

        InfoDialog dialog = new InfoDialog(context, null, element);
        dialog.show();
    }

    /**
     * Called for refreshing the files' list
     */
    @Override
    public void onRefresh() {
        Log.d(TAG, "Refreshing!");
        files.clear();
        setupFirebaseStorageListener(fbHelper.getDatabaseReference(), fbHelper.getStorageReference());
    }

    private void notifyAdapter() {
        myStorageAdapter.notifyDataSetChanged();
    }

    /**
     * Delete a file from a filename
     * @param filename name of the file to delete
     */
    public void onDeleteFromFile(String filename) {
        final MyFile file = StorageElement.retrieveFileByName(filename, files);
        if (file != null) {
            deletePersonalFile(file);
        }
        else {
            Log.w(TAG, "File to delete not found!");
            Toast.makeText(context, "Error removing file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Change the order in which the files are showed,
     * if ascending changed to descending and viceversa
     */
    public void changeOrder() {
        ascending = !ascending;
    }
}
