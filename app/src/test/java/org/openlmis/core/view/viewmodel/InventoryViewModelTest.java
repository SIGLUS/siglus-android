package org.openlmis.core.view.viewmodel;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.view.holder.StockCardViewHolder;

import static org.hamcrest.Matchers.is;
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

        assertThat(inventoryViewModel.getStockCard().getId(),is(1L));
    }

    @Test
    public void shouldReturnTrueWhenProductIsArchivedAndNotQuantityIsEmpty() throws Exception {
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
    public void shouldGetNormalLevelWhenSOHGreaterThanAvg() throws LMISException {
        viewModel.setCmm(80);
        viewModel.setStockOnHand(100);

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_NORMAL);

    }

    @Test
    public void shouldGetNormalLevelWhenAvgMonthlyConsumptionLessThanZero() throws LMISException {
        viewModel.setCmm(-1);
        viewModel.setStockOnHand(100);

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_NORMAL);

    }

    @Test
    public void shouldGetOverLevelWhenSOHSmallerThanAvg() throws LMISException {
        viewModel.setCmm(10);

        viewModel.setStockOnHand(30);

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_OVER_STOCK);
    }

    @Test
    public void shouldGetLowLevelWhenSOHSmallerThanAvg() throws LMISException {
        viewModel.setCmm(100);
        viewModel.setLowStockAvg(5);
        viewModel.setStockOnHand(2);

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_LOW_STOCK);
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHIsZero() throws LMISException {
        viewModel.setCmm(80);

        viewModel.setStockOnHand(0);

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_STOCK_OUT);
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHIsZeroEvenAvgMonthlyConsumptionLessThanZero() throws LMISException {
        viewModel.setCmm(-1);

        viewModel.setStockOnHand(0);

        int stockOnHandLevel = viewModel.getStockOnHandLevel();

        Assertions.assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_STOCK_OUT);
    }
}