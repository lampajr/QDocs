package com.polimi.proj.qdocs.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Toast;

import com.crowdfire.cfalertdialog.CFAlertDialog;

public class ConfirmDialog {


    public ConfirmDialog(final Context context, DialogInterface.OnClickListener confirmListener){
        // Create Alert using Builder
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(context)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setTitle("Are You Sure")
                .setTextColor(1)
                .setTextGravity(Gravity.CENTER_HORIZONTAL)
                .setMessage("\n\n")
                .addButton("CONFIRM", Color.parseColor("#B5B5BE"),
                        Color.parseColor("#575E5D"),
                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.CENTER,
                        confirmListener
                );


        // Show the alert
        builder.show();

    }

}
