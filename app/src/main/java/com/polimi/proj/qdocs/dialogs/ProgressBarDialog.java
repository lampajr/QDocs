package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.polimi.proj.qdocs.R;

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

public class ProgressBarDialog extends Dialog {

    private RoundCornerProgressBar progressBar;
    private Button cancelButton;
    private float max = 100;
    private String title;

    public ProgressBarDialog(@NonNull Context context,
                             @Nullable View.OnClickListener cancelListener,
                             String title) {
        super(context, false, null);

        this.title = title;

        setContentView(R.layout.dialog_progress_bar);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textView = findViewById(R.id.title_text);
        textView.setText(title);

        cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(cancelListener);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(max);
    }

    public void makeCancelVisible() {
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setFocusable(true);
        cancelButton.setClickable(true);
    }

    public void setProgress(float value) {
        progressBar.setProgress(value - 0.1F);
        if (value == max) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 500);
        }
    }
}
