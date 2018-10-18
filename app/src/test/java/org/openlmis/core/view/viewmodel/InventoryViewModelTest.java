package org.openlmis.core.view.viewmodel;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.view.holder.StockCardViewHolder;

import static org.assertj.core.util.Lists.newArrayList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(LMISTestRunner.class)
public class InventoryViewModelTest {
    private InventoryViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        StockCard stockCard = StockCardBuilder.buildStockCard();
        stockCard.setId(1);
        stockCard.setAvgMonthlyConsumption(1);

        viewModel = new InventoryViewModel(stockCard);
    }

    @Test
    public void shouldBuildEmergencyModel() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);
        stockCard.setProduct(ProductBuilder.buildAdultProduct());

        InventoryViewModel inventoryViewModel = InventoryViewModel.buildEmergencyModel(stockCard);

        assertThat(inventoryViewModel.getStockCard().getId(), is(1L));
    }

    @Test
    public void shouldValidateReturnTrueWhenProductIsArchived() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);

        Product product = ProductBuilder.buildAdultProduct();
        product.setArchived(true);
        stockCard.setProduct(product);

        InventoryViewModel inventoryViewModel = InventoryViewModel.buildEmergencyModel(stockCard);
        inventoryViewModel.setChecked(true);

        assertTrue(inventoryViewModel.validate());
    }

    @Test
    public void shouldValidate() throws Exception {
        LotMovementViewModel lotMovementViewModel = new LotMovementViewModel("lot1", "Feb 2017", MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        lotMovementViewModel.setQuantity("10");
        viewModel.setNewLotMovementViewModelList(newArrayList(lotMovementViewModel));
        assertTrue(viewModel.validate());

        lotMovementViewModel.setQuantity("number");
        assertFalse(viewModel.validate());
    }

    @Test
    public void shouldGetNormalLevelWhenSOHGreaterThanAvg() throws LMISException {
        StockCard stockCard = new StockCard();
        Product product = new Product();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(100);
        stockCard.setAvgMonthlyConsumption(80);
        viewModel.setStockCard(stockCard);
        viewModel.setStockOnHand(100);

        StockCardViewHolder.StockOnHandStatus stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.StockOnHandStatus.REGULAR_STOCK);
    }

    @Test
    public void shouldGetNormalLevelWhenAvgMonthlyConsumptionLessThanZero() throws LMISException {
        StockCard stockCard = new StockCard();
        Product product = new Product();
        stockCard.setProduct(product);
        stockCard.setAvgMonthlyConsumption(-1);
        viewModel.setStockCard(stockCard);
        viewModel.setStockOnHand(100);

        StockCardViewHolder.StockOnHandStatus stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.StockOnHandStatus.REGULAR_STOCK);

    }

    @Test
    public void shouldGetOverLevelWhenSOHSmallerThanAvg() throws LMISException {
        StockCard stockCard = new StockCard();
        Product product = new Product();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(30);
        stockCard.setAvgMonthlyConsumption(10);
        viewModel.setStockCard(stockCard);

        viewModel.setStockOnHand(30);

        StockCardViewHolder.StockOnHandStatus stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.StockOnHandStatus.OVER_STOCK);
    }

    @Test
    public void shouldGetLowLevelWhenSOHSmallerThanAvg() throws LMISException {
        StockCard stockCard = new StockCard();
        Product product = new Product();
        stockCard.setProduct(product);
        stockCard.setStockOnHand(2);
        stockCard.setAvgMonthlyConsumption(100);
        viewModel.setStockCard(stockCard);
        viewModel.setStockOnHand(2);

        StockCardViewHolder.StockOnHandStatus stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.StockOnHandStatus.LOW_STOCK);
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHIsZero() throws LMISException {
        StockCard stockCard = new StockCard();
        Product product = new Product();
        stockCard.setProduct(product);
        stockCard.setAvgMonthlyConsumption(80);
        viewModel.setStockCard(stockCard);

        viewModel.setStockOnHand(0);

        StockCardViewHolder.StockOnHandStatus stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.StockOnHandStatus.STOCK_OUT);
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHIsZeroEvenAvgMonthlyConsumptionLessThanZero() throws LMISException {
        StockCard stockCard = new StockCard();
        Product product = new Product();
        stockCard.setProduct(product);
        stockCard.setAvgMonthlyConsumption(-1);
        viewModel.setStockCard(stockCard);

        viewModel.setStockOnHand(0);

        StockCardViewHolder.StockOnHandStatus stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.StockOnHandStatus.STOCK_OUT);
    }

    @Test
    public void shouldReturnFalseWhenLotListIsInvalidate() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);

        Product product = ProductBuilder.buildAdultProduct();
        product.setArchived(false);
        stockCard.setProduct(product);

        InventoryViewModel inventoryViewModel = InventoryViewModel.buildEmergencyModel(stockCard);
        inventoryViewModel.setChecked(true);

        LotMovementViewModel lotMovementViewModel = new LotMovementViewModel("lotNumber", "2012-09-01", MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        lotMovementViewModel.validateLotWithPositiveQuantity();
        inventoryViewModel.newLotMovementViewModelList.add(lotMovementViewModel);

        assertFalse(inventoryViewModel.validate());
    }

    @Test
    public void shouldReturnTrueWhenLotListIsValid() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setId(1);

        Product product = ProductBuilder.buildAdultProduct();
        product.setArchived(false);
        stockCard.setProduct(product);

        InventoryViewModel inventoryViewModel = InventoryViewModel.buildEmergencyModel(stockCard);
        inventoryViewModel.setChecked(true);

        LotMovementViewModel lotMovementViewModel = new LotMovementViewModel("lotNumber", "2012-09-01", MovementReasonManager.MovementType.PHYSICAL_INVENTORY);
        lotMovementViewModel.setQuantity("21");
        inventoryViewModel.newLotMovementViewModelList.add(lotMovementViewModel);

        assertTrue(inventoryViewModel.validate());
    }
}