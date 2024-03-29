package com.polimi.proj.qdocs.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadFileService;

import java.io.File;

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

public class GenericFileActivity extends AppCompatActivity {

    private static final int GENERIC = 100;
    private final String TAG="GENERIC_FILE_FRAGMENT";
    private final String AUTHORITY = "com.polimi.proj.qdocs.fileprovider";
    private  Uri fileUri;
    private Uri providerUri;
    private String mimeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generic_file);


        Bundle bundle = getIntent().getExtras();
        readParameter(bundle);
        checkParameter();
        createProviderUri();
        Log.d(TAG, "provider URI: "+providerUri);

        Intent objIntent = new Intent(Intent.ACTION_VIEW);
        objIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        objIntent.setDataAndType(providerUri, mimeType);

        try {
            startActivityForResult(objIntent, GENERIC);
        }
        catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "Activity not found!", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
            Log.e(TAG, "Activity not found!");
        }
    }

    private void createProviderUri() {
        // in a rare case we received file:// in currentUri, we need to:
        // 1. create new File variable from currentUri that looks like "file:///storage/emulated/0/download/50044382b.jpg"
        // 2. generate a proper content:// Uri for it
        File currentFile = new File(fileUri.getPath());
        providerUri = FileProvider.getUriForFile(GenericFileActivity.this, AUTHORITY, currentFile);

    }

    private void readParameter(Bundle bundle) {
        if (bundle != null) {
            fileUri = (Uri) bundle.get(DownloadFileService.RESULT_KEY_URI);
            mimeType = bundle.getString(DownloadFileService.RESULT_KEY_MIME_TYPE);
        }

        Log.d(TAG, "file uri: " + fileUri);
        Log.d(TAG, "mimeType: " + mimeType);
    }

    private void checkParameter() {
        if(fileUri != null && mimeType != null){
            Log.d(TAG, "file URI received: " + fileUri);
            Log.d(TAG, "mime type received: " + mimeType);
        }
        else {

            if (fileUri == null) {
                Log.e(TAG, "URI received is NULL");
            }
            if (mimeType == null) {
                Log.e(TAG, "mime type received is NULL");
            }

            //TODO: handle error

            finish();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GENERIC)
            finish();
    }
}
