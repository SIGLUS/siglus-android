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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.utils.LogUtil;

import java.util.List;

public class RnrFromListAdapter extends BaseAdapter {

    private final Context context;
    private final List<RnrFormItem> list;

    public RnrFromListAdapter(Context context, List<RnrFormItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RnrFormItem getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        View inflate = View.inflate(context, R.layout.item_rnr_from, null);

        TextView tvIssuedUnit = (TextView) inflate.findViewById(R.id.tv_issued_unit);
        TextView tvInitialAmount = (TextView) inflate.findViewById(R.id.tv_initial_amount);
        TextView tvReceived = (TextView) inflate.findViewById(R.id.tv_received);
        TextView tvIssued = (TextView) inflate.findViewById(R.id.tv_issued);
        TextView tvAdjustment = (TextView) inflate.findViewById(R.id.tv_adjustment);
        TextView tvInventory = (TextView) inflate.findViewById(R.id.tv_inventory);
        TextView tvValidate = (TextView) inflate.findViewById(R.id.tv_validate);

        if (i == 0) {
            tvIssuedUnit.setText(R.string.issued_unit);
            tvInitialAmount.setText(R.string.initial_amount);
            tvReceived.setText(R.string.received);
            tvIssued.setText(R.string.issued);
            tvAdjustment.setText(R.string.adjustment);
            tvInventory.setText(R.string.inventory);
            tvValidate.setText(R.string.validate);
            inflate.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            RnrFormItem item = getItem(i - 1);
            //TODO refactor api field tvIssuedUnit
            tvIssuedUnit.setText(String.valueOf(item.getProduct().getStrength()));
            tvInitialAmount.setText(String.valueOf(item.getInitialAmount()));
            tvReceived.setText(String.valueOf(item.getReceived()));
            tvIssued.setText(String.valueOf(item.getIssued()));
            tvAdjustment.setText(String.valueOf(item.getAdjustment()));
            tvInventory.setText(String.valueOf(item.getInventory()));
            tvValidate.setText(String.valueOf(item.getValidate()));
        }

        inflate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.s("postion---" + i);
            }
        });
        return inflate;
    }

}
