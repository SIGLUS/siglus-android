package org.openlmis.core.view.viewmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryViewModelTest {

    private PhysicalInventoryViewModel viewModel;
    private PhysicalInventoryViewModel draftViewModel;

    @Before
    public void setUp() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setId(1);

        StockCard draftStockCard = StockCardBuilder.buildStockCard();
        draftStockCard.setId(2);

        viewModel = new PhysicalInventoryViewModel(stockCard);
        draftViewModel = new PhysicalInventoryViewModel(draftStockCard);
    }

    @Test
    public void shouldConvertToAndParseDraftInventory() {
        prepareNewViewModel();

        PhysicalInventoryViewModel newViewModel = new PhysicalInventoryViewModel(viewModel.getStockCard());
        newViewModel.getExistingLotMovementViewModelList().add(new LotMovementViewModelBuilder().setLotNumber("lot1").setExpiryDate("Feb 2015").build());
        newViewModel.setDraftInventory(getDraftInventory());

        assertTrue(newViewModel.isDone());
        assertEquals("lot1", newViewModel.getExistingLotMovementViewModelList().get(0).getLotNumber());
        assertEquals("lot2", newViewModel.getNewLotMovementViewModelList().get(0).getLotNumber());
        assertEquals(newViewModel.getFormattedProductName(), "Primary product name [productCode]");
        assertEquals(newViewModel.getFormattedProductUnit(), "serious Adult");
        assertEquals(newViewModel.getGreenName().toString(), "Primary product name [productCode]");
        assertEquals(newViewModel.getGreenUnit().toString(), "serious Adult");

        assertFalse(newViewModel.validateNewLotList());
        assertFalse(newViewModel.validate());
        assertFalse(newViewModel.isDataChanged());
    }

    @Test
    public void shouldSuccessWithOutDraftInventory() {
        prepareNewViewModel();

        assertTrue(viewModel.isDone());
        assertEquals("lot1", viewModel.getExistingLotMovementViewModelList().get(0).getLotNumber());
        assertEquals("lot2", viewModel.getNewLotMovementViewModelList().get(0).getLotNumber());
        assertEquals(viewModel.getFormattedProductName(), "Primary product name [productCode]");
        assertEquals(viewModel.getFormattedProductUnit(), "serious Adult");
        assertEquals(viewModel.getGreenName().toString(), "Primary product name [productCode]");
        assertEquals(viewModel.getGreenUnit().toString(), "serious Adult");

        assertFalse(viewModel.validateNewLotList());
        assertFalse(viewModel.validate());
        assertTrue(viewModel.isDataChanged());
        assertTrue(viewModel.getDraftInventory() == null);
        assertTrue(viewModel.getFrom() == null);

    }

    @Test
    public void shouldEqualBetweenTwoViewModel() {
        StockCard stockCard1 = StockCardBuilder.buildStockCard();
        Product product = ProductBuilder.create().setProductId(2)
                .setType(Product.MEDICINE_TYPE_ADULT)
                .setCode("productCode")
                .setStrength("serious")
                .setPrimaryName("Primary product name")
                .build();
        stockCard1.setId(1);
        stockCard1.setProduct(product);

        PhysicalInventoryViewModel viewModel1 = new PhysicalInventoryViewModel(stockCard1);
        assertEquals(viewModel, viewModel1);
        assertEquals(viewModel1, draftViewModel);
    }

    private DraftInventory getDraftInventory() {
        DraftInventory draftInventory = new DraftInventory(viewModel);
        assertTrue(draftInventory.isDone());
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).isNewAdded(), is(false));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getLotNumber(), is("lot1"));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getQuantity(), is(100L));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(1).isNewAdded(), is(true));
        assertThat(draftInventory.getDraftLotItemListWrapper().get(1).getLotNumber(), is("lot2"));
        return draftInventory;
    }

    private void prepareNewViewModel() {
        LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder().setLotNumber("lot1").setQuantity("100").setExpiryDate("Feb 2015").build();
        LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder().setLotNumber("lot2").setExpiryDate("Feb 2015").build();

        viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel2));
        viewModel.setDone(true);
    }
}