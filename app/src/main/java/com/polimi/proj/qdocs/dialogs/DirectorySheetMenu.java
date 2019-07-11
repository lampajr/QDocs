package com.polimi.proj.qdocs.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class DirectorySheetMenu extends BottomSheetDialogFragment {
    private View.OnClickListener onDelete, onInfo;

    private DirectorySheetMenu(View.OnClickListener onDelete,
                             View.OnClickListener onInfo) {
        this.onDelete = onDelete;
        this.onInfo = onInfo;
    }

    public static DirectorySheetMenu getInstance(View.OnClickListener onDelete,
                                               View.OnClickListener onInfo) {

        return new DirectorySheetMenu(onDelete, onInfo);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.directory_bottom_sheet, container, false);

        TextView deleteOption = view.findViewById(R.id.delete_option),
                infoOption = view.findViewById(R.id.info_option);

        deleteOption.setOnClickListener(onDelete);
        infoOption.setOnClickListener(onInfo);

        return view;
    }
}
