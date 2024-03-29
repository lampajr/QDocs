package com.polimi.proj.qdocs.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.AreYouSureDialog;
import com.polimi.proj.qdocs.dialogs.DirectorySheetMenu;
import com.polimi.proj.qdocs.dialogs.InfoDialog;
import com.polimi.proj.qdocs.dialogs.InputDialog;
import com.polimi.proj.qdocs.dialogs.ProgressBarDialog;
import com.polimi.proj.qdocs.dialogs.QrCodeDialog;
import com.polimi.proj.qdocs.listeners.OnInputListener;
import com.polimi.proj.qdocs.dialogs.BottomSheetMenu;
import com.polimi.proj.qdocs.listeners.OnYesListener;
import com.polimi.proj.qdocs.support.DividerDecorator;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.model.MyDirectory;
import com.polimi.proj.qdocs.model.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.model.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.polimi.proj.qdocs.model.MyFile.emptyElement;


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
 * Fragment that provides to the user the whole storage containing all the user's files organized
 * in folders in according to what the user has done. It allows users to browse among directories
 * and retrieving information about files.
 * For each file some operation are provided, such as Delete, Get info, Get QR code, etc.
 */

public class StorageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnInputListener {

    private static final String TAG = "FILES_LIST_FRAGMENT";

    private static final String KEY_METADATA = "key_metadata";
    private static final String UID_METADATA = "uid_metadata";

    private static final int IMG_PRV = 1;
    private static final int AUD_PRV = 2;
    private static final int FILE_PRV = 3;

    private String currentPath = "";

    private FirebaseHelper fbHelper;
    private OnInputListener onInputListener;

    private final List<StorageElement> storageElements = new ArrayList<>();
    private List<StorageElement> searchList = new ArrayList<>();
    private StorageAdapter myStorageAdapter;
    private StorageAdapter searchAdapter;
    private RecyclerView storageView;
    private EditText searchBar;

    private ImageView getBackDirectoryButton;
    private TextView directoryPathText;

    private Context context;
    private MainActivity parentActivity;
    private SwipeRefreshLayout swipeRefreshLayout;
    private BottomSheetMenu bsm;
    private DirectorySheetMenu psm;

    private SpeedDialView speedDialView;

    /**
     * Required empty public constructor
     */
    public StorageFragment() {}

    public static StorageFragment newInstance() {
        return new StorageFragment();
    }

    //////////////////// OVERRIDE METHODS //////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_storage, container, false);

        fbHelper = new FirebaseHelper();

        RelativeLayout titlebar = view.findViewById(R.id.titlebar);
        directoryPathText = titlebar.findViewById(R.id.title);
        directoryPathText.setText(getString(R.string.storage));
        getBackDirectoryButton = titlebar.findViewById(R.id.get_back_directory);
        setupDirectoryLayout();

        speedDialView = view.findViewById(R.id.upload_button);
        setupSpeedDialView();

        // RecyclerView for elements
        storageView = view.findViewById(R.id.storage_view);
        setupStorageView();

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        setupSwipeRefreshListener();

        searchBar = view.findViewById(R.id.search_view);
        setupSearchBar();

        onInputListener = this;

        return view;
    }




    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        this.parentActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "FILES " + requestCode);
        //Log.d(TAG, "request code: " + requestCode);
        if (requestCode == IMG_PRV || requestCode == AUD_PRV || requestCode == FILE_PRV) {
            Log.d(TAG, "Picked a file");
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, context.getString(R.string.cannot_access_ext_storage), Toast.LENGTH_SHORT).show();
                    Log.e(TAG,"Permission denied for external storage");
                }else {
                    uploadFile(Objects.requireNonNull(data.getData()), "", R.string.uploading_file);
                }
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRefresh() {
        setupFirebaseStorageListener();
    }

    //////////////////// PRIVATE METHODS //////////////////////////////

    /**
     * Setup the Floating Action menu for uploading files online
     */
    private void setupSpeedDialView() {
        //speedDialView.inflate(R.menu.upload_file_menu);
        speedDialView.setMainFabOpenedDrawable(context.getDrawable(R.drawable.ic_close_24dp));
        speedDialView.setMainFabOpenedBackgroundColor(context.getColor(R.color.colorSecondaryLight));
        speedDialView.setMainFabClosedDrawable(context.getDrawable(R.drawable.ic_add_24dp));
        speedDialView.setMainFabClosedBackgroundColor(context.getColor(R.color.colorSecondaryLight));

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.upload_image, R.drawable.ic_picture_24dp)
                .setFabBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelClickable(false)
                .create());

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.upload_audio, R.drawable.ic_note_24dp)
                .setFabBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelClickable(false)
                .create());

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.upload_file, R.drawable.ic_document_24dp)
                .setFabBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelClickable(false)
                .create());

        speedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.create_directory, R.drawable.ic_folder_1)
                .setFabBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelBackgroundColor(context.getColor(R.color.colorSecondaryLight))
                .setLabelClickable(false)
                .create());

        speedDialView.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                switch (actionItem.getId()) {
                    case R.id.upload_image:
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), IMG_PRV);
                        return true;
                    case R.id.upload_audio:
                        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI), AUD_PRV);
                        return true;
                    case R.id.upload_file:
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, FILE_PRV);
                        return true;
                    case R.id.create_directory:
                        speedDialView.close();
                        new InputDialog(context, null, onInputListener, context.getString(R.string.insert_folder_name_caps)).show();
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void setupSearchBar() {

        searchBar.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        updateDisplayList();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                }
        );
    }
    /**
     * Setup the Swipe Refresh Listener of the recycler view
     */
    private void setupSwipeRefreshListener() {
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
                setupFirebaseStorageListener();
                notifyAdapter();
            }
        });
    }

    /**
     * setup the layout that will show the current folder
     * if it is at root level this layout is made invisible
     */
    private void setupDirectoryLayout() {
        getBackDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBackDirectory();
            }
        });
    }

    /**
     * Create a new directory by uploading a secret empty file on the FirebaseStorage
     * @param name name of the new directory
     */
    private void createDirectory(final String name) {
        Log.d(TAG, "Creating new directory");

        if (StorageElement.retrieveDirectoryByName(name, storageElements) != null) {
            // A directory with this name already exist
            Toast.makeText(context, context.getString(R.string.dir_name_already_used), Toast.LENGTH_SHORT).show();
        }
        else {
            File baseDir = PathResolver.getPublicDocStorageDir(context);
            File secretFile = new File(baseDir.getAbsolutePath() + "/" + MainActivity.SECRET_FILE);
            try {
                if (!secretFile.exists()) {
                    boolean result = secretFile.createNewFile();
                    Log.d(TAG, "Secret file created: " + result);
                }
                uploadFile(Uri.fromFile(secretFile), name, R.string.creating_directory);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload a new file on the FirebaseStorage given the Uri provided by external Activities
     * @param fileUri URI of the file to upload
     *
     */
    private void uploadFile(@NonNull final Uri fileUri, final String pathname, int progressTitleResId) {
        speedDialView.close();
        if (parentActivity.isConnected()) {
            Log.d(TAG, "file to upload at uri: " + fileUri);

            File cache = new File(parentActivity.getCacheDir().getAbsolutePath(), "documents");
            if (cache.isDirectory()) {
                for (File f : cache.listFiles()) {
                    if (f.delete()) {
                        Log.d(TAG, "App cache cleared");
                    }
                }

            }

            try {

                String p = PathResolver.getPathFromUri(context, fileUri);
                String child;

                if (p != null && !p.equals("")) {
                    Log.w(TAG, "path: " + p);
                    child = p.substring(p.lastIndexOf("/") + 1);
                } else {
                    Log.w(TAG, "uri: " + fileUri);
                    child = Objects.requireNonNull(fileUri.getLastPathSegment());
                }


                String filteredChild = filterFilename(child, 1);
                child = (filteredChild != null) ? filteredChild : child;

                StorageReference fileRef = !pathname.equals("") ?
                        fbHelper.getStorageReference().child(pathname).child(child)
                        : fbHelper.getStorageReference().child(child);

                // file information
                String contentType = context.getContentResolver().getType(fileUri);

                if (contentType == null)
                    contentType = "application/octet-stream";
                //Log.d(TAG, "content type: " + contentType);

                StorageMetadata metadata = new StorageMetadata.Builder()
                        .setContentType(contentType)
                        .setCustomMetadata(KEY_METADATA, Utility.generateCode())
                        .setCustomMetadata(UID_METADATA, fbHelper.getUserId())
                        .build();

                final UploadTask uploadTask = fileRef.putFile(fileUri, metadata);

                final ProgressBarDialog progressDialog = new ProgressBarDialog(context,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                uploadTask.cancel();
                            }
                        }, context.getString(progressTitleResId));
                progressDialog.makeCancelVisible();
                progressDialog.show();

                Log.d(TAG, "starting uploading");
                // Register observers to listen for when the download is done or if it fails
                parentActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e(TAG, exception.toString());
                                progressDialog.dismiss();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "Upload complete");
                                progressDialog.setProgress(100);
                            }
                        }).addOnCanceledListener(new OnCanceledListener() {
                            @Override
                            public void onCanceled() {
                                Log.e(TAG, "Upload canceled");
                                progressDialog.dismiss();
                            }
                        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG, "Upload paused");
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                final int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                                Log.d(TAG, "PROGRESS -> " + progress);
                                progressDialog.setProgress(progress);
                            }
                        });
                    }
                });
            } catch (NullPointerException ex) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(context, getString(R.string.you_are_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * initialize the List View that will show the list of all user's elements stored in the Firebase
     * Storage, it will add the listener on the items
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupStorageView() {
        Log.d(TAG, "Creating filesList adapter");

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));

        RecyclerView.ItemDecoration dividerItemDecoration = new DividerDecorator(ContextCompat.getDrawable(context, R.drawable.divider));
        storageView.addItemDecoration(dividerItemDecoration);

        /*
        storageView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                speedDialView.close();
                if ((scrollY - oldScrollY) != 0) {
                    View view = parentActivity.findViewById(R.id.main_frame);
                    InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });*/

        storageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                speedDialView.close();
                View view = parentActivity.findViewById(R.id.main_frame);
                InputMethodManager imm = (InputMethodManager) parentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                return false;
            }
        });

        myStorageAdapter = new StorageAdapter(context, storageElements, fbHelper.getStorageReference()) {

            @Override
            public void onFileClick(MyFile file) {
                showFile(file);
            }

            @Override
            public void onFileOptionClick(MyFile file) {
                showFileSettingsMenu(file);
            }

            @Override
            public void onDirectoryClick(MyDirectory dir) {
                openDirectory(dir.getDirectoryName());
                myStorageAdapter.updateStorageReference(fbHelper.getStorageReference());
            }

            @Override
            public void onDirectoryOptionClick(MyDirectory dir) {
                showDirectoryBottomSheetMenu(dir);
            }
        };

        searchAdapter = new StorageAdapter(context, searchList, fbHelper.getStorageReference()) {

            @Override
            public void onFileClick(MyFile file) {
                showFile(file);
            }

            @Override
            public void onFileOptionClick(MyFile file) {
                showFileSettingsMenu(file);
            }

            @Override
            public void onDirectoryClick(MyDirectory dir) {
                openDirectory(dir.getDirectoryName());
                myStorageAdapter.updateStorageReference(fbHelper.getStorageReference());
            }

            @Override
            public void onDirectoryOptionClick(MyDirectory dir) {
                showDirectoryBottomSheetMenu(dir);
            }
        };
        // set the adapter for the elements
        storageView.setAdapter(myStorageAdapter);
    }

    /**
     * Delete a file from its filename, called by MainActivity
     * @param filename name of the file to delete
     */
    public void onDeleteFromFile(String filename) {
        MyFile f = StorageElement.retrieveFileByName(filename, storageElements);
        if (f != null) {
            deletePersonalFile(f.getFilename());
        }
        else {
            Toast.makeText(context, context.getString(R.string.unable_to_find) + " " + filename, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDisplayList() {
        Log.d(TAG,"updating the list");
        String userInput = searchBar.getText().toString().toLowerCase();
        Log.d(TAG,"input: " + userInput);
        searchList.clear();
        Log.d(TAG,"files founded: " + storageElements.size());
        Log.d(TAG,"display list: " + searchList.size() + "elements");

        if (!userInput.equals("")) {
            for (StorageElement element : storageElements) {
                if (element instanceof MyFile) {
                    Log.d(TAG, "watching a file");
                    if (((MyFile) element).getFilename().toLowerCase().contains(userInput)) {

                        searchList.add(element);
                        Log.d(TAG, "files founded: " + storageElements.size());
                    }
                }
                if (element instanceof MyDirectory) {
                    Log.d(TAG, "watching a directory");
                    if (((MyDirectory) element).getDirectoryName().toLowerCase().contains(userInput)) {

                        searchList.add(element);
                        Log.d(TAG, "files founded: " + storageElements.size());
                    }
                }

            }

            storageView.setAdapter(searchAdapter);
        }
        else {
            storageView.setAdapter(myStorageAdapter);
        }
        notifyAdapter();
    }

    /**
     * load all the filesList from the Firebase Realtime Database
     * and store them into the filesList attribute.
     * implements the callback method from the realtime database
     * in order to react in case of db operation.
     */
    private void setupFirebaseStorageListener() {
        storageElements.clear();
        addElement(emptyElement);
        addElement(emptyElement.duplicate());
        // Showing refresh animation before making requests to firebase server
        swipeRefreshLayout.setRefreshing(true);

        fbHelper.getDatabaseReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (StorageElement.isFile(dataSnapshot)) {
                    // the element is a file
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null &&
                            !file.getFilename().equals(MainActivity.SECRET_FILE) &&
                            StorageElement.retrieveFileByName(file.getFilename(), storageElements) == null ) {
                        Log.d(TAG, "Adding new file: " + file.getFilename());
                        addElement(file);
                    }
                }
                else {
                    // the element is a directory
                    Log.d(TAG, "Adding new folder..");
                    MyDirectory dir = new MyDirectory(dataSnapshot.getKey());
                    if (StorageElement.retrieveDirectoryByName(dir.getDirectoryName(), storageElements) == null) {
                        addElement(dir);
                    }
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildChanged");
                // TODO: I don't know why this isn't working
                /*if (StorageElement.isFile(dataSnapshot)) {
                    // update the file
                    MyFile f = dataSnapshot.getValue(MyFile.class);
                    MyFile prevFile = StorageElement.retrieveFileByKey(f.getKey(), storageElements);
                    int idx = storageElements.indexOf(prevFile);
                    addElement(f, idx);
                    storageElements.remove(prevFile);
                }*/
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");
                if (StorageElement.isFile(dataSnapshot)) {
                    // the element to remove is a file
                    Log.d(TAG, "Removing new file..");
                    MyFile file = StorageElement.retrieveFileByKey(dataSnapshot.getValue(MyFile.class).getKey(), storageElements);
                    if (file != null) {
                        storageElements.remove(file);
                    }
                }
                else {
                    // the element to remove is a directory
                    Log.d(TAG, "Removing new folder..");
                    MyDirectory dir = StorageElement.retrieveDirectoryByName(dataSnapshot.getKey(), storageElements);
                    if (dir != null) {
                        storageElements.remove(dir);
                    }
                }
                notifyAdapter();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //do nothing
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "database error occurred: onCanceled");
            }
        });

        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * start the FileViewer which will show the file
     * @param file file to show
     */
    private void showFile(final MyFile file) {
        Log.d(TAG, "Showing file " + file.getFilename());
        fbHelper.updateLastAccessAttribute(file.getKey());

        Utility.startShowFileService(context,
                fbHelper.getCurrentPath(fbHelper.getDatabaseReference()) + "/" + file.getFilename(),
                file.getContentType());
    }

    /**
     * Start the service that will store the file on the public storage
     * @param file file to save locally
     */
    private void saveFile(MyFile file) {
        if (parentActivity.isConnected()) {
            Log.d(TAG, "Saving file: " + file.getFilename());
            if (bsm != null)
                bsm.dismiss();
            fbHelper.updateLastAccessAttribute(file.getKey());
            fbHelper.madeOfflineFile(file.getKey());

            Utility.startSaveFileService(context,
                    fbHelper.getCurrentPath(fbHelper.getDatabaseReference()) + "/" + file.getFilename(),
                    file.getContentType());
        }
        else {
            Toast.makeText(context, getString(R.string.you_are_not_connected), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes a file from list
     * @param filename name of the file to delete
     */
    private void deletePersonalFile(final String filename) {
        Log.w(TAG, "Deleting file: " + filename);

        if (bsm != null)
            bsm.dismiss();

        new AreYouSureDialog(context, new OnYesListener() {
            @Override
            public void onYes() {
                fbHelper.deletePersonalFile(null, filename, new OnFailureListener() {
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
     * Deletes a single directory, deleting all its sub-elements
     * @param path name of the directory to remove, or deep currentPath of directories
     */
    private void deletePersonalDirectory(final String path) {
        Log.d(TAG, "Deleting directory: " + path);

        if (psm != null)
            psm.dismiss();

        new AreYouSureDialog(context, new OnYesListener() {
            @Override
            public void onYes() {
                fbHelper.deletePersonalDirectory(path, new OnFailureListener() {
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
     * Generates a new qrcode bitmap and show it through a dialog
     * where the user can save it locally
     * @param filename text to encode
     */
    private void showQrCode(final String filename) {
        Log.d(TAG, "Showing QR code");

        if (bsm != null)
            bsm.dismiss();

        MyFile f = StorageElement.retrieveFileByName(filename, storageElements);
        QrCodeDialog dialog = new QrCodeDialog(context, null, f);
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

        if (psm != null)
            psm.dismiss();

        InfoDialog dialog = new InfoDialog(context, null, element);
        dialog.show();
    }


    /**
     * get back to the previous directory if any.
     */
    private void getBackDirectory() {
        setStorageAdapter();
        if (!fbHelper.isAtRoot()) {
            Log.d(TAG, "Removing a directory level");
            fbHelper.backwardDatabaseDirectory();
            fbHelper.backwardStorageDirectory();
            storageElements.clear();
            addElement(emptyElement);
            addElement(emptyElement.duplicate());
            currentPath = currentPath.substring(0, currentPath.lastIndexOf(">"));
            directoryPathText.setText(currentPath.substring(currentPath.lastIndexOf(">")+1));
            notifyAdapter();
            setupFirebaseStorageListener();
            if (fbHelper.isAtRoot()) {
                //make get back button invisible
                getBackDirectoryButton.setVisibility(View.INVISIBLE);
                directoryPathText.setText(context.getString(R.string.storage));
            }

            // update the storage adapter
            myStorageAdapter.updateStorageReference(fbHelper.getStorageReference());
        }
        else {
            Log.d(TAG, "You are already at root");
            Toast.makeText(context, getString(R.string.already_at_root_level),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * change the default directory, updating the filesList to show
     * @param directoryName name of the folder to which move to
     */
    private void openDirectory(final String directoryName) {
        setStorageAdapter();
        if (fbHelper.isAtRoot()) {
            getBackDirectoryButton.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "Changing directory, going to " + directoryName);
        fbHelper.updateDatabaseReference(directoryName);
        fbHelper.updateStorageReference(directoryName);
        storageElements.clear();
        addElement(emptyElement);
        addElement(emptyElement.duplicate());
        notifyAdapter();
        setupFirebaseStorageListener();
        currentPath = currentPath + ">" + directoryName;
        directoryPathText.setText(directoryName);
    }

    @Override
    public void onNameInserted(String name) {
        createDirectory(filterDirectoryName(name));
    }

    /**
     * Checks whether there are other files with this name, if so it changes the current one
     * @param name name to check
     * @param num current copy number
     * @return filtered name
     */
    private String filterFilename(String name, int num) {
        String add = "(" + num + ")";
        if (StorageElement.retrieveFileByName(name, storageElements) == null) {
            return name;
        }
        else {
            if (num > 1) name = name.replace("(" + (num-1) + ")", "");
            String newName;
            if (name.contains(".")) {
                String prefix = name.split("\\.")[0];
                String suffix = name.split("\\.")[1];
                newName = prefix+add+"."+suffix;
            }
            else {
                newName = name+add;
            }
            return filterFilename(newName, num+1);
        }
    }

    /**
     * Filters the name removing illegal characters
     * @param name name to filter
     * @return the filtered name
     */
    private String filterDirectoryName(String name) {
        //TODO: implement name filter
        name = name.replace("/", "_");
        name = StringUtils.deleteWhitespace(name);
        return name;
    }

    /**
     * Notifies the adapters that data has been changed
     */
    private void notifyAdapter() {
        myStorageAdapter.notifyDataSetChanged();
    }

    /**
     * Add new element at specific position
     * @param elem to be added
     * @param idx position
     */
    private void addElement(StorageElement elem, int idx) {
        storageElements.add(idx, elem);
        notifyAdapter();
    }

    /**
     * add the current element to the storageElement list
     * preserving the order: directories as first then files
     * @param elem curr element to add
     */
    private void addElement(StorageElement elem) {
        setStorageAdapter();

        if (elem instanceof MyFile &&
                StorageElement.retrieveFileByName(((MyFile)elem).getFilename(), storageElements) != null) {
            // add file in the tail of list
            addElement(elem, storageElements.size() - 1);
        }
        else {
            // add directory in the head of list
           addElement(elem, 0);
        }
    }

    /**
     * shows the bottom menu providing settings about the specific file
     * @param file file for which show settings
     */
    private void showFileSettingsMenu(final MyFile file) {
        Bitmap qrcode_bitmap = Utility.generateQrCode(file.getKey());
        bsm = BottomSheetMenu.getInstance(qrcode_bitmap,
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFile(file);
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePersonalFile(file.getFilename());
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQrCode(file.getFilename());
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
     * Shows the setting menu for a directory
     * @param dir directory object
     */
    private void showDirectoryBottomSheetMenu(final MyDirectory dir) {
        psm = DirectorySheetMenu.getInstance(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePersonalDirectory(dir.getDirectoryName());
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfos(dir);
            }
        });
        psm.show(((MainActivity)context).getSupportFragmentManager(), "directory_settings_" + dir.getDirectoryName());
    }

    private void setStorageAdapter(){
        if (storageView.getAdapter() == myStorageAdapter)
                return;
        searchBar.setText("");
        storageView.setAdapter(myStorageAdapter);
    }
}
