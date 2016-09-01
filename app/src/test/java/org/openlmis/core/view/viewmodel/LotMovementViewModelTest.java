package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LotMovementViewModelTest {
    private LotMovementViewModel viewModel = new LotMovementViewModel();

    @Test
    public void shouldReturnTrueWhenViewModelIsValid() throws Exception {
        viewModel.setLotNumber("ei-233");
        viewModel.setExpiryDate("2013-09-01");
        viewModel.setQuantity("233");

        assertTrue(viewModel.isValid());
    }

    @Test
    public void shouldReturnFalseWhenViewModelIsInvalid() throws Exception {
        viewModel.validate();
        assertFalse(viewModel.isValid());

        viewModel.setLotNumber("dk-23");
        viewModel.validate();
        assertFalse(viewModel.isValid());

        viewModel.setQuantity("333");
        viewModel.validate();
        assertFalse(viewModel.isValid());

        viewModel.setExpiryDate("2014-03-18");
        viewModel.validate();
        assertTrue(viewModel.isValid());
    }

    @Test
    public void shouldReturnFalseWhenIssueQuantityGreaterThanSOH() throws Exception {
        viewModel.setLotSoh("100");
        viewModel.setQuantity("300");
        viewModel.validate();
        assertFalse(viewModel.validateQuantity(MovementReasonManager.MovementType.ISSUE));
    }

    @Test
    public void shouldConvertLotMovementItemWithRightExpiryDate() throws Exception {
        viewModel.setQuantity("10");
        viewModel.setLotNumber("lot1");
        viewModel.setExpiryDate("Feb 2015");

        LotMovementItem lotMovementItem = viewModel.convertViewToModel(new Product());
        assertEquals(new GregorianCalendar(2015, 1, 28).getTime(), lotMovementItem.getLot().getExpirationDate());
    }

    @Test
    public void shouldConvertLotMovementItemWithRightStockOnHand() throws Exception {
        viewModel.setQuantity("10");
        viewModel.setLotNumber("lot1");
        viewModel.setExpiryDate("Feb 2015");

        LotMovementItem lotMovementItem = viewModel.convertViewToModelAndResetSOH(new Product());
        assertEquals(Long.parseLong(viewModel.getLotSoh()), lotMovementItem.getStockOnHand());
    }
}