package org.openlmis.core.view.holder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.view.LayoutInflater;
import android.view.View;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Lot;
import org.openlmis.core.model.LotOnHand;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.service.StockService;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.robolectric.RuntimeEnvironment;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class StockCardViewHolderTest {

  private StockCardViewHolder viewHolder;
  private StockCardViewHolder.OnItemViewClickListener mockedListener;
  private StockService stockService;
  protected StockCard stockCard;

  @Before
  public void setUp() {
    mockedListener = mock(StockCardViewHolder.OnItemViewClickListener.class);
    View view = LayoutInflater.from(RuntimeEnvironment.application)
        .inflate(R.layout.item_stockcard, null, false);

    viewHolder = new StockCardViewHolder(view, mockedListener);

    stockService = mock(StockService.class);
    viewHolder.stockService = stockService;

    stockCard = new StockCard();
    final Product product = new Product();
    product.setPrimaryName("product");
    stockCard.setProduct(product);
  }

  @Test
  public void shouldShowIconWhenExpireDateInCurrentMonth() throws Exception {
    StockCard stockCard = StockCardBuilder.buildStockCard();
    Lot lot = new Lot();
    lot.setExpirationDate(DateUtil.parseString("2016-02-16", DateUtil.DB_DATE_FORMAT));
    LotOnHand lotOnHand = new LotOnHand(lot, stockCard, 100L);
    stockCard.getLotOnHandListWrapper().add(lotOnHand);
    InventoryViewModel inventoryViewModel = new InventoryViewModel(stockCard);
    Date mockCurrentDate = DateUtil.parseString("15/02/2016", DateUtil.SIMPLE_DATE_FORMAT);
    LMISTestApp.getInstance().setCurrentTimeMillis(mockCurrentDate.getTime());

    viewHolder.populate(inventoryViewModel, "");

    assertThat(viewHolder.lyExpiryDateWarning.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void shouldShowIconWhenExpireDateBeforeCurrentTime() throws Exception {
    StockCard stockCard = StockCardBuilder.buildStockCard();
    Lot lot = new Lot();
    lot.setExpirationDate(DateUtil.parseString("2016-01-14", DateUtil.DB_DATE_FORMAT));
    LotOnHand lotOnHand = new LotOnHand(lot, stockCard, 100L);
    stockCard.getLotOnHandListWrapper().add(lotOnHand);
    InventoryViewModel inventoryViewModel = new InventoryViewModel(stockCard);
    Date mockCurrentDate = DateUtil.parseString("16/02/2016", DateUtil.SIMPLE_DATE_FORMAT);
    LMISTestApp.getInstance().setCurrentTimeMillis(mockCurrentDate.getTime());

    viewHolder.populate(inventoryViewModel, "");

    assertThat(viewHolder.lyExpiryDateWarning.getVisibility()).isEqualTo(View.VISIBLE);
  }

  @Test
  public void shouldHideIconWhenExpireDateAfterThreeMonthsWithCurrentTime() throws Exception {
    StockCard stockCard = StockCardBuilder.buildStockCard();
    Lot lot = new Lot();
    lot.setExpirationDate(DateUtil.parseString("2016-06-17", DateUtil.DB_DATE_FORMAT));
    LotOnHand lotOnHand = new LotOnHand(lot, stockCard, 100L);
    stockCard.getLotOnHandListWrapper().add(lotOnHand);
    InventoryViewModel inventoryViewModel = new InventoryViewModel(stockCard);
    Date mockCurrentDate = DateUtil.parseString("16/02/2016", DateUtil.SIMPLE_DATE_FORMAT);
    LMISTestApp.getInstance().setCurrentTimeMillis(mockCurrentDate.getTime());

    viewHolder.populate(inventoryViewModel, "");

    assertThat(viewHolder.lyExpiryDateWarning.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldHideExpiryNotifyIconWhenExpiryDateIsEmpty() {
    Date mockCurrentDate = DateUtil.parseString("16/02/2016", DateUtil.SIMPLE_DATE_FORMAT);
    LMISTestApp.getInstance().setCurrentTimeMillis(mockCurrentDate.getTime());
    StockCard stockCard = StockCardBuilder.buildStockCard();

    // The expiry date icon shows in the previous view
    InventoryViewModel inventoryViewModel = new InventoryViewModel(stockCard);
    viewHolder.populate(inventoryViewModel, "");

    // Reuse the view with the empty expiry dates
    InventoryViewModel secondInventoryViewModel = new InventoryViewModel(stockCard);
    viewHolder.populate(secondInventoryViewModel, "");

    assertThat(viewHolder.lyExpiryDateWarning.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldNotShowExpiryDateWarningWhenStockOnHandIsZero() {
    Date mockCurrentDate = DateUtil.parseString("16/02/2016", DateUtil.SIMPLE_DATE_FORMAT);
    LMISTestApp.getInstance().setCurrentTimeMillis(mockCurrentDate.getTime());
    StockCard stockCard = StockCardBuilder.buildStockCard();
    stockCard.setStockOnHand(0);
    InventoryViewModel inventoryViewModel = new InventoryViewModel(stockCard);

    viewHolder.inflateData(inventoryViewModel, "");

    assertThat(viewHolder.lyExpiryDateWarning.getVisibility()).isEqualTo(View.GONE);
  }
}