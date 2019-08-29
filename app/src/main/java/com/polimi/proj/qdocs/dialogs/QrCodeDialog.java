package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.print.PrintHelper;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.model.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.polimi.proj.qdocs.dialogs.QrCodeDialog.SaveQRCodeTask.ERROR;
import static com.polimi.proj.qdocs.dialogs.QrCodeDialog.SaveQRCodeTask.FILE_ALREADY_EXIST;
import static com.polimi.proj.qdocs.dialogs.QrCodeDialog.SaveQRCodeTask.FILE_CREATED;

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

public class QrCodeDialog extends Dialog {
    private final static String TAG = "QR_CODE_DIALOG";

    private ImageView qrCodeImg;
    private Button saveBtn, printBtn;
    private EditText nameText;
    private MyFile file;
    private Bitmap qrCodeBitmap;
    private Context context;

    public QrCodeDialog(@NonNull Context context,
                        @Nullable DialogInterface.OnCancelListener cancelListener,
                        final MyFile file) {
        super(context, true, cancelListener);
        setTitle(context.getString(R.string.qr_code_dialog_title));
        setContentView(R.layout.dialog_show_qrcode);

        this.context = context;
        this.file = file;
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        saveBtn = findViewById(R.id.save_btn);
        printBtn = findViewById(R.id.print_btn);
        qrCodeImg = findViewById(R.id.qrcode_image);
        nameText = findViewById(R.id.name_text);
        createQrBitmap();
        setupDialog();
    }

    /**
     * Generate the Bitmap object from the code
     */
    private void createQrBitmap() {
        qrCodeBitmap = Utility.generateQrCode(file.getKey());
        if (qrCodeBitmap == null) {
            Log.e(TAG, "Error generating qr code: bitmap is null");
            throw new RuntimeException("Error generating qr code: bitmap is null");
        }
    }

    /**
     * Setup QR code dialog
     */
    private void setupDialog() {
        String name = file.getFilename().split("\\.")[0];
        nameText.setText(name);
        qrCodeImg.setImageBitmap(qrCodeBitmap);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = nameText.getText().toString() + ".png";
                File dst = new File(PathResolver.getPublicDocStorageDir(context).getAbsolutePath(),
                        context.getString(R.string.qrcode_string) + "-" + filename);
                try {
                    Integer result = new SaveQRCodeTask(qrCodeBitmap, dst).execute(qrCodeBitmap).get();
                    switch (result) {
                        case FILE_CREATED:
                            Toast.makeText(context, context.getString(R.string.qr_code_saved), Toast.LENGTH_SHORT)
                                    .show();
                            dismiss();
                            break;
                        case FILE_ALREADY_EXIST:
                            Toast.makeText(context, context.getString(R.string.qrcode_already_saved), Toast.LENGTH_SHORT)
                                    .show();
                            dismiss();
                            break;
                        case ERROR:
                            Toast.makeText(context, "Error saving QR code!!", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Error saving qr code: " + e.getMessage());
                    Toast.makeText(context, "Error saving QR code!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        printBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doPhotoPrint();
            }
        });
    }

    /**
     * Open the PrintManager in order to allow user to print the qrcode
     */
    private void doPhotoPrint() {
        PrintHelper photoPrinter = new PrintHelper(context);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        photoPrinter.printBitmap(file.getFilename() + " - print", qrCodeBitmap, new PrintHelper.OnPrintFinishCallback() {
            @Override
            public void onFinish() {
                dismiss();
            }
        });
    }


    /**
     * Async task that is in charge to compress the qr code bitmap into a png format and
     * save it into a specific file, passed as parameter to the constructor
     */
    protected static class SaveQRCodeTask extends AsyncTask<Bitmap, Void, Integer> {
        private final static String TAG = "SAVE_QR_CODE_TASK";
        final static int  FILE_CREATED = 0, FILE_ALREADY_EXIST = 1, ERROR = -1;

        private File dst;
        
        SaveQRCodeTask(Bitmap qrCode, File dst) {
            this.dst = dst;
        }

        /**
         * Saves the qr code's bitmap into a file (PNG format)
         * @param qrCodes qrcode to save into png file image
         * @return 0    ->  all went well, a new file is generated
         *         1    ->  all went well, the file already exists
         *         -1   ->  something went wrong, nothing was created
         */
        @Override
        protected Integer doInBackground(Bitmap... qrCodes) {
            if (!dst.exists()) {
                try (FileOutputStream out = new FileOutputStream(dst)){
                    int quality = 100;
                    qrCodes[0].compress(Bitmap.CompressFormat.PNG, quality, out);
                    return FILE_CREATED;
                }
                catch (IOException e) {
                    Log.e(TAG, "Error saving qr code " + ERROR + ": " + e.getMessage());
                    return ERROR;
                }
            }
            else {
                return FILE_ALREADY_EXIST;
            }
        }

        @Override
        protected void onPostExecute(Integer res) {
            super.onPostExecute(res);
        }
    }
}
