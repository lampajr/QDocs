package com.polimi.proj.qdocs.fragments;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;
import com.polimi.proj.qdocs.support.Directory;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.support.StorageElement;

import java.util.ArrayList;
import java.util.List;


public class OfflineFilesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "OFFLINE_FILES_FRAGMENT";

    private Context context;
    private MainActivity parentActivity;
    private OnOfflineFilesFragmentSwipe mSwipeListener;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView storageView;
    private StorageAdapter myStorageAdapter;

    private DatabaseReference dbRef;
    private List<StorageElement> offlineFiles;
    private OnSwipeTouchListener onItemSwipeListener;

    /**
     * Required empty public constructor
     */
    public OfflineFilesFragment() {}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    // TODO: Rename and change types and number of parameters
    public static OfflineFilesFragment newInstance() {
        OfflineFilesFragment fragment = new OfflineFilesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        offlineFiles = new ArrayList<>();
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_offline_files, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        storageView = view.findViewById(R.id.storage_view);

        // load files for the first time
        setupSwipeRefresh();
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
        this.mSwipeListener = (OnOfflineFilesFragmentSwipe) context;
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
                loadOfflineFiles(dbRef);
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

        onItemSwipeListener = new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeBottom() {
                Log.d(TAG, "swipe bottom");
            }

            @Override
            public void onSwipeLeft() {
                mSwipeListener.onRightOfflineSwipe();
            }

            @Override
            public void onSwipeRight() {
                mSwipeListener.onLeftOfflineSwipe();
            }

            @Override
            public void onSwipeTop() {
                Log.d(TAG, "swipe top");
            }
        };

        storageView.setOnTouchListener(onItemSwipeListener);

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));

        myStorageAdapter = new StorageAdapter(context, offlineFiles, onItemSwipeListener, FirebaseStorage.getInstance().getReference()) {
            @Override
            public void onFileClick(MyFile file) {
                //TODO: implement showing file
            }

            @Override
            public void onFileOptionClick(MyFile file) {
                //TODO: implement showing file options
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
     * Loads all the user's offline files
     * @param ref reference from which retrieve files
     */
    private void loadOfflineFiles(final DatabaseReference ref) {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (StorageElement.isFile(dataSnapshot)) {
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null && file.isOffline()) offlineFiles.add(file);
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    // if it is a directory call recursively this method
                    loadOfflineFiles(ref.child(dataSnapshot.getKey()));
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
     * refreshes the offline files list
     */
    private void refresh() {
        offlineFiles.clear();
        loadOfflineFiles(dbRef);
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
