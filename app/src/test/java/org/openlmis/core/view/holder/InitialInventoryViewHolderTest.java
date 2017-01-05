package org.openlmis.core.view.holder;

import android.view.LayoutInflater;
import android.view.View;

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

import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class InitialInventoryViewHolderTest {

    private InitialInventoryViewHolder viewHolder;
    private String queryKeyWord = null;
    private Product product;
    private ViewHistoryListener mockedListener;

    @Before
    public void setUp() {
        View itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_initial_inventory, null, false);
        viewHolder = new InitialInventoryViewHolder(itemView);
        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").build();
        mockedListener = mock(ViewHistoryListener.class);
    }

    @Test
    public void shouldInitialViewHolder() throws ParseException {
        InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(true)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord, mockedListener);

        assertThat(viewHolder.checkBox.isChecked()).isTrue();
        assertThat(viewHolder.productName.getText().toString()).isEqualTo("Lamivudina 150mg [08S40]");
        assertThat(viewHolder.productUnit.getText().toString()).isEqualTo("Embalagem");

        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(viewHolder.tvHistoryAction.getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void shouldShowEditPanelIfCheckboxIsChecked() {

        InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord, mockedListener);

        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.GONE);

        viewHolder.taCheckbox.performClick();

        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void shouldClearQuantityAndExpiryDate() {
        InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(true)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord, mockedListener);

        viewHolder.taCheckbox.performClick();

        assertThat(viewModel.getQuantity()).isEmpty();
    }

    @Test
    public void shouldUpdateViewModelQuantityWhenInputFinished() {
        InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
                .setChecked(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord, mockedListener);
        viewHolder.itemView.performClick();

        assertThat(viewModel.getQuantity()).isEqualTo("120");
    }

    @Test
    public void shouldShowHistoryViewAndViewItWhenClicked() {
        ViewHistoryListener mockedListener = mock(ViewHistoryListener.class);
        product.setArchived(true);
        InventoryViewModel viewModel = new InventoryViewModelBuilder(product)
                .setQuantity("10")
                .setChecked(false)
                .setType("Embalagem")
                .build();

        viewHolder.populate(viewModel, queryKeyWord, mockedListener);

        assertThat(viewHolder.tvHistoryAction.getVisibility()).isEqualTo(View.VISIBLE);
        assertThat(viewHolder.actionPanel.getVisibility()).isEqualTo(View.GONE);

        viewHolder.tvHistoryAction.performClick();

        verify(mockedListener).viewHistory(viewModel.getStockCard());
    }

}