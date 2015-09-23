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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import java.util.List;


public class StockMovementHistoryAdapter extends BaseAdapter {

    List<StockMovementViewModel> stockMovementViewModels;

    LayoutInflater layoutInflater;

    Context context;

    public StockMovementHistoryAdapter(Context context, List<StockMovementViewModel> list) {
        stockMovementViewModels = list;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return stockMovementViewModels.size();
    }

    @Override
    public StockMovementViewModel getItem(int position) {
        return stockMovementViewModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_stock_movement, parent, false);
            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        onBindViewHolder(holder, position);
        return convertView;
    }

    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final StockMovementViewModel model = getItem(position);

        disableLine(holder);

        holder.txMovementDate.setText(model.getMovementDate());
        holder.txReason.setText(model.getReason());
        holder.etDocumentNo.setText(model.getDocumentNo());
        holder.etReceived.setText(model.getReceived());
        holder.etNegativeAdjustment.setText(model.getNegativeAdjustment());
        holder.etPositiveAdjustment.setText(model.getPositiveAdjustment());
        holder.etIssued.setText(model.getIssued());
        holder.txStockExistence.setText(model.getStockExistence());

        setFontToRedWhenReasonIsReceived(holder, model);
    }

    private void setFontToRedWhenReasonIsReceived(ViewHolder holder, StockMovementViewModel model) {
        if (model.getReceived() != null || model.getMovementType() == StockMovementItem.MovementType.PHYSICAL_INVENTORY){
            holder.txMovementDate.setTextColor(Color.RED);
            holder.txReason.setTextColor(Color.RED);
            holder.etDocumentNo.setTextColor(Color.RED);
            holder.etReceived.setTextColor(Color.RED);
            holder.txStockExistence.setTextColor(Color.RED);
        }
    }

    private void disableLine(ViewHolder holder) {
        holder.etDocumentNo.setEnabled(false);
        holder.etReceived.setEnabled(false);
        holder.etNegativeAdjustment.setEnabled(false);
        holder.etPositiveAdjustment.setEnabled(false);
        holder.etIssued.setEnabled(false);
    }

    class ViewHolder {

        TextView txMovementDate;
        TextView txReason;
        TextView etDocumentNo;
        TextView etReceived;
        TextView etNegativeAdjustment;
        TextView etPositiveAdjustment;
        TextView etIssued;
        TextView txStockExistence;


        public ViewHolder(View view) {
            txMovementDate = (TextView) view.findViewById(R.id.tx_date);
            txReason = (TextView) view.findViewById(R.id.tx_reason);
            etDocumentNo = (TextView) view.findViewById(R.id.et_document_no);
            etReceived = (TextView) view.findViewById(R.id.et_received);
            etNegativeAdjustment = (TextView) view.findViewById(R.id.et_negative_adjustment);
            etPositiveAdjustment = (TextView) view.findViewById(R.id.et_positive_adjustment);
            etIssued = (TextView) view.findViewById(R.id.et_issued);
            txStockExistence = (TextView) view.findViewById(R.id.tx_stock_on_hand);
        }
    }

}
