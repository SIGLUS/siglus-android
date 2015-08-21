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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;

import java.util.ArrayList;
import java.util.List;

public class RequisitionFormAdapter extends BaseAdapter {

    Context context;
    List<RnrFormItem> data;
    LayoutInflater inflater;
    boolean isNameList;

    int itemLayoutResId;

    public RequisitionFormAdapter(Context context, RnRForm rnRForm, boolean isNameList) {
        this.context = context;
        data = new ArrayList<>();
        for (RnrFormItem item : rnRForm.getRnrFormItemList()) {
            data.add(item);
        }
        inflater = LayoutInflater.from(context);
        this.isNameList = isNameList;
        if (isNameList){
            itemLayoutResId = R.layout.item_requisition_body_left;
        }else{
            itemLayoutResId = R.layout.item_requisition_body;
        }

    }

    @Override
    public int getCount() {
        return 101;
    }

    @Override
    public RnrFormItem getItem(int position) {
        return data.get(0);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(itemLayoutResId, parent, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        onBindViewHolder(viewHolder, position);

        return convertView;
    }

    public void onBindViewHolder(ViewHolder holder, int position) {
        RnrFormItem product = data.get(0);
        holder.productCode.setText(product.getProduct().getCode());
        holder.productName.setText(product.getProduct().getPrimaryName());

        if (!isNameList){
            long received = product.getReceived();
            long total = product.getInitialAmount() + received - product.getIssued();
            long inventory = product.getInventory();

            holder.initAmount.setText(String.valueOf(product.getInitialAmount()));
            holder.received.setText(String.valueOf(received));
            holder.issued.setText(String.valueOf(product.getIssued()));
            holder.theoretical.setText(String.valueOf(total));
            holder.total.setText("-");
            holder.inventory.setText(String.valueOf(inventory));
            holder.different.setText(String.valueOf(product.getAdjustment() - total));
            holder.totalRequest.setText(String.valueOf(received * 2 - inventory));
        }
    }

    static class ViewHolder {

        public TextView productCode;
        public TextView productName;
        public TextView initAmount;
        public TextView received;
        public TextView issued;
        public TextView theoretical;
        public TextView total;
        public TextView inventory;
        public TextView different;
        public TextView totalRequest;

        public ViewHolder(View itemView) {
            productCode = ((TextView) itemView.findViewById(R.id.tx_FNM));
            productName = ((TextView) itemView.findViewById(R.id.tx_product_name));
            received = ((TextView) itemView.findViewById(R.id.tx_received));
            initAmount = ((TextView) itemView.findViewById(R.id.tx_initial_amount));
            issued = ((TextView) itemView.findViewById(R.id.tx_issued));
            theoretical = ((TextView) itemView.findViewById(R.id.tx_theoretical));
            total = ((TextView) itemView.findViewById(R.id.tx_total));
            inventory = ((TextView) itemView.findViewById(R.id.tx_inventory));
            different = ((TextView) itemView.findViewById(R.id.tx_different));
            totalRequest = ((TextView) itemView.findViewById(R.id.tx_total_request));
        }
    }
}

