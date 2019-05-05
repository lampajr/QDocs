package com.polimi.proj.qdocs.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cocosw.bottomsheet.BottomSheet;
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
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.dialogs.InfoDialog;
import com.polimi.proj.qdocs.dialogs.InputDialog;
import com.polimi.proj.qdocs.dialogs.ProgressBarDialog;
import com.polimi.proj.qdocs.dialogs.QrCodeDialog;
import com.polimi.proj.qdocs.listeners.DragAndDropTouchListener;
import com.polimi.proj.qdocs.listeners.OnInputListener;
import com.polimi.proj.qdocs.support.MyDirectory;
import com.polimi.proj.qdocs.support.FirebaseHelper;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.StorageAdapter;
import com.polimi.proj.qdocs.support.StorageElement;
import com.polimi.proj.qdocs.support.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: separate delete operation from this class in order to reuse them in offlineFilesFragment


public class StorageFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnInputListener {

    private static final String TAG = "FILES_LIST_FRAGMENT";

    private static final String KEY_METADATA = "key_metadata";
    private static final String UID_METADATA = "uid_metadata";

    private static final int IMG_PRV = 1;
    private static final int AUD_PRV = 2;
    private static final int FILE_PRV = 3;

    private FirebaseHelper fbHelper;
    private OnInputListener onInputListener;

    private final List<StorageElement> storageElements = new ArrayList<>();
    private StorageAdapter myStorageAdapter;
    private RecyclerView storageView;

    private LinearLayout directoryLayout;
    private ImageView getBackDirectoryButton;
    private TextView directoryPathText;
    private RelativeLayout.LayoutParams params;

    private FloatingActionMenu floatingMenu;
    private FloatingActionButton uploadGenericFileFloatingButton;

    private Context context;
    private MainActivity parentActivity;
    private SwipeRefreshLayout swipeRefreshLayout;

    // drag-and-drop upload file button
    private DragAndDropTouchListener dragAndDropListener;

    /**
     * Required empty public constructor
     */
    public StorageFragment() {}

    public static StorageFragment newInstance() {
        return new StorageFragment();
    }

    //TODO: add on the settings menu of the file the information about it
    //TODO: add settings on directories

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

        uploadGenericFileFloatingButton = view.findViewById(R.id.upload_file_button);

        dragAndDropListener = new DragAndDropTouchListener();
        setupUploadFileFloatingButton();

        fbHelper = new FirebaseHelper();

        directoryLayout = view.findViewById(R.id.directory_layout);
        directoryPathText = view.findViewById(R.id.directory_path_text);
        getBackDirectoryButton = view.findViewById(R.id.get_back_directory);

        setupDirectoryLayout();

        // RecyclerView for elements
        storageView = view.findViewById(R.id.storage_view);
        setupStorageView();
        setupSwipeListener();
        //setupFirebaseStorageListener();
        //notifyAdapter();

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        setupSwipeRefreshListener();

        onInputListener = this;

        return view;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(TAG, "FILES " + requestCode);
        if (requestCode == IMG_PRV || requestCode == AUD_PRV || requestCode == FILE_PRV) {
            Log.d(TAG, "Picked a file");
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"Permission denied for external storage");
                }else {
                    uploadFile(data.getData() , "", "Uploading file..");
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
     * setup the swipe listener in order to change the current fragment
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListener() {
        // TODO: re-add swipe listener
        //storageView.setOnTouchListener(onSwipeListener);
    }

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
        directoryLayout.setVisibility(View.INVISIBLE);
        getBackDirectoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBackDirectory();
            }
        });
    }

    /**
     * Initialize the Floating Action Button Menu that is in charge to
     * upload new filesList, picking them from gallery or 'my filesList' section
     * of the mobile phone
     */
    private void setupUploadFileFloatingButton() {
        //uploadGenericFileFloatingButton.setOnTouchListener(dragAndDropListener);

        // upload image button
        SubActionButton uploadImageButton = generateSubActionButton(R.drawable.ic_picture_24dp);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), IMG_PRV);
            }
        });

        // upload file button
        SubActionButton uploadFileButton = generateSubActionButton(R.drawable.ic_document_24dp);
        uploadFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, FILE_PRV);
            }
        });

        // upload audio button
        SubActionButton uploadAudioButton = generateSubActionButton(R.drawable.ic_note_24dp);
        uploadAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI), AUD_PRV);
            }
        });

        SubActionButton createNewDirButton = generateSubActionButton(R.drawable.ic_folder_24dp);
        createNewDirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: change this method adding a dialog
                new InputDialog(context, null, onInputListener, "INSERT FOLDER NAME").show();
            }
        });

        floatingMenu = new FloatingActionMenu.Builder(parentActivity)
                .addSubActionView(createNewDirButton)
                .addSubActionView(uploadImageButton)
                .addSubActionView(uploadAudioButton)
                .addSubActionView(uploadFileButton)
                .attachTo(uploadGenericFileFloatingButton)
                .build();
    }

    /**
     * Generates a SubActionButton that has to be added to the floating action men Button
     * @param resId image resource id
     * @return SubActionButton object
     */
    private SubActionButton generateSubActionButton(@DrawableRes int resId) {
        //TODO: change the button dimension
        LayoutParams params = new LayoutParams(150, 150);
        SubActionButton.Builder subActionBuilder = new SubActionButton.Builder(parentActivity);
        ImageView contentImage = new ImageView(context);
        contentImage.setImageResource(resId);
        return subActionBuilder.setContentView(contentImage).setLayoutParams(params).
                setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.sub_button_shape)).build();
        // TODO: check correctness ContextCompat.getDrawable(context, R.drawable.sub_button_shape)
    }

    /**
     * Create a new directory by uploading a secret empty file on the FirebaseStorage
     * @param name name of the new directory
     */
    private void createDirectory(final String name) {
        // TODO: check that the directory's name does not already exist
        Log.d(TAG, "Creating new directory");
        File baseDir = PathResolver.getPublicDocFileDir(context);
        File secretFile = new File(baseDir.getAbsolutePath() + "/" + MainActivity.SECRET_FILE);
        try {
            if (!secretFile.exists()) {
                boolean result = secretFile.createNewFile();
                Log.d(TAG, "Secret file created: " + result);
            }
            uploadFile(Uri.fromFile(secretFile), name, "Creating directory..");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Upload a new file on the FirebaseStorage given the Uri provided by external Activities
     * @param fileUri URI of the file to upload
     *
     */
    private void uploadFile(@NonNull final Uri fileUri, final String pathname, String progressTitle) {
        //TODO: check name of the file, for instance if it contains dot it cannot be uploaded
        uploadGenericFileFloatingButton.performClick();

        //Uri fileUri = data.getData();

        //String absoluteFilePath = PathResolver.getPathFromUri(context, fileUri);
        //final Uri file = Uri.fromFile(new File(absoluteFilePath));

        StorageReference fileRef = !pathname.equals("") ?
                fbHelper.getStorageReference().child(pathname).child(fileUri.getLastPathSegment())
                : fbHelper.getStorageReference().child(fileUri.getLastPathSegment());

        // file information
        final String contentType = context.getContentResolver().getType(fileUri);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata(KEY_METADATA, Utility.generateCode())
                .setCustomMetadata(UID_METADATA, fbHelper.getUserId())
                .build();

        final UploadTask uploadTask = fileRef.putFile(fileUri, metadata);

        final ProgressBarDialog progressDialog = new ProgressBarDialog(context,
                null, progressTitle);
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
                    }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "Upload paused");
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        final int progress = (int)((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        Log.d(TAG, "PROGRESS -> " + progress);
                        progressDialog.setProgress(progress);
                    }
                });
            }
        });
    }

    /**
     * initialize the List View that will show the list of all user's elements stored in the Firebase
     * Storage, it will add the listener on the items
     */
    private void setupStorageView() {
        Log.d(TAG, "Creating filesList adapter");

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));
        myStorageAdapter = new StorageAdapter(context, storageElements, fbHelper.getStorageReference()) {

            @Override
            public void onFileClick(MyFile file) {
                showFile(file);
            }

            @Override
            public void onFileOptionClick(MyFile file) {
                showFileBottomSheetMenu(file);
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
        deletePersonalFile(filename);
    }

    /**
     * load all the filesList from the Firebase Realtime Database
     * and store them into the filesList attribute.
     * implements the callback method from the realtime database
     * in order to react in case of db operation.
     */
    private void setupFirebaseStorageListener() {
        storageElements.clear();
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
                //TODO: implement onChildChanged listener on db
                Log.d(TAG, "onChildChanged");
                if (StorageElement.isFile(dataSnapshot)) {
                    // update the file
                    MyFile f = dataSnapshot.getValue(MyFile.class);
                    MyFile prevFile = StorageElement.retrieveFileByKey(f.getKey(), storageElements);
                    int idx = storageElements.indexOf(prevFile);
                    addElement(f, idx);
                    storageElements.remove(prevFile);
                }
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
        Log.d(TAG, "Saving file: " + file.getFilename());
        fbHelper.updateLastAccessAttribute(file.getKey());
        fbHelper.madeOfflineFile(file.getKey());

        Utility.startSaveFileService(context,
                fbHelper.getCurrentPath(fbHelper.getDatabaseReference()) + "/" + file.getFilename(),
                file.getContentType());
    }

    /**
     * Deletes a file from list
     * @param filename name of the file to delete
     */
    private void deletePersonalFile(final String filename) {
        //TODO: implement "are you sure?" dialog
        Log.w(TAG, "Deleting file: " + filename);
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

    /**
     * Deletes a single directory, deleting all its sub-elements
     * @param path name of the directory to remove, or deep path of directories
     */
    private void deletePersonalDirectory(final String path) {
        //TODO: implement "are you sure?" dialog
        Log.d(TAG, "Deleting directory: " + path);
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

    /**
     * Generates a new qrcode bitmap and show it through a dialog
     * where the user can save it locally
     * @param filename text to encode
     */
    private void showQrCode(final String filename) {
        Log.d(TAG, "Showing QR code");
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
        InfoDialog dialog = new InfoDialog(context, null, element);
        dialog.show();
    }


    /**
     * get back to the previous directory if any.
     */
    private void getBackDirectory() {
        if (!fbHelper.isAtRoot()) {
            Log.d(TAG, "Removing a directory level");
            fbHelper.backwardDatabaseDirectory();
            fbHelper.backwardStorageDirectory();
            storageElements.clear();
            //TODO: change directory text
            String pastText = directoryPathText.getText().toString();
            directoryPathText.setText(pastText.substring(0, pastText.lastIndexOf(">")));
            notifyAdapter();
            setupFirebaseStorageListener();
            if (fbHelper.isAtRoot()) {
                //make directory layout invisible
                directoryLayout.setVisibility(View.INVISIBLE);
                //remove layout_below attribute to filesView
                params.removeRule(RelativeLayout.BELOW);
                swipeRefreshLayout.setLayoutParams(params);
            }

            // update the storage adapter
            myStorageAdapter.updateStorageReference(fbHelper.getStorageReference());
        }
        else {
            Log.d(TAG, "you are already at root");
            Toast.makeText(context, getString(R.string.already_at_root_level),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * change the default directory, updating the filesList to show
     * @param directoryName name of the folder to which move to
     */
    private void openDirectory(final String directoryName) {
        if (fbHelper.isAtRoot()) {
            directoryLayout.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "changing directory, going to " + directoryName);
        fbHelper.updateDatabaseReference(directoryName);
        fbHelper.updateStorageReference(directoryName);
        storageElements.clear();
        notifyAdapter();
        setupFirebaseStorageListener();
        //TODO: change directory text
        String path = directoryPathText.getText().toString() + ">" + directoryName;
        directoryPathText.setText(path);
        //add layout_below attribute
        params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.directory_layout);
        swipeRefreshLayout.setLayoutParams(params);
    }

    @Override
    public void onNameInserted(String name) {
        createDirectory(name);
    }

    /**
     * Filters the pathname removing illegal characters
     * @param pathname name to filter
     * @return the filtered pathname
     */
    private String filterPathname(String pathname) {
        //TODO: implement pathname filter
        return pathname;
    }

    /**
     * Notifies the adapters that data has been changed
     */
    public void notifyAdapter() {
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
    private void showFileBottomSheetMenu(final MyFile file) {
        Utility.generateBottomSheetMenu(parentActivity,
                "SETTINGS",
                R.menu.file_settings_menu,
                new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String name = file.getFilename();
                        switch (item.getItemId()) {
                            case R.id.delete_option:
                                deletePersonalFile(name);
                                break;

                            case R.id.save_option:
                                saveFile(file);
                                break;

                            case R.id.get_qrcode_option:
                                showQrCode(name);
                                break;

                            case R.id.info_option:
                                showInfos(file);
                                break;
                        }
                        return false;
                    }
                }).show();
    }

    /**
     * Shows the setting menu for a directory
     * @param dir directory object
     */
    private void showDirectoryBottomSheetMenu(final MyDirectory dir) {
        new BottomSheet.Builder(parentActivity)
                .title(getString(R.string.settings_string))
                .sheet(R.menu.directory_settings_menu)
                .listener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete_option:
                                deletePersonalDirectory(dir.getDirectoryName());
                                break;

                            case R.id.info_option:
                                //TODO: show dialog about file infos
                                break;
                        }
                        return false;
                    }
                }).show();
    }

    /**
     * Shows a dialog which will show to user the infos
     * about the specific file
     * @param file file from which get infos
     */
    private void showFileInfoDialog(MyFile file) {
        //TODO: show infos about file
    }

    private void showDirInfoDialog(MyDirectory dir) {
        //TODO: show infos about directory
    }
}
