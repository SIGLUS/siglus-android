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
import org.openlmis.core.model.repository.RnrFormItemRepository;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class StockCardViewHolderTest {

    private StockCardViewHolder viewHolder;
    private StockCardViewHolder.OnItemViewClickListener mockedListener;
    private RnrFormItemRepository rnrFormItemRepositoryMock;

    @Before
    public void setUp() {
        mockedListener = mock(StockCardViewHolder.OnItemViewClickListener.class);
        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_stockcard, null, false);

        viewHolder = new StockCardViewHolder(view, mockedListener);

        rnrFormItemRepositoryMock = mock(RnrFormItemRepository.class);
        viewHolder.rnrFormItemRepository = rnrFormItemRepositoryMock;
    }

    @Test
    public void shouldGetNormalLevelWhenSOHGreaterThanAvg() throws LMISException {
        when(rnrFormItemRepositoryMock.getLowStockAvg(any(Product.class))).thenReturn(80);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(100);

        int stockOnHandLevel = viewHolder.getStockOnHandLevel(stockCard);

        assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_NORMAL);

    }

    @Test
    public void shouldGetLowLevelWhenSOHSmallerThanAvg() throws LMISException {
        when(rnrFormItemRepositoryMock.getLowStockAvg(any(Product.class))).thenReturn(100);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(2);

        int stockOnHandLevel = viewHolder.getStockOnHandLevel(stockCard);

        assertThat(stockOnHandLevel).isEqualTo(StockCardViewHolder.STOCK_ON_HAND_LOW_STOCK);
    }

    @Test
    public void shouldGetStockOutLevelWhenSOHIsZero() throws LMISException {
        when(rnrFormItemRepositoryMock.getLowStockAvg(any(Product.class))).thenReturn(80);

        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(0);

        int stockOnHandLevel = viewHolder.getStockOnHandLevel(stockCard);

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