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
import android.widget.EditText;
import android.widget.TextView;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import java.util.Date;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.BulkIssueLotViewModel;

@RunWith(LMISTestRunner.class)
public class BulkIssueLotAdapterTest {

  @Test
  public void testConvertEditType() {
    // given
    BulkIssueLotAdapter adapter = new BulkIssueLotAdapter();
    Context context = LMISTestApp.getContext();
    context.setTheme(R.style.AppTheme);
    BaseViewHolder holder = new BaseViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_bulk_issue_lot, null));
    BulkIssueLotViewModel mockLotViewModel = Mockito.mock(BulkIssueLotViewModel.class);
    Mockito.when(mockLotViewModel.getAmount()).thenReturn(1L);
    Mockito.when(mockLotViewModel.isExpired()).thenReturn(true);
    Mockito.when(mockLotViewModel.getLotNumber()).thenReturn("LotNumber");
    Mockito.when(mockLotViewModel.getLotSoh()).thenReturn(1L);
    Mockito.when(mockLotViewModel.getExpirationDate()).thenReturn(new Date());

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
}