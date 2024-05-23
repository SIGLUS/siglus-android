package org.openlmis.core.view.holder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.adapter.ExpiredStockCardListLotAdapter;
import org.openlmis.core.view.adapter.StockcardListLotAdapter;
import org.openlmis.core.view.adapter.StockcardListLotAdapter.LotInfoHolder.OnItemSelectListener;
import org.openlmis.core.view.viewmodel.InventoryViewModel;

@RunWith(LMISTestRunner.class)
public class ExpiredStockCardListViewHolderTest {
    private ExpiredStockCardListViewHolder viewHolder;

    @Before
    public void setUp() throws Exception {
        viewHolder = new ExpiredStockCardListViewHolder(new View(ApplicationProvider.getApplicationContext()), null);
    }

    @Test
    public void shouldSetStockOnHandAndProductNameAndWarningWhenInflateDataIsCalled() {
        // given
        InventoryViewModel mockedInventoryViewModel = mock(InventoryViewModel.class);
        when(mockedInventoryViewModel.getStockOnHand()).thenReturn(123L);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        when(mockedInventoryViewModel.getProductStyledName()).thenReturn(spannableStringBuilder);

        when(mockedInventoryViewModel.getStockCard()).thenReturn(mock(StockCard.class));

        TextView mockedTvStockOnHand = mock(TextView.class);
        String stockOnHand = "123";
        doNothing().when(mockedTvStockOnHand).setText(stockOnHand);
        viewHolder.tvStockOnHand = mockedTvStockOnHand;

        TextView mockedTvProductName = mock(TextView.class);
        viewHolder.tvProductName = mockedTvProductName;
        doNothing().when(mockedTvStockOnHand).setText(any(SpannableStringBuilder.class));

        TextView mockedTvStockStatus = mock(TextView.class);
        viewHolder.tvStockStatus = mockedTvStockStatus;
        doNothing().when(mockedTvStockStatus).setText(anyString());
        doNothing().when(mockedTvStockStatus).setBackgroundColor(anyInt());
        // when
        viewHolder.inflateData(mockedInventoryViewModel, "");
        // then
        verify(mockedTvStockOnHand).setText(stockOnHand);
        verify(mockedTvProductName).setText(any(SpannableStringBuilder.class));
        verify(mockedTvStockStatus).setText(anyString());
        verify(mockedTvStockStatus).setBackgroundColor(anyInt());
    }

  @Test
  public void shouldReturnExpiredStockCardListLotAdapterWhenCreateStockCardListAdapterIsCalled() {
    // when
    StockcardListLotAdapter actualAdapter = viewHolder.createStockCardListAdapter(
        newArrayList(mock(LotOnHand.class), null));
    // then
    assertTrue(actualAdapter instanceof ExpiredStockCardListLotAdapter);
  }

  @Test
  public void shouldSetListenerWhenCallConstructorMethodWithListener() {
    // when
    OnItemSelectListener mockedOnItemSelectListener = mock(OnItemSelectListener.class);
    ExpiredStockCardListViewHolder viewHolder = new ExpiredStockCardListViewHolder(
        new View(ApplicationProvider.getApplicationContext()), mockedOnItemSelectListener);
    // then
    assertEquals(mockedOnItemSelectListener, viewHolder.onItemSelectListener);
  }
}