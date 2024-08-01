package org.openlmis.core.view.holder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import android.view.LayoutInflater;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.view.holder.StockCardViewHolder.OnItemViewClickListener;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import androidx.test.core.app.ApplicationProvider;

@RunWith(LMISTestRunner.class)
public class KitStockCardViewHolderTest {

  private KitStockCardViewHolder viewHolder;

  @Before
  public void setUp() throws Exception {
    View view = LayoutInflater.from(ApplicationProvider.getApplicationContext())
        .inflate(R.layout.item_stockcard, null, false);
    viewHolder = new KitStockCardViewHolder(view, mock(OnItemViewClickListener.class));
  }

  @Test
  public void shouldShowKitInfo() throws Exception {
    StockCard stockCard = new StockCard();
    stockCard.setStockOnHand(200);
    final Product product = new Product();
    product.setPrimaryName("product");
    stockCard.setProduct(product);

    viewHolder.populate(new InventoryViewModel(stockCard), "");

    assertThat(viewHolder.kitTvProductName.getText().toString()).hasToString("product");
    assertThat(viewHolder.kitTvStockOnHand.getText().toString()).hasToString("200");

  }
}