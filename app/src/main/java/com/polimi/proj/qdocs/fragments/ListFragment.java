package com.polimi.proj.qdocs.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;
import com.polimi.proj.qdocs.support.Directory;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.support.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

import java.util.ArrayList;
import java.util.List;

//TODO: implement files option and method

public abstract class ListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    public final String TAG = "LIST_FRAGMENT";

    Context context;
    MainActivity parentActivity;
    FirebaseHelper fbHelper;  // Firebase Helper object

    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storageView;
    private StorageAdapter myStorageAdapter;

    List<StorageElement> files;
    OnSwipeTouchListener onItemSwipeListener;

    /**
     * Required empty public constructor
     */
    public ListFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        files = new ArrayList<>();
        fbHelper = new FirebaseHelper();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        storageView = view.findViewById(R.id.storage_view);

        // load files for the first time
        setupSwipeRefresh();
        setupListener();
        setupStorageView();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        refresh();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        this.parentActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    /**
     * setup the swipe refresh listener
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


        // Showing Swipe Refresh animation on activity create
        // As animation won't start on onCreate, post runnable is used
        swipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {
                // Fetching data from firebase database
                loadFiles(fbHelper.getDatabaseReference(), fbHelper.getStorageReference());
                myStorageAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * setup the storage view setting up the adapter
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupStorageView() {
        Log.d(TAG, "setting up the storage view");

        storageView.setOnTouchListener(onItemSwipeListener);

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));

        myStorageAdapter = new StorageAdapter(context, files, onItemSwipeListener, FirebaseStorage.getInstance().getReference()) {
            @Override
            public void onFileClick(MyFile file) {
                showFile(file);
            }

            @Override
            public void onFileOptionClick(MyFile file) {
                showFileSettingsMenu(file);
            }

            @Override
            public void onDirectoryClick(Directory dir) {
                // do nothing since there will not be any directory
            }

            @Override
            public void onDirectoryOptionClick(Directory dir) {
                // do nothing since there will not be any directory
            }
        };

        storageView.setAdapter(myStorageAdapter);
    }

    /**
     * show the given file
     * @param file file to show
     */
    private void showFile(final MyFile file) {
        Log.d(TAG, "Showing file: " + file.getFilename());
        fbHelper.updateLastAccessAttribute(StorageElement.retrieveFileByName(file.getFilename(), files).getKey());
        Utility.showFile(context, fbHelper.getCurrentPath(file.getDbReference()) + "/" + file.getFilename());
    }

    /**
     * show the bottom sheet menu for the given file
     * @param file file for which show settings
     */
    private void showFileSettingsMenu(final MyFile file) {
        Log.d(TAG, "Showing file settings menu: " + file.getFilename());
        Utility.generateBottomSheetMenu(parentActivity, parentActivity.getString(R.string.settings_string), getMenuId(), getOnItemMenuClickListener(file)).show();
    }

    /**
     * must be implemented to setup the onItemSwipeListener
     */
    abstract void setupListener();

    /**
     * provide a listener for the menu item's click
     * @param file file for which show its settings
     * @return OnMenuItemClickListener obj
     */
    abstract MenuItem.OnMenuItemClickListener getOnItemMenuClickListener(final MyFile file);

    /**
     * provide the resource id of the menu to inflate
     * @return resource id
     */
    abstract int getMenuId();

    /**
     * Loads all the user's offline files
     * @param ref reference from which retrieve files
     */
    abstract void loadFiles(final DatabaseReference ref, final StorageReference storageReference);

    /**
     * refreshes the offline files list
     */
    private void refresh() {
        files.clear();
        loadFiles(fbHelper.getDatabaseReference(), fbHelper.getStorageReference());
    }
}
