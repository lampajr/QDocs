package com.polimi.proj.qdocs.support;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.listeners.OnSwipeTouchListener;

import java.util.List;

public abstract class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.dataViewHolder>{

    private static final String TAG = "STORAGE_ADAPTER";
    private static final int EXTRA = 50;

    private final int FILE = 0;
    private final int DIRECTORY = 1;

    private LayoutInflater inflater;
    private List<StorageElement> elements;
    private Context context;
    private OnSwipeTouchListener onItemSwipeTouchListener;
    private StorageReference storageRef;

    protected StorageAdapter(Context context, List<StorageElement> elements,
                             OnSwipeTouchListener onItemSwipeTouchListener,
                             StorageReference storageRef) {
        this.inflater = LayoutInflater.from(context);
        this.elements = elements;
        this.context = context;
        this.onItemSwipeTouchListener = onItemSwipeTouchListener;
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
    public StorageAdapter.dataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //TODO: change layout according to the viewType
        View view;
        if (viewType == FILE) {
            view = inflater.inflate(R.layout.single_file_layout, parent, false);
        }
        else {
            view = inflater.inflate(R.layout.single_directory_layout, parent, false);
        }
        return new StorageAdapter.dataViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (elements.get(position) instanceof MyFile)
            return FILE;
        else return DIRECTORY;
    }

    @Override
    public void onBindViewHolder(@NonNull final StorageAdapter.dataViewHolder holder, int position) {
        holder.bindData(elements.get(position), storageRef, context, onItemSwipeTouchListener);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    // methods to implement while instantiating this class

    public abstract void onFileClick(final MyFile file);

    public abstract void onFileOptionClick(final MyFile file);

    public abstract void onDirectoryClick(final Directory dir);

    public abstract void onDirectoryOptionClick(final Directory dir);

    class dataViewHolder extends RecyclerView.ViewHolder {
        // Item-row elements
        TextView elementNameView, elementDescriptionView, elementOptionView;
        ImageView elementImage;
        View mainLayout;

        dataViewHolder(@NonNull View itemView) {
            super(itemView);
            View contentView = itemView.findViewById(R.id.content);
            elementNameView = contentView.findViewById(R.id.element_name);
            elementDescriptionView = contentView.findViewById(R.id.element_description);
            elementOptionView = contentView.findViewById(R.id.element_options);
            elementImage = contentView.findViewById(R.id.element_image);
            mainLayout = itemView.findViewById(R.id.layout);
        }

        @SuppressLint("ClickableViewAccessibility")
        void bindData(final StorageElement element,
                      final StorageReference ref,
                      final Context context,
                      final OnSwipeTouchListener onItemSwipeListener) {
            elementImage.setImageDrawable(null);
            //TODO: set onClick animation on the items
            if (element instanceof MyFile) {
                final MyFile file = (MyFile) element;
                elementNameView.setText(file.getFilename());
                elementDescriptionView.setText(file.getContentType());

                final StorageReference refUsed = file.getStReference() == null ? ref : file.getStReference();

                if (file.getContentType().contains("image")) {
                    // preview image for image file
                    refUsed.child(file.getFilename()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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

                mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onFileClick(file);
                    }
                });

                mainLayout.setOnTouchListener(onItemSwipeListener);

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
                final Directory dir = (Directory) element;
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
