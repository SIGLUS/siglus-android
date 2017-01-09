package org.openlmis.core.view.viewmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryViewModelTest {

    private PhysicalInventoryViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setId(1);

        viewModel = new PhysicalInventoryViewModel(stockCard);
    }

    @Test
    public void shouldConvertToAndParseDraftInventory() {
        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("lot1").setQuantity("100").setExpiryDate("Feb 2015").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("lot2").setExpiryDate("Feb 2015").build();

        viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel2));
        viewModel.setDone(true);

        DraftInventory draftInventory = new DraftInventory(viewModel);
        assertTrue(draftInventory.isDone());
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).isNewAdded(), is(false));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getLotNumber(), is("lot1"));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getQuantity(), is(100L));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(1).isNewAdded(), is(true));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(1).getLotNumber(), is("lot2"));

        PhysicalInventoryViewModel newViewModel = new PhysicalInventoryViewModel(viewModel.getStockCard());
        newViewModel.getExistingLotMovementViewModelList().add(new LotMovementViewModelBuilder().setLotNumber("lot1").setExpiryDate("Feb 2015").build());
        newViewModel.setDraftInventory(draftInventory);

        assertTrue(newViewModel.isDone());
        assertEquals("lot1", newViewModel.getExistingLotMovementViewModelList().get(0).getLotNumber());
        assertEquals("lot2", newViewModel.getNewLotMovementViewModelList().get(0).getLotNumber());
    }
}