package com.polimi.proj.qdocs.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.services.DownloadFileService;

public class ShowFileFragmentActivity extends FragmentActivity {

    private Uri fileUri;
    private String mimeType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file);

        Bundle bundle = getIntent().getExtras();
        fileUri = (Uri) bundle.get(DownloadFileService.RESULT_KEY_URI);
        mimeType = bundle.getString(DownloadFileService.RESULT_KEY_EXTENSION);
    }
}
