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

package org.openlmis.core.view.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.openlmis.core.R;

public class StockMovementHeaderView extends FrameLayout {

  private TextView tvLotCode;

  public StockMovementHeaderView(@NonNull Context context) {
    this(context, null);
  }

  public StockMovementHeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StockMovementHeaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initView(context);
  }

  public void hideLotCodeHeaderView() {
    tvLotCode.setVisibility(GONE);

  }

  private void initView(@NonNull Context context) {
    final View rootView = inflate(context, R.layout.view_stock_movement_line, this);
    rootView.findViewById(R.id.ll_stock_movement_root).setBackgroundResource(R.color.color_9c9c9c);
    final TextView tvDate = rootView.findViewById(R.id.tv_date);
    tvDate.setTypeface(Typeface.DEFAULT_BOLD);
    tvDate.setText(context.getString(R.string.label_movement_date));

    final TextView tvReason = rootView.findViewById(R.id.tv_reason);
    tvReason.setTypeface(Typeface.DEFAULT_BOLD);
    tvReason.setText(context.getString(R.string.label_reason));

    tvLotCode = rootView.findViewById(R.id.tv_lot_code);
    tvLotCode.setTypeface(Typeface.DEFAULT_BOLD);
    tvLotCode.setText(context.getString(R.string.label_stockcard_lot_code));

    final TextView tvDocumentNumber = rootView.findViewById(R.id.tv_document_number);
    tvDocumentNumber.setTypeface(Typeface.DEFAULT_BOLD);
    tvDocumentNumber.setText(context.getString(R.string.label_document_no));

    final TextView tvReceived = rootView.findViewById(R.id.tv_received);
    tvReceived.setTypeface(Typeface.DEFAULT_BOLD);
    tvReceived.setText(context.getString(R.string.label_received_mmia));

    final TextView tvNegativeAdjustment = rootView.findViewById(R.id.tv_negative_adjustment);
    tvNegativeAdjustment.setTypeface(Typeface.DEFAULT_BOLD);
    tvNegativeAdjustment.setText(context.getString(R.string.label_negative_adjustment));

    final TextView tvPositiveAdjustment = rootView.findViewById(R.id.tv_positive_adjustment);
    tvPositiveAdjustment.setTypeface(Typeface.DEFAULT_BOLD);
    tvPositiveAdjustment.setText(context.getString(R.string.label_positive_adjustment));

    final TextView tvIssued = rootView.findViewById(R.id.tv_issued);
    tvIssued.setTypeface(Typeface.DEFAULT_BOLD);
    tvIssued.setText(context.getString(R.string.label_issued_mmia));

    final TextView tvStockOnHand = rootView.findViewById(R.id.tv_stock_on_hand);
    tvStockOnHand.setTypeface(Typeface.DEFAULT_BOLD);
    tvStockOnHand.setText(context.getString(R.string.label_stock_existence));

    final TextView tvRequested = rootView.findViewById(R.id.tv_requested);
    tvRequested.setTypeface(Typeface.DEFAULT_BOLD);
    tvRequested.setText(context.getString(R.string.label_requested));

    final TextView tvSignature = rootView.findViewById(R.id.tv_signature);
    tvSignature.setTypeface(Typeface.DEFAULT_BOLD);
    tvSignature.setText(context.getString(R.string.label_signature));
  }
}
