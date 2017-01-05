package org.openlmis.core.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;

import java.util.ArrayList;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class DraftInventoryTest {
    DraftInventory draftInventory;
    @Test
    public void shouldConvertViewModelToDraft() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(10);
        Product product = new Product();
        product.setPrimaryName("Product");
        stockCard.setProduct(product);
        stockCard.setExpireDates("");
        PhysicalInventoryViewModel viewModel = new PhysicalInventoryViewModel(stockCard);
        ArrayList<String> list = new ArrayList<>();
        list.add("18/10/2016");
        list.add("18/10/2017");
        list.add("18/10/2018");
        list.add("18/10/2015");
        viewModel.getExpiryDates().addAll(list);
        viewModel.setQuantity("10");

        draftInventory = new DraftInventory(viewModel);

        assertThat(draftInventory.getQuantity(), is(10L));
        assertThat(draftInventory.getExpireDates(), is("18/10/2015,18/10/2016,18/10/2017,18/10/2018"));

        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModel();
        lotMovementViewModel1.setQuantity("10");
        lotMovementViewModel1.setExpiryDate("Aug 2016");
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel1));

        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModel();
        lotMovementViewModel2.setQuantity("10");
        lotMovementViewModel2.setExpiryDate("Aug 2016");
        viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel2));

        draftInventory = new DraftInventory(viewModel);

        assertThat(draftInventory.getQuantity(), is(20L));
        assertThat(draftInventory.getDraftLotItemListWrapper().size(),is(2));
        assertTrue(draftInventory.getDraftLotItemListWrapper().get(1).isNewAdded());
        assertFalse(draftInventory.getDraftLotItemListWrapper().get(0).isNewAdded());
    }
}