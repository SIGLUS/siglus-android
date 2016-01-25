package org.openlmis.core.view.holder;

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
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(LMISTestRunner.class)
public class KitStockCardViewHolderTest {

    private OnItemViewClickListener listener;
    private KitStockCardViewHolder viewHolder;

    @Before
    public void setUp() throws Exception {
        listener = mock(OnItemViewClickListener.class);

        View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_stockcard, null, false);
        viewHolder = new KitStockCardViewHolder(view, listener);


    }

    @Test
    public void shouldShowKitInfo() throws Exception {
        StockCard stockCard = new StockCard();
        stockCard.setStockOnHand(200);
        final Product product = new Product();
        product.setPrimaryName("product");
        stockCard.setProduct(product);

        viewHolder.populate(new InventoryViewModel(stockCard), "");

        assertThat(viewHolder.tvProductName.getText().toString()).isEqualTo("product");
        assertThat(viewHolder.tvProductUnit.getVisibility()).isEqualTo(View.INVISIBLE);
        assertThat(viewHolder.tvStockOnHand.getText().toString()).isEqualTo("200");

    }
}