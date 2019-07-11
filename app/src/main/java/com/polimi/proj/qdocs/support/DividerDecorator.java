package com.polimi.proj.qdocs.support;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class DividerDecorator extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private int bottomSpace;

    public DividerDecorator(Drawable divider, int bottomSpace) {
        this.divider = divider;
        this.bottomSpace = bottomSpace;
    }

    public DividerDecorator(Drawable divider) {
        this(divider, 2);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas,
                       @NonNull RecyclerView parent,
                       @NonNull RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        int dividerLeft = 5;
        int dividerRight = parent.getWidth() - 5;

        for (int i = 0; i < parent.getChildCount() - bottomSpace; i++) {
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
