package org.openlmis.core.model;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.view.viewmodel.LotMovementViewModel;
import org.openlmis.core.view.viewmodel.PhysicalInventoryViewModel;

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
    PhysicalInventoryViewModel viewModel = new PhysicalInventoryViewModel(stockCard);

    draftInventory = new DraftInventory(viewModel);

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
    assertThat(draftInventory.getDraftLotItemListWrapper().size(), is(2));
    assertTrue(draftInventory.getDraftLotItemListWrapper().get(1).isNewAdded());
    assertFalse(draftInventory.getDraftLotItemListWrapper().get(0).isNewAdded());
  }
}