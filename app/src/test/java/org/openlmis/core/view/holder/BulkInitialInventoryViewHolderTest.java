package org.openlmis.core.view.holder;

import android.view.LayoutInflater;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(LMISTestRunner.class)
public class BulkInitialInventoryViewHolderTest {

    private InventoryViewModel viewModel;
    private BulkInitialInventoryViewHolder bulkInitialInventoryViewHolder;

    @Before
    public void setup(){
        viewModel = mock(InventoryViewModel.class);

        View itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_bulk_initial_inventory, null, false);
        bulkInitialInventoryViewHolder = new BulkInitialInventoryViewHolder(itemView);
    }

    @Test
    public void shouldAddAndDeleteLotFromNewLots() {
        List<LotMovementViewModel> lotsMovementViewModel = new ArrayList<LotMovementViewModel>();
        when(viewModel.getNewLotMovementViewModelList()).thenReturn(lotsMovementViewModel);
        bulkInitialInventoryViewHolder.populate(viewModel, "");
        LotMovementViewModel lot = new LotMovementViewModel();

        bulkInitialInventoryViewHolder.addNewLot(lot);
        bulkInitialInventoryViewHolder.removeLot(lot);

        assertTrue(bulkInitialInventoryViewHolder.getViewModel().getNewLotMovementViewModelList().isEmpty());
    }
}