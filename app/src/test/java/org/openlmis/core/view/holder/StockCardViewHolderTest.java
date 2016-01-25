package org.openlmis.core.view.holder;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockCardViewHolderTest {

    private StockCardViewHolder viewHolder;
    private StockCardViewHolder.OnItemViewClickListener mockedListener;
    private StockRepository stockRepository;
    protected StockCard stockCard;

    @Before
    public void setUp() {
        mockedListener = mock(StockCardViewHolder.OnItemViewClickListener.class);
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_stockcard, null, false);

        viewHolder = new StockCardViewHolder(view, mockedListener);

        stockRepository = mock(StockRepository.class);
        viewHolder.stockRepository = stockRepository;

        stockCard = new StockCard();
        final Product product = new Product();
        product.setPrimaryName("product");
        stockCard.setProduct(product);
    }

    @Test
    public void shouldGetNormalLevelWhenSOHGreaterThanAvg() throws LMISException {
        when(stockRepository.getLowStockAvg(any(StockCard.class))).thenReturn(80);

        stockCard.setStockOnHand(100);

        int stockOnHandLevel = viewHolder.getStockOnHandLevel(new InventoryViewModel(stockCard));

        assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_NORMAL);

    }

    @Test
    public void shouldGetLowLevelWhenSOHSmallerThanAvg() throws LMISException {
        when(stockRepository.getLowStockAvg(any(StockCard.class))).thenReturn(100);

        stockCard.setStockOnHand(2);

        int stockOnHandLevel = viewHolder.getStockOnHandLevel(new InventoryViewModel(stockCard));

        assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_LOW_STOCK);
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHIsZero() throws LMISException {
        when(stockRepository.getLowStockAvg(any(StockCard.class))).thenReturn(80);

        stockCard.setStockOnHand(0);

        int stockOnHandLevel = viewHolder.getStockOnHandLevel(new InventoryViewModel(stockCard));

        assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_STOCK_OUT);
    }

    @NonNull
    private RnrFormItem getRnrFormItem(long issued) {
        RnrFormItem rnrFormItem = new RnrFormItem();
        rnrFormItem.setInventory(10);
        rnrFormItem.setIssued(issued);
        return rnrFormItem;
    }
}