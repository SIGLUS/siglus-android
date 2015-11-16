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

package org.openlmis.core.view.holder;

import android.view.View;
import android.widget.TextView;

import org.openlmis.core.R;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

import roboguice.inject.InjectView;

public class StockMovementHistoryViewHolder extends BaseViewHolder {

    @InjectView(R.id.tx_date)TextView txMovementDate;
    @InjectView(R.id.tx_reason)TextView txReason;
    @InjectView(R.id.et_document_no)TextView etDocumentNo;
    @InjectView(R.id.et_received)TextView etReceived;
    @InjectView(R.id.et_negative_adjustment)TextView etNegativeAdjustment;
    @InjectView(R.id.et_positive_adjustment)TextView etPositiveAdjustment;
    @InjectView(R.id.et_issued)TextView etIssued;
    @InjectView(R.id.tx_stock_on_hand)TextView txStockExistence;
    private final int blackColor;
    private final int redColor;

    public StockMovementHistoryViewHolder(View itemView) {
        super(itemView);

        disableLine();
        hideUnderline();
        blackColor = context.getResources().getColor(R.color.black);
        redColor = context.getResources().getColor(R.color.red);
    }

    public void populate(final StockMovementViewModel model) {
        setRowFontColor(blackColor);

        txMovementDate.setText(model.getMovementDate());
        etDocumentNo.setText(model.getDocumentNo());
        etReceived.setText(model.getReceived());
        etNegativeAdjustment.setText(model.getNegativeAdjustment());
        etPositiveAdjustment.setText(model.getPositiveAdjustment());
        etIssued.setText(model.getIssued());
        txStockExistence.setText(model.getStockExistence());
        txReason.setText(model.getReason().getDescription());

        setInventoryItemsFontColorToRed(model);
    }

    private void setInventoryItemsFontColorToRed(StockMovementViewModel model) {
        if (model.getReceived() != null
                || model.getReason().getMovementType() == StockMovementItem.MovementType.PHYSICAL_INVENTORY
                || model.getReason().isInventoryAdjustment()) {
            setRowFontColor(redColor);
        }
    }

    private void setRowFontColor(int color) {
        txMovementDate.setTextColor(color);
        txReason.setTextColor(color);
        etDocumentNo.setTextColor(color);
        etReceived.setTextColor(color);
        etPositiveAdjustment.setTextColor(color);
        etNegativeAdjustment.setTextColor(color);
        txStockExistence.setTextColor(color);
    }

    private void hideUnderline() {
        etDocumentNo.setBackground(null);
        etIssued.setBackground(null);
        etNegativeAdjustment.setBackground(null);
        etPositiveAdjustment.setBackground(null);
        etReceived.setBackground(null);
    }

    private void disableLine() {
        etDocumentNo.setEnabled(false);
        etReceived.setEnabled(false);
        etNegativeAdjustment.setEnabled(false);
        etPositiveAdjustment.setEnabled(false);
        etIssued.setEnabled(false);
    }

}
