package com.polimi.proj.qdocs.support;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DividerDecorator extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private Drawable lastDivider;

    public DividerDecorator(Drawable divider) {
        this.divider = divider;
    }

    public DividerDecorator(Drawable divider, Drawable lastDivider) {
        this.divider = divider;
        this.lastDivider = lastDivider;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas,
                       @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        int dividerLeft = 5;
        int dividerRight = parent.getWidth() - 5;

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            StorageAdapter.DataViewHolder vHolder = (StorageAdapter.DataViewHolder) parent.getChildViewHolder(child);

            if (vHolder.isFile) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int dividerTop = child.getBottom() + params.bottomMargin;

                if (i == (parent.getChildCount()-1) && lastDivider != null) {
                    int dividerBottom = dividerTop + lastDivider.getIntrinsicHeight();

                    lastDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                    lastDivider.draw(canvas);
                }
                else {
                    int dividerBottom = dividerTop + divider.getIntrinsicHeight();

                    divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
                    divider.draw(canvas);
                }
            }
        }

        // last item
        /*
        int idx = parent.getChildCount() - 1;
        View child = parent.getChildAt(idx);

        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
        int dividerTop = child.getBottom() + params.bottomMargin;
        int dividerBottom = dividerTop + divider.getIntrinsicHeight();

        divider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
        divider.draw(canvas);*/
    }
}
