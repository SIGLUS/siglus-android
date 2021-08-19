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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.googleanalytics.ScreenName;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.view.viewmodel.StockMovementHistoryViewModel;

@RunWith(LMISTestRunner.class)
public class StockMovementAdapterTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testConvert() {
    // given
    StockMovementItem movementItem = new StockMovementItemBuilder()
        .withMovementType(MovementType.RECEIVE)
        .withMovementReason(MovementReasonManager.DEFAULT_RECEIVE)
        .build();
    StockMovementHistoryViewModel viewModel = new StockMovementHistoryViewModel(movementItem);
    StockMovementAdapter stockMovementAdapter = new StockMovementAdapter();
    stockMovementAdapter.setFromPage(ScreenName.STOCK_CARD_MOVEMENT_SCREEN);
    BaseViewHolder holder = new BaseViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_stock_movement, null));

    // when
    stockMovementAdapter.convert(holder, viewModel);

    // then
    TextView tvReceived = holder.getView(R.id.tv_requested);
    Assert.assertEquals("10", tvReceived.getText());
    Assert.assertEquals(ContextCompat.getColor(LMISTestApp.getContext(), R.color.color_de1313),
        tvReceived.getCurrentTextColor());
    Assert.assertEquals(View.GONE, holder.getView(R.id.rv_stock_movement_lot_list).getVisibility());
  }
}