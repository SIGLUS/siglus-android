package org.openlmis.core.view.holder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.view.LayoutInflater;
import android.view.View;
import java.text.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.holder.InitialInventoryViewHolder.ViewHistoryListener;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.InventoryViewModelBuilder;
import org.robolectric.RuntimeEnvironment;

@RunWith(LMISTestRunner.class)
public class InitialInventoryViewHolderTest {

  private InitialInventoryViewHolder viewHolder;
  private final String queryKeyWord = null;
  private Product product;
  private ViewHistoryListener mockedListener;

  @Before
  public void setUp() {
    View itemView = LayoutInflater.from(RuntimeEnvironment.application)
        .inflate(R.layout.item_initial_inventory, null, false);
    viewHolder = new InitialInventoryViewHolder(itemView);
    product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build();
    mockedListener = mock(ViewHistoryListener.class);
  }

  @Test
  public void shouldInitialViewHolder() throws ParseException {
    InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
        .setChecked(false)
        .setType("Embalagem")
        .build();

    viewHolder.populate(viewModel, queryKeyWord, mockedListener);

    assertThat(viewHolder.productName.getText().toString()).isEqualTo("Lamivudina 150mg [08S40]");
    assertThat(viewHolder.productUnit.getText().toString()).isEqualTo("Embalagem");

    assertThat(viewHolder.tvHistoryAction.getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldShowHistoryViewAndViewItWhenClicked() {
    ViewHistoryListener mockedListener = mock(ViewHistoryListener.class);
    product.setArchived(true);
    InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
        .setChecked(false)
        .setType("Embalagem")
        .build();

    viewHolder.populate(viewModel, queryKeyWord, mockedListener);

    assertThat(viewHolder.tvHistoryAction.getVisibility()).isEqualTo(View.VISIBLE);

    viewHolder.tvHistoryAction.performClick();

    verify(mockedListener).viewHistory(viewModel.getStockCard());
  }
}