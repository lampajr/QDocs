package com.polimi.proj.qdocs.support;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

public abstract class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.DataViewHolder>{

    private static final String TAG = "STORAGE_ADAPTER";

    private final int FILE = 0;
    private final int DIRECTORY = 1;

    private LayoutInflater inflater;
    private List<StorageElement> elements;
    private Context context;
    private StorageReference storageRef;

    protected StorageAdapter(Context context, List<StorageElement> elements,
                             StorageReference storageRef) {
        this.inflater = LayoutInflater.from(context);
        this.elements = elements;
        this.context = context;
        this.storageRef = storageRef;

        setHasStableIds(true);
    }

    /**
     * updates the storage reference
     * @param ref new reference
     */
    public void updateStorageReference(StorageReference ref) {
        storageRef = ref;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //TODO: change layout according to the viewType
        View view;
        if (viewType == FILE) {
            view = inflater.inflate(R.layout.single_file_layout, parent, false);
        }
        else {
            view = inflater.inflate(R.layout.single_directory_layout, parent, false);
        }
        return new DataViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (elements.get(position) instanceof MyFile)
            return FILE;
        else return DIRECTORY;
    }

    @Override
    public void onBindViewHolder(@NonNull final DataViewHolder holder, int position) {
        holder.bindData(elements.get(position), storageRef, context);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    // methods to implement while instantiating this class

    public abstract void onFileClick(final MyFile file);

    public abstract void onFileOptionClick(final MyFile file);

    public abstract void onDirectoryClick(final MyDirectory dir);

    public abstract void onDirectoryOptionClick(final MyDirectory dir);

    class DataViewHolder extends RecyclerView.ViewHolder {
        // Item-row elements
        boolean isFile;
        TextView elementNameView, elementDescriptionView, elementOptionView;
        ImageView elementImage;
        View mainLayout;

        DataViewHolder(@NonNull View itemView) {
            super(itemView);
            View contentView = itemView.findViewById(R.id.content);
            elementNameView = contentView.findViewById(R.id.element_name);
            elementDescriptionView = contentView.findViewById(R.id.element_description);
            elementOptionView = contentView.findViewById(R.id.element_options);
            elementImage = contentView.findViewById(R.id.element_image);
            mainLayout = itemView.findViewById(R.id.layout);
        }

        void bindData(final StorageElement element,
                      final StorageReference ref,
                      final Context context) {
            elementImage.setImageDrawable(null);

            if (element instanceof MyFile) {
                isFile = true;
                final MyFile file = (MyFile) element;
                elementNameView.setText(file.getFilename().split("\\.")[0]);
                elementDescriptionView.setText(file.getContentType());
                Log.d(TAG, "Adding file to the recycler view");

                final StorageReference refUsed = file.getStReference() == null ? ref : file.getStReference();

                if (file.getContentType() == null) {
                    elementImage.setImageResource(R.drawable.ic_unsupported_file_24dp);
                }
                else if (file.getContentType().contains("image")) {
                    // preview image for image file

                    File[] files = null;

                    if (file.isOffline()) {
                        // if offline file load the preview image from local directory
                        File baseDir = PathResolver.getPublicDocFileDir(context);
                        files = baseDir.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.equals(file.getFilename());
                            }
                        });

                        if (files != null && files.length != 0) {
                            File offlineFile = files[0];
                            Bitmap myBitmap = BitmapFactory.decodeFile(offlineFile.getAbsolutePath());
                            elementImage.setImageBitmap(myBitmap);
                        }
                    }
                    
                    if (files == null || files.length == 0){
                        // if not offline load the image from Firebase
                        refUsed.child(file.getFilename()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(TAG, "preview image loaded successfully");
                                Glide.with(context).load(uri).into(elementImage);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // TODO: handle the situation where you are not able to download the preview image, setting default image
                            }
                        });
                    }

                }
                else if (file.getContentType().contains("audio")) {
                    // image for audio
                    elementImage.setImageResource(R.drawable.ic_mic_24dp);
                }
                else if (file.getContentType().contains("pdf")) {
                    elementImage.setImageResource(R.drawable.ic_tmp_pdf_24dp);
                }
                else {
                    elementImage.setImageResource(R.drawable.ic_unsupported_file_24dp);
                }

                mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFileClick(file);
                    }
                });

                elementOptionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // will show the bottom menu here
                        onFileOptionClick(file);
                    }
                });
            }
            else {
                // the current element is a directory
                isFile = false;
                final MyDirectory dir = (MyDirectory) element;
                elementNameView.setText(dir.getDirectoryName());
                elementDescriptionView.setText(context.getString(R.string.empty_string));

                mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDirectoryClick(dir);
                    }
                });

                elementImage.setImageResource(R.drawable.ic_folder_24dp);

                elementOptionView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // will shoe the bottom menu here
                        onDirectoryOptionClick(dir);
                    }
                });
            }
        }
    }
}
