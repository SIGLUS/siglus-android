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
import org.openlmis.core.utils.DateUtil;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class StockMovementViewModelTest extends LMISRepositoryUnitTest{


    private StockMovementViewModel stockMovementViewModel;

    @Before
    public void setup() {
        stockMovementViewModel = new StockMovementViewModel();
    }

    @Test
    public void shouldReturnValidWhenStockMovementViewModelHasAllData() {
        stockMovementViewModel.setMovementDate(DateUtil.formatDate(new Date()));
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason("abc");
        stockMovementViewModel.setReceived("100");
        assertTrue(stockMovementViewModel.validateInputValid());
    }

    @Test
    public void shouldReturnFalseIfMovementDateIsMissing() {
        stockMovementViewModel.setStockExistence("123");
        stockMovementViewModel.setDocumentNo("111");
        stockMovementViewModel.setReason("abc");
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
        stockMovementViewModel.setReason("abc");
        stockMovementViewModel.setMovementDate("2016-11-20");
        assertFalse(stockMovementViewModel.validateEmpty());
    }

}
