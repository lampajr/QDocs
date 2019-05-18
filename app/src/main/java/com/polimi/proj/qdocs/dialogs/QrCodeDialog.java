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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.polimi.proj.qdocs.R;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.PathResolver;
import com.polimi.proj.qdocs.support.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.polimi.proj.qdocs.dialogs.QrCodeDialog.SaveQRCodeTask.ERROR;
import static com.polimi.proj.qdocs.dialogs.QrCodeDialog.SaveQRCodeTask.FILE_ALREADY_EXIST;
import static com.polimi.proj.qdocs.dialogs.QrCodeDialog.SaveQRCodeTask.FILE_CREATED;

public class QrCodeDialog extends Dialog {
    private final static String TAG = "QR_CODE_DIALOG";

    private ImageView qrCodeImg;
    private ImageView saveImg;
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
        saveImg = findViewById(R.id.save_button);
        qrCodeImg = findViewById(R.id.qrcode_image);
        setupDialog();
    }

    /**
     * Setup QR code dialog
     */
    private void setupDialog() {
        qrCodeBitmap = Utility.generateQrCode(file.getKey());
        if (qrCodeBitmap == null) {
            Log.e(TAG, "Error generating qr code: bitmap is null");
            return;
        }
        qrCodeImg.setImageBitmap(qrCodeBitmap);
        saveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = file.getFilename().split("\\.")[0] + ".png";
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
    }

    /**
     * Async task that is in charge to compress the qr code bitmap into a png format and
     * save it into a specific file, passed as parameter to the constructor
     */
    protected static class SaveQRCodeTask extends AsyncTask<Bitmap, Void, Integer> {
        private final static String TAG = "SAVE_QR_CODE_TASK";
        final static int  FILE_CREATED = 0, FILE_ALREADY_EXIST = 1, ERROR = -1;

        private int quality = 100;
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
