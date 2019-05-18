package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.polimi.proj.qdocs.R;

import java.util.Objects;

public class ProgressBarDialog extends Dialog {

    private RoundCornerProgressBar progressBar;
    private float max = 100;
    private String title;

    public ProgressBarDialog(@NonNull Context context,
                             @Nullable DialogInterface.OnCancelListener cancelListener,
                             String title) {
        super(context, false, cancelListener);

        this.title = title;

        setContentView(R.layout.dialog_progress_bar);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textView = findViewById(R.id.title_text);
        textView.setText(title);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setMax(max);
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
