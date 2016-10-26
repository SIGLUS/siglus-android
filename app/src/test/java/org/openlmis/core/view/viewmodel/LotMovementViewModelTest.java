package org.openlmis.core.view.viewmodel;

import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.LotMovementItem;
import org.openlmis.core.model.Product;

import java.util.GregorianCalendar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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
        viewModel.validateLotWithPositiveAmount();
        assertFalse(viewModel.isValid());

        viewModel.setLotNumber("dk-23");
        viewModel.validateLotWithPositiveAmount();
        assertFalse(viewModel.isValid());

        viewModel.setQuantity("333");
        viewModel.validateLotWithPositiveAmount();
        assertFalse(viewModel.isValid());

        viewModel.setExpiryDate("2014-03-18");
        viewModel.validateLotWithPositiveAmount();
        assertTrue(viewModel.isValid());
    }

    @Test
    public void shouldReturnFalseWhenIssueQuantityGreaterThanSOH() throws Exception {
        viewModel.setLotSoh("100");
        viewModel.setQuantity("300");
        viewModel.validateLotWithPositiveAmount();
        assertFalse(viewModel.validateQuantityNotGreaterThanSOH(MovementReasonManager.MovementType.ISSUE));
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
        viewModel.setLotSoh("20");

        LotMovementItem lotMovementItem = viewModel.convertViewToModelAndResetSOH(new Product());
        assertThat(lotMovementItem.getStockOnHand(), is(10L));
    }
}