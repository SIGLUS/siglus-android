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

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.view.adapter.BulkIssueLotAdapter.BulkIssueLotViewHolder;
import org.openlmis.core.view.viewmodel.BulkIssueLotViewModel;

@RunWith(LMISTestRunner.class)
public class BulkIssueLotAdapterTest {

  private BulkIssueLotAdapter adapter;
  private BulkIssueLotViewModel mockLotViewModel;

  @Before
  public void setup(){
    adapter = new BulkIssueLotAdapter();
    LMISTestApp.getContext().setTheme(R.style.AppTheme);

    mockLotViewModel = Mockito.mock(BulkIssueLotViewModel.class);
    LotOnHand mockLotOnHand = Mockito.mock(LotOnHand.class);
    Lot mockLot = Mockito.mock(Lot.class);
    Mockito.when(mockLotOnHand.getLot()).thenReturn(mockLot);
    Mockito.when(mockLotViewModel.getAmount()).thenReturn(1L);
    Mockito.when(mockLotViewModel.isExpired()).thenReturn(true);
    Mockito.when(mockLotViewModel.getLotOnHand()).thenReturn(mockLotOnHand);
    Mockito.when(mockLotOnHand.getQuantityOnHand()).thenReturn(1L);
    Mockito.when(mockLot.getLotNumber()).thenReturn("LotNumber");
    Mockito.when(mockLot.getExpirationDate()).thenReturn(new Date());
  }

  @Test
  public void testConvertEditType() {
    // given
    BulkIssueLotViewHolder holder = adapter.new BulkIssueLotViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_lot_edit, null));

    // when
    adapter.convert(holder, mockLotViewModel);

    // when
    Assert.assertEquals(View.VISIBLE, holder.getView(R.id.tv_expired_tips).getVisibility());
    Assert.assertEquals(View.INVISIBLE, holder.getView(R.id.til_amount).getVisibility());
    EditText etAmount = holder.getView(R.id.et_amount);
    Assert.assertEquals("1", etAmount.getText().toString());
    TextView tvExistingOnHand = holder.getView(R.id.tv_existing_lot_on_hand);
    Assert.assertEquals("Existing stock on hand of lot  1", tvExistingOnHand.getText().toString());
  }

  @Test
  public void shouldSetAmountAfterAmountChange(){
    // given
    BulkIssueLotViewHolder holder = adapter.new BulkIssueLotViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_bulk_issue_lot_edit, null));
    holder.populate(mockLotViewModel);

    // when
    holder.getAmountTextWatcher().afterTextChanged(new SpannableStringBuilder("123"));

    // then
    Mockito.verify(mockLotViewModel,Mockito.times(1)).setAmount(123L);
  }
}