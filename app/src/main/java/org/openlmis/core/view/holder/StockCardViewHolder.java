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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.enums.StockOnHandStatus;
import org.openlmis.core.googleanalytics.AnalyticsTracker;
import org.openlmis.core.googleanalytics.TrackerActions;
import org.openlmis.core.googleanalytics.TrackerCategories;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.utils.TextStyleUtil;
import org.openlmis.core.view.adapter.StockcardListLotAdapter;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import roboguice.RoboGuice;
import roboguice.inject.InjectView;

public class StockCardViewHolder extends BaseViewHolder {

  @InjectView(R.id.tv_product_name)
  TextView tvProductName;

  @InjectView(R.id.tv_stock_on_hand)
  TextView tvStockOnHand;

  @InjectView(R.id.tv_expiry_date_msg)
  TextView tvExpiryDateMsg;

  @InjectView(R.id.tv_stock_status)
  TextView tvStockStatus;

  @InjectView(R.id.rv_lot_container)
  RecyclerView rvLotList;

  protected StockService stockService;
  private final OnItemViewClickListener listener;

  public StockCardViewHolder(View itemView, OnItemViewClickListener listener) {
    super(itemView);
    this.listener = listener;
    this.stockService = RoboGuice.getInjector(context).getInstance(StockService.class);
  }

  public void populate(final InventoryViewModel inventoryViewModel, String queryKeyWord) {
    setListener(inventoryViewModel);
    inflateData(inventoryViewModel, queryKeyWord);
    inflateLotLayout(inventoryViewModel.getStockCard().getNonEmptyLotOnHandList());
  }

  protected void inflateData(InventoryViewModel inventoryViewModel, String queryKeyWord) {
    tvStockOnHand.setText(String.valueOf(inventoryViewModel.getStockOnHand()));
    tvProductName
        .setText(TextStyleUtil.getHighlightQueryKeyWord(queryKeyWord, inventoryViewModel.getProductStyledName()));

    initExpiryDateWarning(inventoryViewModel);
    initStockOnHandWarning(inventoryViewModel);
  }

  private void initExpiryDateWarning(InventoryViewModel inventoryViewModel) {
    Date earliestLotExpiryDate = inventoryViewModel.getStockCard().getEarliestLotExpiryDate();

    if (earliestLotExpiryDate != null) {
      Calendar earliestLotExpiryDateAddTwoDay = Calendar.getInstance();
      earliestLotExpiryDateAddTwoDay.setTime(earliestLotExpiryDate);
      earliestLotExpiryDateAddTwoDay.add(Calendar.DATE, 2);
      if (earliestLotExpiryDateAddTwoDay.getTime()
          .compareTo(new Date(LMISApp.getInstance().getCurrentTimeMillis())) <= 0) {
        showExpiryDateWithMessage(R.string.msg_expired_date, earliestLotExpiryDate);
        return;
      }
      if (DateUtil.calculateDateMonthOffset(new Date(LMISApp.getInstance().getCurrentTimeMillis()),
          earliestLotExpiryDateAddTwoDay.getTime()) <= 3) {
        showExpiryDateWithMessage(R.string.msg_expiring_date, earliestLotExpiryDate);
        return;
      }
    }
    hideExpiryDate();
  }

  private void showExpiryDateWithMessage(int expiryMsg, Date earliestExpiryDate) {
    tvExpiryDateMsg.setVisibility(View.VISIBLE);
    tvExpiryDateMsg.setText(context.getResources()
        .getString(expiryMsg, DateUtil.formatDateWithShortMonthAndYear(earliestExpiryDate)));
  }

  private void hideExpiryDate() {
    tvExpiryDateMsg.setVisibility(View.GONE);
  }

  private void setListener(final InventoryViewModel inventoryViewModel) {
    itemView.setOnClickListener(v -> {
      if (listener != null) {
        AnalyticsTracker.getInstance()
            .trackEvent(TrackerCategories.STOCK_MOVEMENT, TrackerActions.SELECT_STOCK_CARD);
        listener.onItemViewClick(inventoryViewModel);
      }
    });
  }

  private void initStockOnHandWarning(final InventoryViewModel viewModel) {
    StockOnHandStatus stockOnHandStatus = StockOnHandStatus
        .calculateStockOnHandLevel(viewModel.getStockCard(), viewModel.getStockOnHand());
    tvStockStatus.setText(context.getResources().getString(stockOnHandStatus.getDescription()));
    tvStockStatus.setBackgroundColor(ContextCompat.getColor(context, stockOnHandStatus.getBgColor()));
  }

  private void inflateLotLayout(List<LotOnHand> lotOnHandList) {
    rvLotList.setLayoutManager(new LinearLayoutManager(context));
    rvLotList.setAdapter(new StockcardListLotAdapter(lotOnHandList));
  }

  public interface OnItemViewClickListener {

    void onItemViewClick(InventoryViewModel inventoryViewModel);
  }
}
