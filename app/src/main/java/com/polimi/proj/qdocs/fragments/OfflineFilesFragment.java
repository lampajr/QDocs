package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
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

import com.google.firebase.storage.FirebaseStorage;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Fragment that will show the list of user's offline files
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (myStorageAdapter != null) {
            if (isVisibleToUser) {
                Log.d(TAG, "Resumed");
                loadLocalFiles();            }
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
                loadLocalFiles();
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
        Uri fileUri = Uri.fromFile(new File(PathResolver.getPublicDocFileDir(context).getAbsolutePath() + "/" + file.getFilename()));
        Utility.showFile(context, fileUri, file.getFilename().split("\\.")[0], file.getContentType(), file.getFilename().split("\\.")[1]);
    }

    /**
     * This method will show the settings menu of a specific file
     *
     */
    private void showFileSettingsMenu(MyFile file) {
        Log.d(TAG, "Showing file settings menu");
        Utility.generateBottomSheetMenu((MainActivity)context, context.getString(R.string.settings_string), R.menu.offline_file_settings_menu, getOnItemMenuClickListener(file)).show();
    }

    MenuItem.OnMenuItemClickListener getOnItemMenuClickListener(final MyFile file) {
        return new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_option:
                        deleteLocalFile(file);
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


    private void loadLocalFiles() {
        Log.d(TAG, "Loading local files..");

        File baseDirectory = PathResolver.getPublicDocFileDir(context);
        File[] localFiles = baseDirectory.listFiles();

        if (localFiles != null && localFiles.length != 0) {
            for (File f : localFiles) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Path path = Paths.get(f.getAbsolutePath());
                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                        String filename = f.getName();
                        String size = Files.size(path) + "";
                        Long lastAccess = attrs.lastAccessTime().toMillis();
                        String contentType = Files.probeContentType(path);
                        String savedAt = attrs.lastModifiedTime() + "";

                        if (StorageElement.retrieveFileByName(filename, files) == null) {
                            files.add(new MyFile(filename, contentType, null, size, savedAt, lastAccess, true));
                            notifyAdapter();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Delete a local file from the local public directory
     * @param file file to delete
     */
    private void deleteLocalFile(final MyFile file) {
        Log.d(TAG, "Deleting local file: " + file.getFilename());
        File baseDirectory = PathResolver.getPublicDocFileDir(context);

        // get local files that matches the filename of the file to delete
        File[] localFiles = baseDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String pathname = f.getAbsolutePath();
                return pathname.substring(pathname.lastIndexOf("/") + 1).split("\\.")[0].equals(file.getFilename().split("\\.")[0]);
            }
        });

        File toDelete;
        if (localFiles.length != 0) {
            if ((toDelete = localFiles[0]).exists()) {
                Log.d(TAG,"Removing local file named " + file.getFilename() + "..");
                if (toDelete.delete()) {
                    Log.d(TAG, "Local file deleted successfully!");
                    fbHelper.updateOfflineAttribute(file.getKey(), false);
                    Toast.makeText(context, "Local file deleted!", Toast.LENGTH_SHORT).show();
                    notifyAdapter();
                }
                else {
                    Log.d(TAG, "Deletion failed!");
                    Toast.makeText(context, "Deletion failed!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Log.d(TAG, "File not found!");
            }
        }
        else {
            Log.d(TAG, "No matching files found!");
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
        loadLocalFiles();
        myStorageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        files.clear();
        loadLocalFiles();
    }

    /**
     * Delete a file from a filename
     * @param filename name of the file to delete
     */
    public void onDeleteFromFile(String filename) {
        MyFile file = StorageElement.retrieveFileByName(filename, files);
        if (file != null) {
            deleteLocalFile(file);
        }
        else {
            Log.w(TAG, "File to delete not found!");
            Toast.makeText(context, "Error removing file", Toast.LENGTH_SHORT).show();
        }
    }
}
