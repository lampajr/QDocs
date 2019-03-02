package com.polimi.proj.qdocs.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.activities.MainActivity;
import com.polimi.proj.qdocs.services.DownloadFileService;
import com.polimi.proj.qdocs.services.SaveFileReceiver;
import com.polimi.proj.qdocs.services.ShowFileReceiver;
import com.polimi.proj.qdocs.support.Directory;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.StorageElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class FilesListFragment extends Fragment {

    private static final String TAG = "FILES_LIST_FRAGMENT";

    private static final String BASE_REFERENCE = "documents";
    private static final String KEY_METADATA = "key_metadata";
    private static final String UID_METADATA = "uid_metadata";


    private static final int IMG_PRV = 100;
    private static final int AUD_PRV = 200;
    private static final int FILE_PRV = 300;

    private StorageReference storageRef;

    private final List<MyFile> files = new ArrayList<>();
    private final List<Directory> directories = new ArrayList<>();
    //TODO: order elements, as first folder then files
    private final List<StorageElement> storageElements = new ArrayList<>();
    private FilesListFragment.StorageAdapter storageAdapter;
    private RecyclerView storageView;

    private NumberProgressBar uploadProgressBar;

    private LinearLayout directoryLayout;
    private ImageView getBackDirectoryButton;
    private TextView directoryPathText;
    private RelativeLayout.LayoutParams params;

    private FloatingActionMenu floatingMenu;
    private FloatingActionButton uploadGenericFileFloatingButton;

    private FirebaseUser user;
    private DatabaseReference dbRef;
    private Context context;
    private MainActivity parentActivity;

    /**
     * Required empty public constructor
     */
    public FilesListFragment() {}

    public static FilesListFragment newInstance() {
        FilesListFragment fragment = new FilesListFragment();
        return fragment;
    }


    //////////////////// OVERRIDE METHODS //////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_files_list, container, false);

        uploadGenericFileFloatingButton = view.findViewById(R.id.upload_file_button);
        setupUploadFileFloatingButton();

        user = FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid());

        dbRef = FirebaseDatabase.getInstance().getReference()
                .child(BASE_REFERENCE).child(user.getUid());

        // get progress bar
        uploadProgressBar = view.findViewById(R.id.number_progress_bar);

        directoryLayout = view.findViewById(R.id.directory_layout);
        directoryPathText = view.findViewById(R.id.directory_path_text);
        getBackDirectoryButton = view.findViewById(R.id.get_back_directory);

        setupDirectoryLayout();

        // RecyclerView for elements
        storageView = view.findViewById(R.id.files_view);
        loadStorageElements();
        initFilesList();

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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMG_PRV || requestCode == AUD_PRV || requestCode == FILE_PRV) {
            Log.d(TAG, "Picked a file");
            if (resultCode == Activity.RESULT_OK) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG,"Permission denied for external storage");
                }else {
                    showPathnameChooserDialog(data);
                }
            }
        }
    }


    //////////////////// PRIVATE METHODS //////////////////////////////

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
        // upload image button
        SubActionButton uploadImageButton = generateSubActionButton(R.drawable.galley);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.INTERNAL_CONTENT_URI), IMG_PRV);
            }
        });

        // upload file button
        SubActionButton uploadFileButton = generateSubActionButton(R.drawable.file_image);
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
        SubActionButton uploadAudioButton = generateSubActionButton(R.drawable.audio_image);
        uploadAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.INTERNAL_CONTENT_URI), AUD_PRV);
            }
        });

        floatingMenu = new FloatingActionMenu.Builder(parentActivity)
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
        SubActionButton.Builder subActionBuilder = new SubActionButton.Builder(parentActivity);
        ImageView contentImage = new ImageView(context);
        contentImage.setImageResource(resId);
        return subActionBuilder.setContentView(contentImage).build();
    }

    /**
     * Upload a new file on the FirebaseStorage given the Uri provided by external Activities
     * @param data data of the file to upload
     *
     */
    private void uploadFile(Intent data, final String pathname) {
        uploadGenericFileFloatingButton.performClick();

        Uri fileUri = data.getData();

        String absoluteFilePath = PathResolver.getPathFromUri(context, fileUri);

        final Uri file = Uri.fromFile(new File(absoluteFilePath));

        StorageReference fileRef = !pathname.equals("") ?
                storageRef.child(pathname).child(file.getLastPathSegment())
                : storageRef.child(file.getLastPathSegment());

        // file information
        final String contentType = context.getContentResolver().getType(fileUri);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(contentType)
                .setCustomMetadata(KEY_METADATA, generateCode())
                .setCustomMetadata(UID_METADATA, user.getUid())
                .build();

        final UploadTask uploadTask = fileRef.putFile(file, metadata);

        final RelativeLayout.LayoutParams params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ABOVE, R.id.upload_file_button);
        storageView.setLayoutParams(params);
        uploadProgressBar.setVisibility(View.VISIBLE);

        int fileSize = Integer.parseInt(String.valueOf((new File(absoluteFilePath)).length()/1024));
        //uploadProgressBar.setMax(fileSize);

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
                        uploadProgressBar.setProgress(1000);
                        uploadProgressBar.setVisibility(View.INVISIBLE);
                        uploadProgressBar.setProgress(0);
                        params.removeRule(RelativeLayout.ABOVE);
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
                        final double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "PROGRESS -> " + progress);
                        uploadProgressBar.setProgress((int)progress * 10);
                    }
                });
            }
        });
    }

    /**
     * initialize the List View that will show the list of all user's elements stored in the Firebase
     * Storage, it will add the listener on the items
     */
    private void initFilesList() {
        Log.d(TAG, "Creating filesList adapter");

        storageView.setHasFixedSize(true);
        storageView.setLayoutManager(new LinearLayoutManager(context));
        storageAdapter = new FilesListFragment.StorageAdapter(context, storageElements);
        // set the adapter for the elements
        storageView.setAdapter(storageAdapter);
    }

    /**
     * load all the filesList from the Firebase Realtime Database
     * and store them into the filesList attribute.
     * implements the callback method from the realtime database
     * in order to react in case of db operation.
     */
    private void loadStorageElements() {

        dbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded : key found -> " + dataSnapshot.getKey());
                if (dataSnapshot.getKey().matches("\\d+")) {
                    // the element is a file
                    Log.d(TAG, "adding new file..");
                    MyFile file = dataSnapshot.getValue(MyFile.class);
                    if (file != null) {
                        Log.d(TAG, "Found new file: " + file.getKey() + "; " + file.getFilename() +
                                "; " + file.getContentType() + "; " + file.getSize() +
                                "; " + file.getTime());
                        files.add(file);
                        storageElements.add(file);
                    }
                }
                else {
                    // the element is a directory
                    Log.d(TAG, "adding new folder..");
                    Directory dir = new Directory(dataSnapshot.getKey());
                    directories.add(dir);
                    storageElements.add(dir);
                }
                notifyAdapters();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TODO: implement onChildChanged listener on db
                Log.d(TAG, "onChildChanged");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved");
                if (dataSnapshot.getKey().matches("\\d+")) {
                    // the element to remove is a file
                    Log.d(TAG, "removing new file..");
                    MyFile file = retrieveFileByKey(dataSnapshot.getValue(MyFile.class).getKey());
                    if (file != null) {
                        files.remove(file);
                        storageElements.remove(file);
                    }
                }
                else {
                    // the element to remove is a directory
                    Log.d(TAG, "removing new folder..");
                    Directory dir = retrieveDirectoryByName(dataSnapshot.getKey());
                    if (dir != null) {
                        directories.remove(dir);
                        storageElements.remove(dir);
                    }
                }
                notifyAdapters();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //TODO: implement onChildMoved
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "database error occurred: onCanceled");
            }
        });
    }

    /**
     * start the FileViewer which will show the file
     * @param filename name of the file to show
     */
    private void startShowingFileService(String filename) {
        Log.d(TAG, "showing file " + filename);
        Intent viewerIntentService = new Intent(parentActivity, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        filename = getCurrentPath(dbRef) + "/" + filename;

        // create the result receiver for the IntentService
        ShowFileReceiver receiver = new ShowFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        context.startService(viewerIntentService);
    }

    /**
     * Start the service that will store the file on the public storage
     * @param filename name of the file
     */
    private void startingSavingFileService(String filename) {
        Intent viewerIntentService = new Intent(parentActivity, DownloadFileService.class);

        viewerIntentService.setAction(DownloadFileService.ACTION_DOWNLOAD_TMP_FILE);

        filename = getCurrentPath(dbRef) + "/" + filename;

        // create the result receiver for the IntentService
        SaveFileReceiver receiver = new SaveFileReceiver(context, new Handler());
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_RECEIVER, receiver);
        viewerIntentService.putExtra(DownloadFileService.EXTRA_PARAM_FILENAME, filename);
        context.startService(viewerIntentService);
    }

    /**
     * Return the current path, from the root to the current directory
     * @return the path string
     */
    private String getCurrentPath(DatabaseReference ref) {
        if (ref.getKey().equals(user.getUid()))
            return "";
        else
            return getCurrentPath(ref.getParent()) + "/" + ref.getKey();
    }

    /**
     * Deletes a file from list
     * @param filename name of the file to delete
     */
    private void deletePersonalFile(final String filename) {
        //TODO: implement "are you sure?" dialog

        Log.d(TAG, "deleting file: " + filename);
        storageRef.child(filename).delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failure occurred during file removing");
                Toast.makeText(context, getString(R.string.delition_failed), Toast.LENGTH_SHORT)
                        .show();
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "File correctly removed!");
            }
        });
    }

    /**
     * Generates a new qrcode bitmap
     * @param filename text to encode
     */
    private void showQrCode(final String filename) {
        Log.d(TAG, "getting qrcode");
        MyFile f = retrieveFileByName(filename);
        final Bitmap qrCode = generateQrCode(f.getKey());
        if (qrCode != null) {
            Log.d(TAG, "showing qrcode dialog..");
            final Dialog d = new Dialog(context);
            d.setTitle(getString(R.string.pathname_chooser));
            d.setCancelable(true);
            d.setContentView(R.layout.show_qrcode_dialog);
            ImageView qrcodeImage = d.findViewById(R.id.qrcode_iamge);
            qrcodeImage.setImageBitmap(qrCode);
            Button saveButton = d.findViewById(R.id.save_button);
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            File dst = new File(PathResolver.createPublicDocStorageDir(context).getAbsolutePath(),
                                    "QRCode-" + filename);

                            //File dst = new File(PathResolver.getPublicDocFileDir(FilesListActivity.this).getAbsolutePath(), "QRCode-" + filename);
                            if (!dst.exists()) {
                                //TODO: 2 filesList with same name but different folder cannot produce 2 qr code different
                                try (FileOutputStream out = new FileOutputStream(dst)) {
                                    qrCode.compress(Bitmap.CompressFormat.PNG, 100, out); // qrCode is the Bitmap instance
                                    // PNG is a lossless format, the compression factor (100) is ignored
                                    d.dismiss();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                Log.d(TAG, "QRCode already saved");
                                Toast.makeText(context,
                                        getString(R.string.file_already_stored),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
            d.show();
        }
    }

    /**
     * get back to the previous directory if any.
     */
    private void getBackDirectory() {
        if (!dbRef.getKey().equals(user.getUid())) {
            Log.d(TAG, "Removing a directory level");
            dbRef = dbRef.getParent();
            storageRef = storageRef.getParent();
            files.clear();
            directories.clear();
            storageElements.clear();
            //TODO: change directory text
            String pastText = directoryPathText.getText().toString();
            directoryPathText.setText(pastText.substring(0, pastText.lastIndexOf("/")));
            notifyAdapters();
            loadStorageElements();
            if (dbRef.getKey().equals(user.getUid())) {
                //TODO: make directory layout invisible
                directoryLayout.setVisibility(View.INVISIBLE);
                //TODO: remove layout_below attribute to filesView
                params.removeRule(RelativeLayout.BELOW);
                storageView.setLayoutParams(params);
            }
        }
        else {
            Log.d(TAG, "you are already at root");
            Toast.makeText(context, getString(R.string.already_at_root_level),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * change the default directory, updating the filesList to show
     * @param folderName name of the folder to which move to
     */
    private void openDirectory(final String folderName) {
        if (dbRef.getKey().equals(user.getUid())) {
            directoryLayout.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "changing directory, going to " + folderName);
        dbRef = dbRef.child(folderName);
        storageRef = storageRef.child(folderName);
        files.clear();
        directories.clear();
        storageElements.clear();
        notifyAdapters();
        loadStorageElements();
        //TODO: change directory text
        String path = directoryPathText.getText().toString() + "/" + folderName;
        directoryPathText.setText(path);
        //TODO: add layout_below attribute
        params= new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, R.id.directory_layout);
        storageView.setLayoutParams(params);
    }

    /**
     * Show a dialog that will ask to the user the pathname of the file
     * that is going to be uploaded, the pathname consists of the folder,
     * even if it doesn't exist or null if the current folder is ok.
     * if user confirms then the file can be uploaded
     * @param data data to upload
     *
     */
    private void showPathnameChooserDialog(final Intent data) {
        Log.d(TAG, "starting pathname chooser dialog..");
        final Dialog d = new Dialog(context);
        d.setTitle(getString(R.string.pathname_chooser));
        d.setCancelable(true);
        d.setContentView(R.layout.pathname_chooser_dialog);

        final EditText pathnameText = d.findViewById(R.id.pathname_text);

        Button confirmButton = d.findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pathname = filterPathname(pathnameText.getText().toString());
                Log.d(TAG, "pathname inserted : " + pathname);
                uploadFile(data, pathname);
                d.dismiss();
            }
        });

        Button cancelButton = d.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
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
    public void notifyAdapters() {
        storageAdapter.notifyDataSetChanged();
    }

    /**
     * Generate a new code from which provide a new qrcode to
     * associate to a new file
     * @return the code
     */
    private String generateCode() {
        long time = Calendar.getInstance().getTimeInMillis();
        String code = time + "";// + "" + new Random().nextLong();
        Log.d(TAG, "new code: " + code);
        return code;
    }

    /**
     * generates a new qrcode
     * @param key text to encode
     * @return the qr code bitmap
     */
    private Bitmap generateQrCode(String key) {
        try {
            Log.d(TAG, "encoding " + key + " into a new QR Code");
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmapQrCode = barcodeEncoder.encodeBitmap(key, BarcodeFormat.QR_CODE, 400, 400);
            Log.d(TAG, "QR Code generated!");
            return bitmapQrCode;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve a MyFile object matching the filename passed as paramater
     * @param filename name of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public MyFile retrieveFileByName(String filename) {
        for(MyFile f : files) {
            if (f.getFilename().equals(filename)) return f;
        }
        return null;
    }

    /**
     * Retrieve a MyFile object matching the key passed as paramater
     * @param key key of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public MyFile retrieveFileByKey(String key) {
        for(MyFile f : files) {
            if (f != null && f.getKey() != null
                    && f.getKey().equals(key)) return f;
        }
        return null;
    }

    /**
     * Retrieve a Directory object from the filesList attribute
     * @param name name to check, is unique for folders
     * @return the Directory obj
     */
    public Directory retrieveDirectoryByName(String name) {
        for(Directory d : directories) {
            if (d != null && d.getDirectoryName() != null
                    && d.getDirectoryName().equals(name)) return d;
        }
        return null;
    }

    /**
     * RecyclerView adapter for the elements' list
     */
    private class StorageAdapter extends RecyclerView.Adapter<FilesListFragment.StorageAdapter.dataViewHolder> {
        private LayoutInflater inflater;
        private List<StorageElement> elements;
        private Context context;

        StorageAdapter(Context context, List<StorageElement> elements) {
            this.inflater = LayoutInflater.from(context);
            this.elements = elements;
            this.context = context;

            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @NonNull
        @Override
        public FilesListFragment.StorageAdapter.dataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            //TODO: rearrange item file layout
            View view = inflater.inflate(R.layout.item_storage_element, parent, false);
            return new FilesListFragment.StorageAdapter.dataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final FilesListFragment.StorageAdapter.dataViewHolder holder, int position) {
            holder.bindData(elements.get(position));
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        /**
         * Holder for the File element
         */
        class dataViewHolder extends RecyclerView.ViewHolder{
            // Item-row elements
            TextView elementNameView, elementDescriptionView, elementOptionView;
            ImageView elementImage;
            CardView elementCardView;

            dataViewHolder(@NonNull View itemView) {
                super(itemView);
                elementNameView = itemView.findViewById(R.id.element_name);
                elementDescriptionView = itemView.findViewById(R.id.element_description);
                elementOptionView = itemView.findViewById(R.id.element_options);
                elementImage = itemView.findViewById(R.id.element_image);
                elementCardView = itemView.findViewById(R.id.element_card);
            }

            void bindData(final StorageElement element) {
                elementImage.setImageDrawable(null);
                //TODO: set onClick animation on the items
                if (element instanceof MyFile) {
                    final MyFile file = (MyFile) element;
                    elementNameView.setText(file.getFilename());
                    elementDescriptionView.setText(file.getContentType());

                    if (file.getContentType().contains("image")) {
                        // preview image for image file
                        storageRef.child(file.getFilename()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(TAG, "preview image loaded successfully");
                                Glide.with(context).load(uri).into(elementImage);
                            }
                        });
                    }
                    else if (file.getContentType().contains("audio")) {
                        // image for audio
                        elementImage.setImageResource(R.drawable.ic_mic_24dp);
                    }
                    else if (file.getContentType().contains("pdf")) {
                        //TODO: set image for pdf
                        elementImage.setImageResource(R.drawable.ic_tmp_pdf_24dp);
                    }
                    else {
                        //TODO: set image for another file type
                        elementImage.setImageResource(R.drawable.ic_unsupported_file_24dp);
                    }

                    elementCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startShowingFileService(file.getFilename());
                        }
                    });

                    elementOptionView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // will show popup menu here
                            PopupMenu fileSettingsPopup = new PopupMenu(context, elementOptionView);
                            fileSettingsPopup.inflate(R.menu.file_settings_menu);

                            fileSettingsPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    String name = file.getFilename();
                                    switch (menuItem.getItemId()) {
                                        case R.id.delete_option:
                                            deletePersonalFile(name);
                                            break;

                                        case R.id.save_option:
                                            startingSavingFileService(name);
                                            break;

                                        case R.id.get_qrcode_option:
                                            showQrCode(name);
                                            break;
                                    }
                                    return false;
                                }
                            });

                            if (fileSettingsPopup.getMenu() instanceof MenuBuilder) {
                                MenuBuilder helper = (MenuBuilder) fileSettingsPopup.getMenu();
                                helper.setOptionalIconsVisible(true);
                            }
                            /*
                            MenuPopupHelper fileSettingsMenuHelper = new MenuPopupHelper(context, (MenuBuilder) fileSettingsPopup.getMenu(), elementOptionView);
                            fileSettingsMenuHelper.setForceShowIcon(true);

                            fileSettingsMenuHelper.show();*/
                            fileSettingsPopup.show();
                        }
                    });
                }
                else {
                    // the current element is a directory
                    final Directory dir = (Directory) element;
                    elementNameView.setText(dir.getDirectoryName());
                    elementDescriptionView.setText(getString(R.string.empty_string));

                    //TODO: add changing color onPressed

                    elementCardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openDirectory(dir.getDirectoryName());
                        }
                    });

                    elementImage.setImageResource(R.drawable.ic_folder_24dp);

                    //TODO: add popup settings menu for the directory
                }
            }
        }
    }
}
