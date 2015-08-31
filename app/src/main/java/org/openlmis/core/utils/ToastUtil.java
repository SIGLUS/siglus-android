/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */
package org.openlmis.core.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.openlmis.core.LMISApp;
import org.openlmis.core.R;

public final class ToastUtil {

    private ToastUtil() {

    }

    public static void show(CharSequence text) {
        if (text == null)
            return;
        Toast.makeText(LMISApp.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    public static void show(int resId) {
        Toast.makeText(LMISApp.getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    public static void showInCenter(CharSequence text) {
        if (text == null)
            return;
        Toast toast = new Toast(LMISApp.getContext());
        LayoutInflater inflate = (LayoutInflater)
                LMISApp.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.view_toast, null);
        TextView tv = (TextView) v.findViewById(R.id.message);
        tv.setText(text);
        toast.setView(v);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showInCenter(int text) {
        Toast toast = Toast
                .makeText(LMISApp.getContext(), text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void showLongTimeInCenter(CharSequence text) {
        if (text == null)
            return;
        Toast toast = Toast.makeText(LMISApp.getContext(), text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
