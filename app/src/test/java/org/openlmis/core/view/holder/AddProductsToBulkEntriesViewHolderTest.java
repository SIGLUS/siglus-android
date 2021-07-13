package org.openlmis.core.view.holder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.view.LayoutInflater;
import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.viewmodel.ProductsToBulkEntriesViewModel;
import org.robolectric.RuntimeEnvironment;

@RunWith(LMISTestRunner.class)
public class AddProductsToBulkEntriesViewHolderTest {

  private final String queryKeyWord = null;
  private AddProductsToBulkEntriesViewHolder viewHolder;
  private Product product;

  @Before
  public void setUp() {
    View itemView = LayoutInflater.from(RuntimeEnvironment.application)
        .inflate(R.layout.item_add_product_bulk_entries, null, false);
    viewHolder = new AddProductsToBulkEntriesViewHolder(itemView);
    product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build();
  }

  @Test
  public void shouldInitialViewHolder() {
    // given
    ProductsToBulkEntriesViewModel viewModel = new ProductsToBulkEntriesViewModel(product);
    // when
    viewHolder.populate(viewModel, queryKeyWord);
    // then
    assertEquals("Lamivudina 150mg [08S40]", viewHolder.productName.getText().toString());
  }

  @Test
  public void shouldSetCheckBoxListener() {
    // given
    ProductsToBulkEntriesViewModel viewModel = new ProductsToBulkEntriesViewModel(product);
    // when
    viewHolder.putOnChangedListener(viewModel);
    viewHolder.checkBox.performClick();
    // then
    assertTrue(viewModel.isChecked());
  }
}