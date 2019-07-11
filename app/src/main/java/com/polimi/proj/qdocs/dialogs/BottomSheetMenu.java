package com.polimi.proj.qdocs.dialogs;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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

public class BottomSheetMenu extends BottomSheetDialogFragment {

    private View.OnClickListener onSave, onDelete, onGetQrCode, onInfo;
    private Bitmap qrcode;

    private BottomSheetMenu(Bitmap qrcode,
                            View.OnClickListener onSave,
                            View.OnClickListener onDelete,
                            View.OnClickListener onGetQrCode,
                            View.OnClickListener onInfo) {
        this.qrcode = qrcode;
        this.onSave = onSave;
        this.onDelete = onDelete;
        this.onGetQrCode = onGetQrCode;
        this.onInfo = onInfo;
    }

    public static BottomSheetMenu getInstance(Bitmap qrcode,
                                            View.OnClickListener onSave,
                                              View.OnClickListener onDelete,
                                              View.OnClickListener onGetQrCode,
                                              View.OnClickListener onInfo) {

        return new BottomSheetMenu(qrcode, onSave, onDelete, onGetQrCode, onInfo);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.complete_bottom_sheet, container, false);
        AppCompatImageView image = view.findViewById(R.id.bottom_sheet_qr_image);
        image.setImageBitmap(this.qrcode);
        TextView deleteOption = view.findViewById(R.id.delete_option),
                saveOption = view.findViewById(R.id.save_option),
                getQrCodeOption = view.findViewById(R.id.get_qrcode_option),
                infoOption = view.findViewById(R.id.info_option);

        saveOption.setOnClickListener(onSave);
        deleteOption.setOnClickListener(onDelete);
        getQrCodeOption.setOnClickListener(onGetQrCode);
        infoOption.setOnClickListener(onInfo);

        return view;
    }
}
