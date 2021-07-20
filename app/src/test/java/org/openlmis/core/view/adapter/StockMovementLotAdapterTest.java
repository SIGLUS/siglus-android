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
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.view.viewmodel.LotMovementHistoryViewModel;

@RunWith(LMISTestRunner.class)
public class StockMovementLotAdapterTest {

  private LotMovementItem lotMovementItem;

  @Before
  public void setUp() throws Exception {
    Lot mockLot = Mockito.mock(Lot.class);
    Mockito.when(mockLot.getLotNumber()).thenReturn("lotNumber");
    lotMovementItem = new LotMovementItemBuilder()
        .setLot(mockLot)
        .setMovementQuantity(100L)
        .setReason(MovementReasonManager.DEFAULT_RECEIVE)
        .setStockOnHand(1000L)
        .setStockMovementItem(Mockito.mock(StockMovementItem.class))
        .build();
  }

  @Test
  public void testConvert() {
    // given
    LotMovementHistoryViewModel viewModel = new LotMovementHistoryViewModel(MovementType.RECEIVE, lotMovementItem);
    StockMovementLotAdapter stockMovementLotAdapter = new StockMovementLotAdapter();
    BaseViewHolder holder = new BaseViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.view_stock_movement_line, null));

    // when
    stockMovementLotAdapter.convert(holder, viewModel);

    // then
    TextView tvReceived = holder.getView(R.id.tv_received);
    Assert.assertEquals("100", tvReceived.getText());
    Assert.assertEquals(ContextCompat.getColor(LMISTestApp.getContext(), R.color.color_de1313),
        tvReceived.getCurrentTextColor());
  }
}