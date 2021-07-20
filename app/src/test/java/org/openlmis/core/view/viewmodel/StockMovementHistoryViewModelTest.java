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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockMovementItemBuilder;

@RunWith(LMISTestRunner.class)
public class StockMovementHistoryViewModelTest {

  @Test
  public void shouldCorrectSetValues() {
    // given & when
    StockMovementItem movementItem = new StockMovementItemBuilder().build();
    StockMovementHistoryViewModel viewModel = new StockMovementHistoryViewModel(movementItem);

    // then
    Assert.assertEquals("10 Oct 2010", viewModel.getMovementDate());
    Assert.assertEquals("200", viewModel.getStockOnHand());
    Assert.assertEquals("signature", viewModel.getSignature());
    Assert.assertEquals("10", viewModel.getRequested());
    Assert.assertEquals(0, viewModel.getLotViewModelList().size());
  }

  @Test
  public void testNeedShowRed() {
    // given
    StockMovementItem initialInventoryMovementItem = new StockMovementItemBuilder()
        .withMovementType(MovementType.INITIAL_INVENTORY)
        .build();
    StockMovementHistoryViewModel initialInventoryViewModel = new StockMovementHistoryViewModel(
        initialInventoryMovementItem);

    StockMovementItem physicalInventoryMovementItem = new StockMovementItemBuilder()
        .withMovementType(MovementType.PHYSICAL_INVENTORY)
        .build();
    StockMovementHistoryViewModel physicalInventoryViewModel = new StockMovementHistoryViewModel(
        physicalInventoryMovementItem);

    StockMovementItem receiveMovementItem = new StockMovementItemBuilder()
        .withMovementType(MovementType.RECEIVE)
        .build();
    StockMovementHistoryViewModel receiveViewModel = new StockMovementHistoryViewModel(receiveMovementItem);

    StockMovementItem unpackKitMovementItem = new StockMovementItemBuilder()
        .withMovementType(MovementType.RECEIVE)
        .withMovementReason(MovementReasonManager.UNPACK_KIT)
        .build();
    StockMovementHistoryViewModel unpackKitViewModel = new StockMovementHistoryViewModel(unpackKitMovementItem);

    // when & then
    Assert.assertTrue(initialInventoryViewModel.needShowRed());
    Assert.assertTrue(physicalInventoryViewModel.needShowRed());
    Assert.assertTrue(receiveViewModel.needShowRed());
    Assert.assertTrue(unpackKitViewModel.needShowRed());
  }

  @Test
  public void testIsNoStock() {
    // given
    StockMovementItem noStockMovementItem = new StockMovementItemBuilder()
        .withMovementType(MovementType.INITIAL_INVENTORY)
        .withQuantity(0)
        .withStockOnHand(0)
        .withMovementReason(MovementReasonManager.INVENTORY)
        .build();
    StockMovementHistoryViewModel noStockViewModel = new StockMovementHistoryViewModel(noStockMovementItem);

    // when & then
    Assert.assertTrue(noStockViewModel.isNoStock());
  }
}