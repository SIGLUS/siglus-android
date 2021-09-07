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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.manager.MovementReasonManager.MovementType;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.persistence.migrations.ChangeMovementReasonToCode;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.widget.NewMovementLotListView;

@RunWith(LMISTestRunner.class)
public class StockMovementViewModelTest extends LMISRepositoryUnitTest {

  public static final String ISSUE = "issue";
  public static final String NUMBER_100 = "100";
  public static final String DOCUMENT_NO = "111";
  public static final String STOCK_EXISTENCE = "123";
  private StockMovementViewModel stockMovementViewModel;
  private MovementReasonManager.MovementReason movementReason;
  private Calendar featureCalendar;
  private Calendar featureCalendar1;
  private Calendar oldCalendar;

  @Before
  public void setup() {
    stockMovementViewModel = new StockMovementViewModel();
    movementReason = new MovementReasonManager.MovementReason(
        MovementReasonManager.MovementType.RECEIVE, "RECEIVE", "receive");
    featureCalendar = Calendar.getInstance();
    featureCalendar1 = Calendar.getInstance();
    oldCalendar = Calendar.getInstance();
    featureCalendar.add(Calendar.YEAR, 1);
    featureCalendar1.add(Calendar.YEAR, 2);
    oldCalendar.add(Calendar.YEAR, -1);
  }

  @Test
  public void shouldReturnValidWhenStockMovementViewModelHasAllData() {
    stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setReceived(NUMBER_100);
    assertTrue(stockMovementViewModel.validateInputValid());
  }

  @Test
  public void shouldReturnFalseIfMovementDateIsMissing() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setReceived(NUMBER_100);
    assertFalse(stockMovementViewModel.validateEmpty());
  }

  @Test
  public void shouldReturnFalseIfReasonIsMissing() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementDate("2016-11-20");
    stockMovementViewModel.setReceived(NUMBER_100);
    assertFalse(stockMovementViewModel.validateEmpty());
  }

  @Test
  public void shouldReturnFalseIfAllQuantitiesAreEmpty() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setMovementDate("2016-11-20");
    assertFalse(stockMovementViewModel.validateEmpty());
  }

  @Test
  public void shouldSetRequestedAsNullWhenRequestedIsNull() {
    stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setReceived(NUMBER_100);
    StockMovementItem stockMovementItem = stockMovementViewModel
        .convertViewToModel(new StockCard());
    assertNull(stockMovementItem.getRequested());
  }

  @Test
  public void shouldSetRequestedAsNullWhenRequestedIsEmpty() {
    stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(MovementReasonManager.MovementType.ISSUE);
    stockMovementViewModel.setMovementReason(ISSUE);
    stockMovementViewModel.setIssued(NUMBER_100);
    stockMovementViewModel.setRequested("");
    StockMovementItem stockMovementItem = stockMovementViewModel
        .convertViewToModel(new StockCard());
    assertNull(stockMovementItem.getRequested());
  }

  @Test
  public void shouldReturnFalseIfReceivedIsZero() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setReceived("0");
    assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
  }

  @Test
  public void shouldReturnFalseIfIssueIsZero() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setIssued("0");
    assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
  }

  @Test
  public void shouldReturnFalseIfNegativeAdjustmentIsZero() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setNegativeAdjustment("0");
    assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
  }

  @Test
  public void shouldReturnFalseIfPositiveAdjustmentIsZero() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setPositiveAdjustment("0");
    assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
  }

  @Test
  public void shouldReturnTrueIfReceivedIsNotZero() {
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(movementReason.getMovementType());
    stockMovementViewModel.setMovementReason(movementReason.getCode());
    stockMovementViewModel.setReceived("12");
    assertTrue(stockMovementViewModel.validateQuantitiesNotZero());
  }

  @Test
  public void shouldSetRequestedCorrectlyWhenRequestedNotEmptyAndNotNull() {
    stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
    stockMovementViewModel.setStockExistence(STOCK_EXISTENCE);
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setMovementType(MovementType.ISSUE);
    stockMovementViewModel.setMovementReason(ISSUE);
    stockMovementViewModel.setIssued(NUMBER_100);
    stockMovementViewModel.setRequested("999");
    StockMovementItem stockMovementItem = stockMovementViewModel
        .convertViewToModel(new StockCard());
    assertThat(stockMovementItem.getRequested(), is(999L));
  }

  @Test
  public void shouldCalculateNewLotListMovementQuantityToStockOnHandWhenConvertViewModel() {
    StockCard stockCard = new StockCard();
    stockCard.setId(1);

    stockMovementViewModel.setStockExistence("1");
    stockMovementViewModel.setMovementType(MovementType.RECEIVE);
    stockMovementViewModel.setMovementReason("receive");
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setReceived("0");
    stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
    LotMovementViewModel lot1 = new LotMovementViewModel();
    lot1.setQuantity("1");
    lot1.setLotNumber("AAA");
    lot1.setExpiryDate(DateUtil.formatDateWithoutDay(new Date()));
    stockMovementViewModel.getNewLotMovementViewModelList().addAll(Arrays.asList(lot1));

    StockMovementItem convertedStockMovementItem = stockMovementViewModel
        .convertViewToModel(stockCard);

    assertEquals(2, convertedStockMovementItem.getStockOnHand());
  }

  @Test
  public void shouldCalculateNewAndExistingLotListMovementQuantityToStockOnHandWhenConvertViewModel() {
    StockCard stockCard = new StockCard();
    stockCard.setId(1);

    stockMovementViewModel.setStockExistence("1");
    stockMovementViewModel.setMovementType(MovementReasonManager.MovementType.RECEIVE);
    stockMovementViewModel.setMovementReason("receive");
    stockMovementViewModel.setDocumentNo(DOCUMENT_NO);
    stockMovementViewModel.setReceived("0");
    stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
    LotMovementViewModel lot1 = new LotMovementViewModel();
    lot1.setQuantity("1");
    lot1.setLotNumber("AAA");
    lot1.setExpiryDate(DateUtil.formatDateWithoutDay(new Date()));
    LotMovementViewModel existingLot = new LotMovementViewModel();
    existingLot.setQuantity("1");
    existingLot.setLotNumber("BBB");
    existingLot.setExpiryDate(DateUtil.formatDateWithoutDay(new Date()));

    stockMovementViewModel.getExistingLotMovementViewModelList().addAll(Arrays.asList(existingLot));
    stockMovementViewModel.getNewLotMovementViewModelList().addAll(Arrays.asList(lot1));

    StockMovementItem convertedStockMovementItem = stockMovementViewModel
        .convertViewToModel(stockCard);

    assertEquals(3, convertedStockMovementItem.getStockOnHand());
  }

  @Test
  public void shouldValidateEarlyExpiredLotIssued() {
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(oldCalendar.getTime()))
        .setQuantity(null).build());
    assertEquals(NewMovementLotListView.LotStatus.CONTAIN_EXPIRED_LOTS,
        stockMovementViewModel.getSoonestToExpireLotsIssued());

    stockMovementViewModel.existingLotMovementViewModelList.clear();
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
        .setQuantity("50").build());
    assertEquals(NewMovementLotListView.LotStatus.DEFAULT_STATUS,
        stockMovementViewModel.getSoonestToExpireLotsIssued());

    stockMovementViewModel.existingLotMovementViewModelList.clear();
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
        .setQuantity("50").build());
    // LotMovementViewModelBuilder().build() : expireDate == null
    assertEquals(NewMovementLotListView.LotStatus.CONTAIN_EXPIRED_LOTS,
        stockMovementViewModel.getSoonestToExpireLotsIssued());

    stockMovementViewModel.existingLotMovementViewModelList.clear();
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
        .setQuantity(NUMBER_100).build());
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
        .setQuantity("50").build());
    assertEquals(NewMovementLotListView.LotStatus.CONTAIN_EXPIRED_LOTS,
        stockMovementViewModel.getSoonestToExpireLotsIssued());

    stockMovementViewModel.existingLotMovementViewModelList.clear();
    stockMovementViewModel.existingLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().build());
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
        .setQuantity("50")
        .build());
    assertEquals(NewMovementLotListView.LotStatus.CONTAIN_EXPIRED_LOTS,
        stockMovementViewModel.getSoonestToExpireLotsIssued());

    stockMovementViewModel.existingLotMovementViewModelList.clear();
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
        .setQuantity(null)
        .build());
    stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
        .setLotSOH(NUMBER_100)
        .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar1.getTime()))
        .setQuantity("10")
        .build());
    assertEquals(NewMovementLotListView.LotStatus.NOT_SOONEST_TO_EXPIRE_LOTS_ISSUED,
        stockMovementViewModel.getSoonestToExpireLotsIssued());
  }

  @Test
  public void shouldValidateLotChange() throws Exception {
    stockMovementViewModel.existingLotMovementViewModelList.add(
        new LotMovementViewModelBuilder().setLotSOH(NUMBER_100).setQuantity("50")
            .setHasLotDataChanged(false).build());
    assertFalse(stockMovementViewModel.hasLotDataChanged());

    stockMovementViewModel.newLotMovementViewModelList
        .add(new LotMovementViewModelBuilder().setLotSOH(NUMBER_100).setQuantity("30").build());
    assertTrue(stockMovementViewModel.hasLotDataChanged());

    stockMovementViewModel.newLotMovementViewModelList.clear();
    stockMovementViewModel.existingLotMovementViewModelList.add(
        new LotMovementViewModelBuilder().setLotSOH(NUMBER_100).setQuantity("50")
            .setHasLotDataChanged(true).build());
    stockMovementViewModel.setSignature("signa");
    stockMovementViewModel.setMovementType(MovementType.ISSUE);
    stockMovementViewModel.setMovementReason(ChangeMovementReasonToCode.DEFAULT_ISSUE);
    assertTrue(stockMovementViewModel.hasLotDataChanged());
    assertTrue(stockMovementViewModel.movementQuantitiesExist());
    assertFalse(stockMovementViewModel.isLotEmpty());
    assertTrue(stockMovementViewModel.validateSignature());
    assertTrue(stockMovementViewModel.validateMovementReason());
  }

  @Test
  public void shouldCreateFromStockMovementItemAndCheckViewModelEqual() {
    Product product = new ProductBuilder().setType(Product.MEDICINE_TYPE_ADULT)
        .setCode("productCode")
        .setStrength("serious")
        .setProductId(1)
        .setPrimaryName("Primary product name")
        .build();
    StockCard stockCard = new StockCardBuilder()
        .setProduct(product)
        .setStockOnHand(200)
        .setStockCardId(1)
        .build();
    StockMovementItem item = new StockMovementItem();
    item.setStockCard(stockCard);
    item.setDocumentNumber("documentNumber1");
    item.setMovementDate(new Date());
    item.setStockOnHand(200);
    item.setSignature("signa");
    item.setMovementType(MovementType.ISSUE);
    item.setReason(ChangeMovementReasonToCode.DEFAULT_ISSUE);

    StockMovementViewModel stockMovementViewModel1 = new StockMovementViewModel(item);
    StockMovementViewModel stockMovementViewModel2 = new StockMovementViewModel(item);

    assertTrue(stockMovementViewModel1.validateSignature());
    assertEquals("documentNumber1", stockMovementViewModel1.documentNo);
    assertFalse(stockMovementViewModel1.isDraft);
    assertEquals(stockMovementViewModel1.movementDate, DateUtil.formatDate(new Date()));
    assertTrue(stockMovementViewModel1.isLotEmpty());
    assertTrue(stockMovementViewModel1.validateMovementReason());

    assertEquals(stockMovementViewModel1, stockMovementViewModel2);
  }

}
