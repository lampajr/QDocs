package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.support.MyDirectory;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageElement;

import java.text.DateFormat;
import java.util.Date;
import java.util.Objects;

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
 */

public class InfoDialog extends Dialog {
    private static final String TAG = "INFO_DIALOG";

    private StorageElement storageElement;

    public InfoDialog(@NonNull Context context,
                         @Nullable DialogInterface.OnCancelListener cancelListener,
                         final StorageElement element) {
        super(context, true, cancelListener);

        this.storageElement = element;

        setTitle(context.getString(R.string.info_string).toUpperCase());
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (storageElement instanceof MyFile)
            setupFileInfoDialog();
        else setupDirectoryInfoDialog();
    }


    /**
     * Setup Info Dialog for a file
     */
    private void setupFileInfoDialog() {
        Log.d(TAG, "Setting up file's info dialog");

        MyFile file = (MyFile) storageElement;
        setContentView(R.layout.dialog_file_info);
        TextView filenameText = findViewById(R.id.filename_text),
                contentTypeText = findViewById(R.id.content_type_text),
                createdAtText = findViewById(R.id.created_at_text),
                lastAccessText = findViewById(R.id.last_access_text),
                sizeText = findViewById(R.id.size_text);

        filenameText.setText(file.getFilename().split("\\.")[0]);
        contentTypeText.setText(file.getContentType());
        createdAtText.setText(file.getTime());

        DateFormat simple = DateFormat.getDateTimeInstance();
        Date lastAccessDate = new Date(file.getLastAccess());
        lastAccessText.setText(simple.format(lastAccessDate));

        long sizeKb = Long.parseLong(file.getSize()) / 1000;
        String size;
        if (sizeKb > 1000) {
            long sizeMb = sizeKb / 1000;
            size = sizeMb + " Mb";
        }
        else {
            size = sizeKb + " Kb";
        }
        sizeText.setText(size);
    }

    /**
     * Setup Info Dialog for a directory
     */
    private void setupDirectoryInfoDialog() {
        MyDirectory dir = (MyDirectory) storageElement;
        setContentView(R.layout.dialog_dir_info);

        TextView dirName = findViewById(R.id.directory_name_text);
        dirName.setText(dir.getDirectoryName());
    }
}
