package org.openlmis.core.view.viewmodel;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.DraftInitialInventory;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class BulkInitialInventoryViewModelTest {
    private BulkInitialInventoryViewModel viewModel;
    @Before
    public void setUp() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setId(1);
        viewModel = new BulkInitialInventoryViewModel(stockCard);
    }

    @Test
    public void shouldConvertToAndParseDraftInventory() {
        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("lot1").setQuantity("100").setExpiryDate("Feb 2015").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("lot2").setExpiryDate("Feb 2015").build();

        viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel2));
        viewModel.setDone(true);

        DraftInitialInventory draftInventory = new DraftInitialInventory(viewModel);
        assertTrue(draftInventory.isDone());
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getLotNumber(), is("lot1"));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getQuantity(), is(100L));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(1).getQuantity(), is(nullValue()));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(1).getLotNumber(), is("lot2"));

        BulkInitialInventoryViewModel newViewModel = new BulkInitialInventoryViewModel(viewModel.getStockCard());
        newViewModel.getExistingLotMovementViewModelList().add(new LotMovementViewModelBuilder().setLotNumber("lot1").setExpiryDate("Feb 2015").build());
        newViewModel.getNewLotMovementViewModelList().add(new LotMovementViewModelBuilder().setLotNumber("lot2").setExpiryDate("Feb 2015").build());
        newViewModel.setDraftInventory(draftInventory);

        assertThat(newViewModel.isDone(), is(false));
        assertEquals("lot1", newViewModel.getExistingLotMovementViewModelList().get(0).getLotNumber());
        assertEquals("lot2", newViewModel.getNewLotMovementViewModelList().get(0).getLotNumber());
    }
    @Test
    public void shouldReturnTrueWhenDataChange(){
        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("lot1").setQuantity("100").setExpiryDate("Feb 2015").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("lot2").setExpiryDate("Feb 2015").build();

        viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel2));
        viewModel.setDone(true);

        assertThat(viewModel.getFormattedProductName(),is("Primary product name [productCode]"));
        assertThat(viewModel.getFormattedProductUnit(),is("serious Adult"));
        assertTrue(viewModel.isDataChanged());

    }
}
