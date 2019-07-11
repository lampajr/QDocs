package com.polimi.proj.qdocs.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.storage.FirebaseStorage;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.AreYouSureDialog;
import com.polimi.proj.qdocs.dialogs.InfoDialog;
import com.polimi.proj.qdocs.dialogs.OfflineSheetMenu;
import com.polimi.proj.qdocs.listeners.OnYesListener;
import com.polimi.proj.qdocs.support.DividerDecorator;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.model.MyDirectory;
import com.polimi.proj.qdocs.model.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.model.StorageElement;
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
 * Copyright 2018-2019 Lamparelli Andrea & Chittò Pietro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Fragment that will show the list of user's offline files
 */

public class OfflineFilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = "OFFLINE_FILES_FRAGMENT";


    private Context context;
    private FirebaseHelper fbHelper;

    private RelativeLayout titlebar;
    private TextView titleText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storageView;
    private StorageAdapter myStorageAdapter;
    private OfflineSheetMenu fsm;

    private List<StorageElement> files;
    private int count;

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

        titlebar = view.findViewById(R.id.titlebar);
        titleText = titlebar.findViewById(R.id.title);
        titleText.setText(getString(R.string.offline));

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
                loadLocalFiles();
                ((MainActivity)context).resetNotification(1);
            } else {
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
        Uri fileUri = Uri.fromFile(new File(PathResolver.getPublicDocStorageDir(context).getAbsolutePath() + "/" + file.getFilename()));
        Utility.showFile(context, fileUri, file.getFilename().split("\\.")[0], file.getContentType(), file.getFilename().split("\\.")[1]);
    }

    /**
     * This method will show the settings menu of a specific file
     *
     */
    private void showFileSettingsMenu(final MyFile file) {
        Log.d(TAG, "Showing file settings menu");

        fsm = OfflineSheetMenu.getInstance(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocalFile(file);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfos(file);
            }
        });
        fsm.show(((MainActivity)context).getSupportFragmentManager(), "file_settings_" + file.getFilename());
    }

    private void loadLocalFiles() {
        Log.d(TAG, "Loading local files..");

        files.clear();

        File baseDirectory = PathResolver.getPublicDocStorageDir(context);
        File[] localFiles = baseDirectory.listFiles();

        if (localFiles != null && localFiles.length != 0) {
            for (File f : localFiles) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    try {
                        Path path = Paths.get(f.getAbsolutePath());
                        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                        String filename = f.getName();
                        if (filename.equals(MainActivity.SECRET_FILE)) {
                            continue;
                        }
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

        if (fsm != null)
            fsm.dismiss();

        new AreYouSureDialog(context, new OnYesListener() {
            @Override
            public void onYes() {
                File baseDirectory = PathResolver.getPublicDocStorageDir(context);

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
                            Toast.makeText(context, getString(R.string.local_file_deleted), Toast.LENGTH_SHORT).show();
                            files.remove(file);
                            notifyAdapter();
                            ((MainActivity) context).restoreBottomNavigationBar();
                        }
                        else {
                            Log.d(TAG, "Deletion failed!");
                            Toast.makeText(context, context.getString(R.string.deletion_failed), Toast.LENGTH_SHORT).show();
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
        }).show();
    }


    /**
     * Show a new dialog containg all the stored information about the storage element
     * @param element StorageElement for which provide infos
     */
    private void showInfos(StorageElement element) {
        Log.d(TAG, "Showing infos");

        if (fsm != null)
            fsm.dismiss();

        InfoDialog dialog = new InfoDialog(context, null, element);
        dialog.show();
    }

    private void notifyAdapter() {
        //loadLocalFiles();
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
            Toast.makeText(context, context.getString(R.string.error_removing_file), Toast.LENGTH_SHORT).show();
        }
    }
}
