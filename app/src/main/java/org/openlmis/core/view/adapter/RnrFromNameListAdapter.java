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
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;

import java.util.List;

public class RnrFromNameListAdapter extends BaseAdapter {

    private final Context context;
    private final List<RnrFormItem> list;

    public RnrFromNameListAdapter(Context context, List<RnrFormItem> list) {
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate = View.inflate(context, R.layout.item_rnr_from_product_name, null);
        TextView tvPrimaryName = (TextView) inflate.findViewById(R.id.tv_primary_name);

        if (i == 0) {
            tvPrimaryName.setText(R.string.list_rnrfrom_left_header);
            tvPrimaryName.setGravity(Gravity.CENTER);
            inflate.setBackgroundResource(R.color.color_mmia_info_name);
        } else {
            RnrFormItem item = getItem(i - 1);
            Product product = item.getProduct();
            tvPrimaryName.setText(product.getPrimaryName());
        }

        return inflate;
    }

}
