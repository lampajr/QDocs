package com.polimi.proj.qdocs.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;

import com.polimi.proj.qdocs.R;

import java.util.Objects;

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

public class AboutDialog extends Dialog {
    public AboutDialog(@NonNull Context context, @Nullable OnCancelListener cancelListener) {
        super(context, true, cancelListener);

        setContentView(R.layout.dialog_about);

        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Spanned ref = HtmlCompat.fromHtml(context.getString(R.string.licensed_under) + "\n<a href='https://www.apache.org/licenses/LICENSE-2.0.txt'>Apache License 2.0</a>", HtmlCompat.FROM_HTML_MODE_LEGACY);
        TextView licenseView = findViewById(R.id.license_view);
        licenseView.setText(ref);
        licenseView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
