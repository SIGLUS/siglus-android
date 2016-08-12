package org.openlmis.core.view.viewmodel;

import org.junit.Test;

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
        assertFalse(viewModel.isValid());

        viewModel.setLotNumber("dk-23");
        assertFalse(viewModel.isValid());

        viewModel.setQuantity("333");
        assertFalse(viewModel.isValid());

        viewModel.setExpiryDate("2014-03-18");
        assertTrue(viewModel.isValid());
    }
}