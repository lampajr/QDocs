package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.polimi.proj.qdocs.R;

public class ProgressBarDialog extends Dialog {

    private RoundCornerProgressBar progressBar;
    private float max = 100;
    private String title;

    public ProgressBarDialog(@NonNull Context context,
                             boolean cancelable,
                             @Nullable DialogInterface.OnCancelListener cancelListener,
                             String title) {
        super(context, cancelable, cancelListener);

        this.title = title;

        setContentView(R.layout.dialog_progress_bar);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

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
