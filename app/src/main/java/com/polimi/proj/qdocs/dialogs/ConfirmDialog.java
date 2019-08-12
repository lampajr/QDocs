package com.polimi.proj.qdocs.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.polimi.proj.qdocs.R;

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

public class ConfirmDialog {


    public ConfirmDialog(final Context context, DialogInterface.OnClickListener confirmListener){
        // Create Alert using Builder
        CFAlertDialog.Builder builder = new CFAlertDialog.Builder(context)
                .setDialogStyle(CFAlertDialog.CFAlertStyle.ALERT)
                .setTitle("Are You Sure")
                .setTextColor(1)
                .setTextGravity(Gravity.CENTER_HORIZONTAL)
                .setMessage("\n\n")
                .addButton(context.getString(R.string.confirm), Color.parseColor("#B5B5BE"),
                        Color.parseColor("#575E5D"),
                        CFAlertDialog.CFAlertActionStyle.POSITIVE,
                        CFAlertDialog.CFAlertActionAlignment.CENTER,
                        confirmListener
                );


        // Show the alert
        builder.show();

    }

}
