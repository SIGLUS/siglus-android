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

package org.openlmis.core.view.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Regimen;
import org.openlmis.core.model.RegimenItem;

import java.util.List;

public class RegimeListAdapter extends BaseAdapter {

    private final Context context;
    private final List<RegimenItem> list;

    public RegimeListAdapter(Context context, List<RegimenItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RegimenItem getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {


        View inflate = View.inflate(context, R.layout.item_regime, null);
        TextView tvCode = (TextView) inflate.findViewById(R.id.tv_code);
        TextView tvName = (TextView) inflate.findViewById(R.id.tv_name);
        EditText etTotal = (EditText) inflate.findViewById(R.id.et_total);

        if (i == 0) {
            tvCode.setText(R.string.list_regime_header_code);
            tvName.setText(R.string.list_regime_header_name);
            tvName.setGravity(Gravity.CENTER);
            etTotal.setText(R.string.TOTAL);
            etTotal.setEnabled(false);
            inflate.setBackgroundResource(R.color.color_mmia_speed_list_header);
        } else {
            RegimenItem item = getItem(i - 1);
            Regimen regimen = item.getRegimen();
            tvCode.setText(regimen.getCode());
            tvName.setText(regimen.getName());
            if (Regimen.RegimeType.BABY.equals(regimen.getType())) {
                inflate.setBackgroundResource(R.color.color_regime_baby);
            } else {
                inflate.setBackgroundResource(R.color.color_regime_adult);
            }
        }

        return inflate;
    }

}
