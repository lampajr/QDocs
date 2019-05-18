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

public class OfflineSheetMenu extends BottomSheetDialogFragment {
    private View.OnClickListener onSave, onDelete, onGetQrCode, onInfo;

    private OfflineSheetMenu(View.OnClickListener onDelete,
                             View.OnClickListener onInfo) {
        this.onDelete = onDelete;
        this.onInfo = onInfo;
    }

    public static OfflineSheetMenu getInstance(View.OnClickListener onDelete,
                                               View.OnClickListener onInfo) {

        return new OfflineSheetMenu(onDelete, onInfo);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.offline_bottom_sheet, container, false);

        TextView deleteOption = view.findViewById(R.id.delete_option),
                infoOption = view.findViewById(R.id.info_option);

        deleteOption.setOnClickListener(onDelete);
        infoOption.setOnClickListener(onInfo);

        return view;
    }
}
