package org.openlmis.core.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.viewmodel.BulkInitialInventoryViewModel;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;

import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(LMISTestRunner.class)
public class DraftInitialInventoryTest {

    private DraftInitialInventory draftInitialInventory;

    @Test
    public void shouldConvertViewModelToDraft() {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(10);
        Product product = new Product();
        product.setPrimaryName("Product");
        stockCard.setProduct(product);

        BulkInitialInventoryViewModel viewModel = new BulkInitialInventoryViewModel(stockCard);
        draftInitialInventory = new DraftInitialInventory(viewModel);


        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModel();
        lotMovementViewModel1.setQuantity("11");
        lotMovementViewModel1.setExpiryDate("Aug 2016");
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel1));

        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModel();
        lotMovementViewModel2.setQuantity("10");
        lotMovementViewModel2.setExpiryDate("Aug 2016");
        viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel2));

        draftInitialInventory = new DraftInitialInventory(viewModel);

        assertThat(draftInitialInventory.getQuantity(), is(21L));
        assertThat(draftInitialInventory.getDraftLotItemListWrapper().size(),is(2));
        assertThat(draftInitialInventory.getDraftLotItemListWrapper().get(1).getQuantity(),is(11L));
    }
}
