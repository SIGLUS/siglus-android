package org.openlmis.core.view.viewmodel;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.DraftInventory;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;

@RunWith(LMISTestRunner.class)
public class PhysicalInventoryViewModelTest {

  private PhysicalInventoryViewModel viewModel;
  private PhysicalInventoryViewModel draftViewModel;
  private final String lotNumberOne = "lot1";
  private final String lotNumberTwo = "lot2";
  private final String expiryDate = "Feb 2015";
  private final String formattedProductName = "Primary product name [productCode]";
  private final String formattedProductUnit = "serious Adult";

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
    // given
    prepareNewViewModel();
    PhysicalInventoryViewModel newViewModel = new PhysicalInventoryViewModel(viewModel.getStockCard());
    newViewModel.getExistingLotMovementViewModelList().add(
        new LotMovementViewModelBuilder().setLotNumber(lotNumberOne).setExpiryDate(expiryDate).build());
    newViewModel.getNewLotMovementViewModelList().add(
        new LotMovementViewModelBuilder().setLotNumber(lotNumberTwo).setExpiryDate(expiryDate).build());

    // when
    newViewModel.setDraftInventory(getDraftInventory());

    // then
    assertTrue(newViewModel.isDone());
    assertEquals(lotNumberOne, newViewModel.getExistingLotMovementViewModelList().get(0).getLotNumber());
    assertEquals(lotNumberTwo, newViewModel.getNewLotMovementViewModelList().get(0).getLotNumber());
    assertEquals(formattedProductName, newViewModel.getFormattedProductName());
    assertEquals(formattedProductUnit, newViewModel.getFormattedProductUnit());
    assertEquals(formattedProductName, newViewModel.getGreenName().toString());
    assertEquals(formattedProductUnit, newViewModel.getGreenUnit().toString());
    assertFalse(newViewModel.validateNewLotList());
    assertFalse(newViewModel.validate());
    assertTrue(newViewModel.isDataChanged());
  }

  @Test
  public void shouldSuccessWithOutDraftInventory() {
    prepareNewViewModel();

    assertTrue(viewModel.isDone());
    assertEquals(lotNumberOne, viewModel.getExistingLotMovementViewModelList().get(0).getLotNumber());
    assertEquals(lotNumberTwo, viewModel.getNewLotMovementViewModelList().get(0).getLotNumber());
    assertEquals(formattedProductName, viewModel.getFormattedProductName());
    assertEquals(formattedProductUnit, viewModel.getFormattedProductUnit());
    assertEquals(formattedProductName, viewModel.getGreenName().toString());
    assertEquals(formattedProductUnit, viewModel.getGreenUnit().toString());

    assertFalse(viewModel.validateNewLotList());
    assertFalse(viewModel.validate());
    assertTrue(viewModel.isDataChanged());
    assertNull(viewModel.getDraftInventory());
    assertNull(viewModel.getFrom());

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
    assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getLotNumber(), is(lotNumberOne));
    assertThat(draftInventory.getDraftLotItemListWrapper().get(0).getQuantity(), is(100L));
    assertThat(draftInventory.getDraftLotItemListWrapper().get(1).isNewAdded(), is(true));
    assertThat(draftInventory.getDraftLotItemListWrapper().get(1).getLotNumber(), is(lotNumberTwo));
    return draftInventory;
  }

  private void prepareNewViewModel() {
    LotMovementViewModel lotMovementViewModel1 = new LotMovementViewModelBuilder()
        .setLotNumber(lotNumberOne).setQuantity("100").setExpiryDate(expiryDate).build();
    LotMovementViewModel lotMovementViewModel2 = new LotMovementViewModelBuilder()
        .setLotNumber(lotNumberTwo).setExpiryDate(expiryDate).build();

    viewModel.setExistingLotMovementViewModelList(newArrayList(lotMovementViewModel1));
    viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel2));
    viewModel.setDone(true);
  }
}