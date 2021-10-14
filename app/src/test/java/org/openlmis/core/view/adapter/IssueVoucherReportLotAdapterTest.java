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

import static org.junit.Assert.assertEquals;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.constant.FieldConstants;
import org.openlmis.core.enumeration.OrderStatus;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.PodProductItem;
import org.openlmis.core.model.PodProductLotItem;
import org.openlmis.core.model.builder.LotBuilder;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.adapter.IssueVoucherReportLotAdapter.IssueVoucherReportLotViewHolder;
import org.openlmis.core.view.viewmodel.IssueVoucherReportLotViewModel;

@RunWith(LMISTestRunner.class)
public class IssueVoucherReportLotAdapterTest {

  private IssueVoucherReportLotAdapter adapter;
  private IssueVoucherReportLotViewHolder holder;
  private IssueVoucherReportLotViewModel lotViewModel;

  @Before
  public void setup() {
    Lot lot = new LotBuilder()
        .setProduct(ProductBuilder.buildAdultProduct())
        .setLotNumber(FieldConstants.LOT_NUMBER)
        .setExpirationDate(new Date())
        .build();
    PodProductLotItem podProductLotItem = PodProductLotItem.builder()
        .podProductItem(null)
        .lot(lot)
        .shippedQuantity(10L)
        .build();
    PodProductItem podProductItem = PodProductItem.builder()
        .product(ProductBuilder.buildAdultProduct())
        .build();
    lotViewModel = new IssueVoucherReportLotViewModel(podProductLotItem, podProductItem, OrderStatus.SHIPPED, true, true);
    adapter = new IssueVoucherReportLotAdapter(LMISTestApp.getContext(), Arrays.asList(lotViewModel));
    holder = adapter.new IssueVoucherReportLotViewHolder(
        LayoutInflater.from(LMISTestApp.getContext()).inflate(R.layout.item_issue_voucher_report_lot, null));
  }

  @Test
  public void testCorrectUIFocusableForLocalDraft()  {
    // given
    String notes = "notes";
    lotViewModel.setNotes(notes);

    // when
    holder.populate(lotViewModel, 0);

    // then
    assertEquals(true, holder.getView(R.id.et_quantity_accepted).isFocusable());
    assertEquals(true, holder.getView(R.id.et_quantity_shipped).isFocusable());
    assertEquals(true, holder.getView(R.id.et_note).isFocusable());
  }

  @Test
  public void testCorrectUIFocusableForRemoteIssueVoucher()  {
    // given
    String notes = "test";
    lotViewModel.setNotes(notes);
    lotViewModel.setLocal(false);

    // when
    holder.populate(lotViewModel, 0);

    // then
    assertEquals(true, holder.getView(R.id.et_quantity_accepted).isFocusable());
    assertEquals(false, holder.getView(R.id.et_quantity_shipped).isFocusable());
    assertEquals(true, holder.getView(R.id.et_note).isFocusable());
  }

  @Test
  public void testCorrectUIFocusableForRemoteReceived()  {
    // given
    lotViewModel.setLocal(false);
    lotViewModel.setOrderStatus(OrderStatus.RECEIVED);

    // when
    holder.populate(lotViewModel, 0);

    // then
    assertEquals(false, holder.getView(R.id.et_quantity_accepted).isFocusable());
    assertEquals(false, holder.getView(R.id.et_quantity_shipped).isFocusable());
    assertEquals(false, holder.getView(R.id.et_note).isFocusable());
  }

  @Test
  public void testValueForIssueVoucher() {
    // given
    lotViewModel.setAcceptedQuantity(3L);
    lotViewModel.setRejectedReason("test");

    // when
    holder.populate(lotViewModel, 0);

    // then
    assertEquals("3", ((EditText)holder.getView(R.id.et_quantity_accepted)).getText().toString());
    assertEquals("10", ((EditText)holder.getView(R.id.et_quantity_shipped)).getText().toString());
    TextView returnedValue = holder.getView(R.id.tv_quantity_returned);
    assertEquals("-7", returnedValue.getText().toString());
    TextView reason = holder.getView(R.id.tv_rejection_reason);
    assertEquals("test", (reason.getText().toString()));
  }

  @Test
  public void testCorrectUIForRemoteReceived()  {
    // given
    lotViewModel.setAcceptedQuantity(4L);
    lotViewModel.setRejectedReason("reason2");
    lotViewModel.setOrderStatus(OrderStatus.RECEIVED);

    // when
    holder.populate(lotViewModel, 0);

    // then
    assertEquals("4", ((EditText)holder.getView(R.id.et_quantity_accepted)).getText().toString());
    assertEquals("10", ((EditText)holder.getView(R.id.et_quantity_shipped)).getText().toString());
    TextView returnedValue = holder.getView(R.id.tv_quantity_returned);
    assertEquals("-6", returnedValue.getText().toString());
    TextView reason = holder.getView(R.id.tv_rejection_reason);
    assertEquals("reason2", (reason.getText().toString()));
    ImageView reasonLogo = holder.getView(R.id.iv_rejection_reason);
    assertEquals(View.GONE, reasonLogo.getVisibility());
  }

  @Test
  public void testCorrectUIForValueNull() {
    // given
    lotViewModel.setShippedQuantity(null);
    lotViewModel.setAcceptedQuantity(null);
    lotViewModel.setRejectedReason("reason");
    lotViewModel.setOrderStatus(OrderStatus.RECEIVED);

    // when
    holder.populate(lotViewModel, 0);

    // then
    assertEquals("", ((EditText)holder.getView(R.id.et_quantity_accepted)).getText().toString());
    assertEquals("", ((EditText)holder.getView(R.id.et_quantity_shipped)).getText().toString());
    TextView returnedValue = holder.getView(R.id.tv_quantity_returned);
    assertEquals("", returnedValue.getText().toString());
    TextView reason = holder.getView(R.id.tv_rejection_reason);
    assertEquals("reason", (reason.getText().toString()));
    ImageView reasonLogo = holder.getView(R.id.iv_rejection_reason);
    assertEquals(View.GONE, reasonLogo.getVisibility());
  }
}
