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

package org.openlmis.core.view.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.LotBuilder;
import org.openlmis.core.model.builder.LotMovementItemBuilder;
import org.openlmis.core.persistence.migrations.ChangeMovementReasonToCode;
import org.openlmis.core.utils.DateUtil;

@RunWith(LMISTestRunner.class)
public class LotMovementHistoryViewModelTest {

  private LotMovementItem lotMovementItem;

  @Before
  public void setUp() throws Exception {
    Lot mockLot = Mockito.mock(Lot.class);
    Mockito.when(mockLot.getLotNumber()).thenReturn("lotNumber");
    lotMovementItem = new LotMovementItemBuilder()
        .setLot(mockLot)
        .setMovementQuantity(0L)
        .setReason("")
        .setStockOnHand(0L)
        .setStockMovementItem(Mockito.mock(StockMovementItem.class))
        .build();
  }

  @Test
  public void shouldShowEmptyWhenGivenUnknownReason() {
    // given
    lotMovementItem.setMovementQuantity(100L);
    lotMovementItem.setReason("unknown");
    lotMovementItem.setDocumentNumber("aaa");
    LotMovementHistoryViewModel viewModel = new LotMovementHistoryViewModel(MovementType.RECEIVE, lotMovementItem);
    // then
    Assert.assertEquals("",viewModel.getMovementDesc());
  }

  @Test
  public void shouldGetCorrectReceivedValue() {
    // given
    lotMovementItem.setMovementQuantity(100L);
    lotMovementItem.setReason(ChangeMovementReasonToCode.DEFAULT_RECEIVE);
    lotMovementItem.setDocumentNumber("aaa");
    LotMovementHistoryViewModel viewModel = new LotMovementHistoryViewModel(MovementType.RECEIVE, lotMovementItem);

    // when & then
    Assert.assertEquals("100", viewModel.getReceived());
    Assert.assertNull(viewModel.getIssued());
    Assert.assertNull(viewModel.getNegativeAdjustment());
    Assert.assertNull(viewModel.getPositiveAdjustment());
  }

  @Test
  public void shouldGetCorrectIssuedValue() {
    // given
    lotMovementItem.setMovementQuantity(200L);
    lotMovementItem.setReason(ChangeMovementReasonToCode.DEFAULT_ISSUE);
    LotMovementHistoryViewModel viewModel = new LotMovementHistoryViewModel(MovementType.ISSUE, lotMovementItem);

    // when & then
    Assert.assertNull(viewModel.getReceived());
    Assert.assertEquals("200", viewModel.getIssued());
    Assert.assertNull(viewModel.getNegativeAdjustment());
    Assert.assertNull(viewModel.getPositiveAdjustment());
  }

  @Test
  public void shouldGetCorrectNegativeAdjustment() {
    // given
    lotMovementItem.setMovementQuantity(300L);
    lotMovementItem.setReason(ChangeMovementReasonToCode.DEFAULT_NEGATIVE_ADJUSTMENT);
    LotMovementHistoryViewModel viewModel = new LotMovementHistoryViewModel(MovementType.NEGATIVE_ADJUST,
        lotMovementItem);

    // when & then
    Assert.assertNull(viewModel.getReceived());
    Assert.assertNull(viewModel.getIssued());
    Assert.assertEquals("300", viewModel.getNegativeAdjustment());
    Assert.assertNull(viewModel.getPositiveAdjustment());
  }

  @Test
  public void shouldGetCorrectPositiveAdjustment() {
    // given
    lotMovementItem.setMovementQuantity(400L);
    lotMovementItem.setReason(ChangeMovementReasonToCode.DEFAULT_POSITIVE_ADJUSTMENT);
    LotMovementHistoryViewModel viewModel = new LotMovementHistoryViewModel(MovementType.POSITIVE_ADJUST,
        lotMovementItem);

    // when & then
    Assert.assertNull(viewModel.getReceived());
    Assert.assertNull(viewModel.getIssued());
    Assert.assertNull(viewModel.getNegativeAdjustment());
    Assert.assertEquals("400", viewModel.getPositiveAdjustment());
  }

  @Test
  public void testShouldShowRed() {
    // given
    lotMovementItem.setReason(ChangeMovementReasonToCode.DEFAULT_ISSUE);
    LotMovementHistoryViewModel initialInventoryViewModel = new LotMovementHistoryViewModel(
        MovementType.INITIAL_INVENTORY, lotMovementItem);

    // then
    Assert.assertTrue(initialInventoryViewModel.shouldShowRed());

    lotMovementItem.setReason(MovementReasonManager.INVENTORY);
    LotMovementHistoryViewModel physicalInventoryViewModel = new LotMovementHistoryViewModel(
        MovementType.PHYSICAL_INVENTORY, lotMovementItem);

    // then
    Assert.assertTrue(physicalInventoryViewModel.shouldShowRed());

    lotMovementItem.setReason(ChangeMovementReasonToCode.DEFAULT_RECEIVE);
    LotMovementHistoryViewModel receiveViewModel = new LotMovementHistoryViewModel(
        MovementType.RECEIVE, lotMovementItem);

    // then
    Assert.assertTrue(receiveViewModel.shouldShowRed());


    lotMovementItem.setReason(MovementReasonManager.UNPACK_KIT);
    LotMovementHistoryViewModel unpackKitViewModel = new LotMovementHistoryViewModel(
        MovementType.ISSUE, lotMovementItem);
    // then
    Assert.assertTrue(unpackKitViewModel.shouldShowRed());
  }

  @Test
  public void testCompareTo() {
    // given
    LotMovementItem lotMovementItemOne = new LotMovementItemBuilder()
        .setLot(new LotBuilder()
            .setExpirationDate(DateUtil.parseString("2020-01-02", DateUtil.DB_DATE_FORMAT))
            .build())
        .setReason(ChangeMovementReasonToCode.DEFAULT_ISSUE)
        .setMovementQuantity(1L)
        .build();
    LotMovementItem lotMovementItemTwo = new LotMovementItemBuilder()
        .setLot(new LotBuilder()
            .setExpirationDate(DateUtil.parseString("2020-01-01", DateUtil.DB_DATE_FORMAT))
            .build())
        .setReason(ChangeMovementReasonToCode.DEFAULT_ISSUE)
        .setMovementQuantity(1L)
        .build();
    LotMovementHistoryViewModel viewModelOne = new LotMovementHistoryViewModel(MovementType.RECEIVE,
        lotMovementItemOne);
    LotMovementHistoryViewModel viewModelTwo = new LotMovementHistoryViewModel(MovementType.RECEIVE,
        lotMovementItemTwo);
    ArrayList<LotMovementHistoryViewModel> viewModels = new ArrayList<>();
    viewModels.add(viewModelOne);
    viewModels.add(viewModelTwo);

    // when
    Collections.sort(viewModels);

    // then
    Assert.assertEquals(viewModelOne, viewModels.get(0));
    Assert.assertEquals(viewModelTwo, viewModels.get(1));
  }
}