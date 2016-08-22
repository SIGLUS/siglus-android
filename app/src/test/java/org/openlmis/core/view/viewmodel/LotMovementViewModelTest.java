package org.openlmis.core.view.viewmodel;

import org.apache.tools.ant.taskdefs.Move;
import org.junit.Test;
import org.openlmis.core.manager.MovementReasonManager;

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

        viewModel.validate();
        assertTrue(viewModel.isValid());
    }

    @Test
    public void shouldReturnFalseWhenIssueQuantityGreaterThanSOH() throws Exception {
        viewModel.setExpiryDate("2014-03-18");
        viewModel.setLotSoh("100");
        viewModel.setQuantity("300");
        viewModel.validate();
        assertFalse(viewModel.isQuantityGreaterThanSOH(MovementReasonManager.MovementType.ISSUE));
    }
}