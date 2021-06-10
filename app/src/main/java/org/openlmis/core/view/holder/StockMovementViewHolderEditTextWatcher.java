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

import android.text.Editable;
import android.view.View;
import android.widget.TextView;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.utils.SingleTextWatcher;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;

class StockMovementViewHolderEditTextWatcher extends SingleTextWatcher {

  private final StockMovementViewHolder stockMovementViewHolder;
  private final View view;
  private final long currentStockOnHand;
  private final StockMovementViewModel model;

  public StockMovementViewHolderEditTextWatcher(StockMovementViewHolder stockMovementViewHolder,
      View view, StockMovementViewModel model, long currentStockOnHand) {
    this.stockMovementViewHolder = stockMovementViewHolder;
    this.view = view;
    this.currentStockOnHand = currentStockOnHand;
    this.model = model;
  }

  @Override
  public void afterTextChanged(Editable editable) {
    setValue(view, model, currentStockOnHand);
  }

  private void setValue(View v, StockMovementViewModel model, long currentStockOnHand) {
    String text = ((TextView) v).getText().toString();

    if (v != stockMovementViewHolder.etDocumentNo && v != stockMovementViewHolder.etRequested) {
      updateStockExistence(v, model, currentStockOnHand, text);
    }

    if (v == stockMovementViewHolder.etReceived) {
      model.setReceived(stockMovementViewHolder.etReceived.getText().toString());
    } else if (v == stockMovementViewHolder.etIssued) {
      model.setIssued(stockMovementViewHolder.etIssued.getText().toString());
    } else if (v == stockMovementViewHolder.etPositiveAdjustment) {
      model
          .setPositiveAdjustment(stockMovementViewHolder.etPositiveAdjustment.getText().toString());
    } else if (v == stockMovementViewHolder.etNegativeAdjustment) {
      model
          .setNegativeAdjustment(stockMovementViewHolder.etNegativeAdjustment.getText().toString());
    } else if (v == stockMovementViewHolder.etDocumentNo) {
      model.setDocumentNo(stockMovementViewHolder.etDocumentNo.getText().toString());
    } else if (v == stockMovementViewHolder.etRequested) {
      model.setRequested(stockMovementViewHolder.etRequested.getText().toString());
    }
  }

  private void updateStockExistence(View v, StockMovementViewModel model, long currentStockOnHand,
      String text) {
    long number = 0;
    if (!StringUtils.isEmpty(text)) {
      number = Long.parseLong(text);
    }

    String stockExistence = "";
    if (v == stockMovementViewHolder.etReceived
        || v == stockMovementViewHolder.etPositiveAdjustment) {
      stockExistence = String.valueOf(currentStockOnHand + number);
    } else if (v == stockMovementViewHolder.etIssued
        || v == stockMovementViewHolder.etNegativeAdjustment) {
      stockExistence = String.valueOf(currentStockOnHand - number);
    }
    stockMovementViewHolder.txStockExistence.setText(stockExistence);
    model.setStockExistence(stockExistence);
  }
}
