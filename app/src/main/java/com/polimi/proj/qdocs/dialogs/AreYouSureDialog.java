package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.listeners.OnYesListener;

import java.util.Objects;

public class AreYouSureDialog extends Dialog {
    public AreYouSureDialog(@NonNull Context context,
                            final OnYesListener onYesListener) {
        super(context, false, null);

        setTitle(context.getString(R.string.are_you_sure_string));
        setContentView(R.layout.dialog_are_you_sure);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.yes_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onYesListener.onYes();
                dismiss();
            }
        });

        findViewById(R.id.no_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
