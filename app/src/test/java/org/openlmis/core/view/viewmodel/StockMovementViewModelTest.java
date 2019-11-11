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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.widget.NewMovementLotListView;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class StockMovementViewModelTest extends LMISRepositoryUnitTest {

    private StockMovementViewModel stockMovementViewModel;
    private MovementReasonManager.MovementReason movementReason;
    private Calendar featureCalendar;
    private Calendar featureCalendar1;
    private Calendar oldCalendar;

    @Before
    public void setup() {
        stockMovementViewModel = new StockMovementViewModel();
        movementReason = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.RECEIVE, "RECEIVE", "receive");
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
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setReceived("100");
        assertTrue(stockMovementViewModel.validateInputValid());
    }

    @Test
    public void shouldReturnFalseIfMovementDateIsMissing() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setReceived("100");
        assertFalse(stockMovementViewModel.validateEmpty());
    }

    @Test
    public void shouldReturnFalseIfReasonIsMissing() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setMovementDate("2016-11-20");
        stockMovementViewModel.setReceived("100");
        assertFalse(stockMovementViewModel.validateEmpty());
    }

    @Test
    public void shouldReturnFalseIfAllQuantitiesAreEmpty() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setMovementDate("2016-11-20");
        assertFalse(stockMovementViewModel.validateEmpty());
    }

    @Test
    public void shouldSetRequestedAsNullWhenRequestedIsNull() throws Exception {
        stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setReceived("100");
        StockMovementItem stockMovementItem = stockMovementViewModel.convertViewToModel(new StockCard());
        assertNull(stockMovementItem.getRequested());
    }

    @Test
    public void shouldSetRequestedAsNullWhenRequestedIsEmpty() throws Exception {
        stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "issue", "issue"));
        stockMovementViewModel.setIssued("100");
        stockMovementViewModel.setRequested("");
        StockMovementItem stockMovementItem = stockMovementViewModel.convertViewToModel(new StockCard());
        assertNull(stockMovementItem.getRequested());
    }

    @Test
    public void shouldReturnFalseIfReceivedIsZero() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setReceived("0");
        assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
    }

    @Test
    public void shouldReturnFalseIfIssueIsZero() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setIssued("0");
        assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
    }

    @Test
    public void shouldReturnFalseIfNegativeAdjustmentIsZero() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setNegativeAdjustment("0");
        assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
    }

    @Test
    public void shouldReturnFalseIfPositiveAdjustmentIsZero() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setPositiveAdjustment("0");
        assertFalse(stockMovementViewModel.validateQuantitiesNotZero());
    }

    @Test
    public void shouldReturnTrueIfReceivedIsNotZero() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason);
        stockMovementViewModel.setReceived("12");
        assertTrue(stockMovementViewModel.validateQuantitiesNotZero());
    }

    @Test
    public void shouldSetRequestedCorrectlyWhenRequestedNotEmptyAndNotNull() throws Exception {
        stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason(movementReason = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.ISSUE, "issue", "issue"));
        stockMovementViewModel.setIssued("100");
        stockMovementViewModel.setRequested("999");
        StockMovementItem stockMovementItem = stockMovementViewModel.convertViewToModel(new StockCard());
        assertThat(stockMovementItem.getRequested(), is(999L));
    }

    @Test
    public void shouldCalculateNewLotListMovementQuantityToStockOnHandWhenConvertViewModel() throws ParseException {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);

        stockMovementViewModel.setStockExistence("1");
        stockMovementViewModel.setReason(movementReason = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.RECEIVE, "receive", "entries"));
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReceived("0");
        stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
        LotMovementViewModel lot1 = new LotMovementViewModel();
        lot1.setQuantity("1");
        lot1.setLotNumber("AAA");
        lot1.setExpiryDate(DateUtil.formatDateWithoutDay(new Date()));
        stockMovementViewModel.getNewLotMovementViewModelList().addAll(Arrays.asList(lot1));

        StockMovementItem convertedStockMovementItem = stockMovementViewModel.convertViewToModel(stockCard);

        assertEquals(2, convertedStockMovementItem.getStockOnHand());
    }

    @Test
    public void shouldCalculateNewAndExistingLotListMovementQuantityToStockOnHandWhenConvertViewModel() throws ParseException {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);

        stockMovementViewModel.setStockExistence("1");
        stockMovementViewModel.setReason(movementReason = new MovementReasonManager.MovementReason(MovementReasonManager.MovementType.RECEIVE, "receive", "entries"));
        stockMovementViewModel.setDocumentNo("111");
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

        StockMovementItem convertedStockMovementItem = stockMovementViewModel.convertViewToModel(stockCard);

        assertEquals(3, convertedStockMovementItem.getStockOnHand());
    }

    @Test
    public void shouldValidateEarlyExpiredLotIssued() throws Exception {
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(oldCalendar.getTime()))
                .setQuantity(null).build());
        assertEquals(NewMovementLotListView.LotStatus.containExpiredLots, stockMovementViewModel.getSoonestToExpireLotsIssued());

        stockMovementViewModel.existingLotMovementViewModelList.clear();
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
                .setQuantity("50").build());
        assertEquals(NewMovementLotListView.LotStatus.defaultStatus, stockMovementViewModel.getSoonestToExpireLotsIssued());

        stockMovementViewModel.existingLotMovementViewModelList.clear();
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
                .setQuantity("50").build());
        // LotMovementViewModelBuilder().build() : expireDate == null
        assertEquals(NewMovementLotListView.LotStatus.containExpiredLots, stockMovementViewModel.getSoonestToExpireLotsIssued());

        stockMovementViewModel.existingLotMovementViewModelList.clear();
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
                .setQuantity("100").build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
                .setQuantity("50").build());
        assertEquals(NewMovementLotListView.LotStatus.containExpiredLots, stockMovementViewModel.getSoonestToExpireLotsIssued());


        stockMovementViewModel.existingLotMovementViewModelList.clear();
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
                .setQuantity("50")
                .build());
        assertEquals(NewMovementLotListView.LotStatus.containExpiredLots, stockMovementViewModel.getSoonestToExpireLotsIssued());

        stockMovementViewModel.existingLotMovementViewModelList.clear();
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar.getTime()))
                .setQuantity(null)
                .build());
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder()
                .setLotSOH("100")
                .setExpiryDate(DateUtil.formatDateWithoutDay(featureCalendar1.getTime()))
                .setQuantity("10")
                .build());
        assertEquals(NewMovementLotListView.LotStatus.notSoonestToExpireLotsIssued, stockMovementViewModel.getSoonestToExpireLotsIssued());
    }

    @Test
    public void shouldValidateLotChange() throws Exception {
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().setLotSOH("100").setQuantity("50").setHasLotDataChanged(false).build());
        assertFalse(stockMovementViewModel.hasLotDataChanged());

        stockMovementViewModel.newLotMovementViewModelList.add(new LotMovementViewModelBuilder().setLotSOH("100").setQuantity("30").build());
        assertTrue(stockMovementViewModel.hasLotDataChanged());

        stockMovementViewModel.newLotMovementViewModelList.clear();
        stockMovementViewModel.existingLotMovementViewModelList.add(new LotMovementViewModelBuilder().setLotSOH("100").setQuantity("50").setHasLotDataChanged(true).build());
        assertTrue(stockMovementViewModel.hasLotDataChanged());
    }


}
