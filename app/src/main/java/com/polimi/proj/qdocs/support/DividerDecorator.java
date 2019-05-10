package com.polimi.proj.qdocs.support;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerDecorator extends RecyclerView.ItemDecoration {
    private Drawable divider;

    public DividerDecorator(Drawable divider) {
        this.divider = divider;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas,
                       @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        int dividerLeft = 32;
        int dividerRight = parent.getWidth() - 32;

        for (int i = 0; i < parent.getChildCount() - 1; i++) {
            View child = parent.getChildAt(i);
            StorageAdapter.DataViewHolder vHolder = (StorageAdapter.DataViewHolder) parent.getChildViewHolder(child);

            if (vHolder.isFile) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int dividerTop = child.getBottom() + params.bottomMargin;
                int dividerBottom = dividerTop + divider.getIntrinsicHeight();

                divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                divider.draw(canvas);
            }
        }
    }
}
