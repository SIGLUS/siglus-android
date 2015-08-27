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

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.utils.SimpleTextWatcher;
import org.openlmis.core.view.viewmodel.RequisitionFormItemViewModel;

import java.util.List;

public class RequisitionFormAdapter extends BaseAdapter {

    Context context;
    List<RequisitionFormItemViewModel> data;
    LayoutInflater inflater;
    boolean isNameList;
    private boolean hasDataChanged = false;

    int itemLayoutResId;

    public RequisitionFormAdapter(Context context, List<RequisitionFormItemViewModel> data, boolean isNameList) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(context);
        this.isNameList = isNameList;
        if (isNameList) {
            itemLayoutResId = R.layout.item_requisition_body_left;
        } else {
            itemLayoutResId = R.layout.item_requisition_body;
        }
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RequisitionFormItemViewModel getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final RequisitionFormItemViewModel entry = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(itemLayoutResId, parent, false);
            viewHolder = new ViewHolder(convertView, isNameList);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        View currentFocus = ((Activity) context).getCurrentFocus();
        if (currentFocus != null) {
            currentFocus.clearFocus();
        }
        onBindViewHolder(viewHolder, entry);

        return convertView;
    }

    public void onBindViewHolder(final ViewHolder holder, final RequisitionFormItemViewModel entry) {

        holder.productCode.setText(entry.getFmn());
        holder.productName.setText(entry.getProductName());

        if (!isNameList) {
            holder.initAmount.setText(entry.getInitAmount());
            holder.received.setText(entry.getReceived());
            holder.issued.setText(entry.getIssued());
            holder.theoretical.setText(entry.getTheoretical());
            holder.total.setText(entry.getTotal());
            holder.inventory.setText(entry.getInventory());
            holder.different.setText(entry.getDifferent());
            holder.totalRequest.setText(entry.getTotalRequest());

            MySimpleTextWatcher mySimpleTextWatcher = new MySimpleTextWatcher(holder.approvedAmount, entry);
            holder.requestAmount.removeTextChangedListener(mySimpleTextWatcher);
            holder.requestAmount.setText(entry.getRequestAmount());
            holder.requestAmount.setError(null);
            holder.requestAmount.addTextChangedListener(mySimpleTextWatcher);

            holder.approvedAmount.setText(entry.getApprovedAmount());

        }
    }

    public boolean hasDataChanged() {
        return hasDataChanged;
    }

    class MySimpleTextWatcher extends SimpleTextWatcher {

        private final EditText approvedAmount;
        private final RequisitionFormItemViewModel entry;

        public MySimpleTextWatcher(EditText approvedAmount, RequisitionFormItemViewModel entry) {
            this.approvedAmount = approvedAmount;
            this.entry = entry;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return true;
        }

        @Override
        public void afterTextChanged(Editable editable) {
            hasDataChanged = true;
            String value = editable.toString();
            approvedAmount.setText(value);
            entry.setRequestAmount(value);
            entry.setApprovedAmount(value);
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
        public EditText requestAmount;
        public EditText approvedAmount;

        public ViewHolder(View itemView, boolean isNameList) {
            productCode = ((TextView) itemView.findViewById(R.id.tx_FNM));
            productName = ((TextView) itemView.findViewById(R.id.tx_product_name));


            if (!isNameList) {
                received = ((TextView) itemView.findViewById(R.id.tx_received));
                initAmount = ((TextView) itemView.findViewById(R.id.tx_initial_amount));
                issued = ((TextView) itemView.findViewById(R.id.tx_issued));
                theoretical = ((TextView) itemView.findViewById(R.id.tx_theoretical));
                total = ((TextView) itemView.findViewById(R.id.tx_total));
                inventory = ((TextView) itemView.findViewById(R.id.tx_inventory));
                different = ((TextView) itemView.findViewById(R.id.tx_different));
                totalRequest = ((TextView) itemView.findViewById(R.id.tx_total_request));
                requestAmount = ((EditText) itemView.findViewById(R.id.et_request_amount));
                approvedAmount = ((EditText) itemView.findViewById(R.id.et_approved_amount));
            }
        }
    }

}

