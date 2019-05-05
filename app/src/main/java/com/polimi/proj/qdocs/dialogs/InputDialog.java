package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.listeners.OnInputListener;

public class InputDialog extends Dialog {
    public InputDialog(@NonNull Context context,
                       @Nullable DialogInterface.OnCancelListener cancelListener,
                       final OnInputListener onInputListener,
                       String title) {
        super(context, false, cancelListener);

        setTitle(title);
        setContentView(R.layout.dialog_insert_name);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText nameEditTex = findViewById(R.id.name_text);

        findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditTex.getText().toString();
                onInputListener.onNameInserted(name);
                dismiss();
            }
        });

        findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
