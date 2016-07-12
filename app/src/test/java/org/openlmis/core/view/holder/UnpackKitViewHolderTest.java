package org.openlmis.core.view.holder;

import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.builder.ProductBuilder;
import org.openlmis.core.view.viewmodel.InventoryViewModel;
import org.openlmis.core.view.viewmodel.StockCardViewModelBuilder;
import org.openlmis.core.view.widget.ExpireDateViewGroup;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(LMISTestRunner.class)
public class UnpackKitViewHolderTest {
    private UnpackKitViewHolder viewHolder;
    private Product product;

    @Before
    public void setUp() {
        viewHolder = new UnpackKitViewHolder(LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_physical_inventory, null, false));
        product = new ProductBuilder().setPrimaryName("Lamivudina 150mg").setCode("08S40").setStrength("10mg").setType("VIA").build();
    }

    @Test
    public void shouldGetRightEtQuantity() {
        InventoryViewModel firstViewModel = new StockCardViewModelBuilder(product)
                .setKitExpectQuantity(20)
                .setQuantity("20")
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();

        viewHolder.populate(firstViewModel);

        InventoryViewModel secondViewModel = new StockCardViewModelBuilder(product)
                .setKitExpectQuantity(200)
                .setQuantity("150")
                .setChecked(false)
                .setType("Embalagem")
                .setSOH(123L)
                .build();

        viewHolder.populate(secondViewModel);

        ExpireDateViewGroup mockedExpireDateView = mock(ExpireDateViewGroup.class);
        viewHolder.expireDateViewGroup = mockedExpireDateView;

        assertThat(viewHolder.etQuantity.getText().toString()).isEqualTo("150");
    }

    @Test
    public void shouldChangePopUIWhenQuantityChanged() throws Exception {
        InventoryViewModel viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("100")
                .setKitExpectQuantity(100l)
                .setChecked(true)
                .setType("Embalagem")
                .build();

        viewHolder.afterQuantityChanged(viewModel, "60");
        assertThat(viewHolder.tvStockOnHandInInventoryTip.getText().toString()).isEqualTo("Smaller quantity entered than expected. Please recheck it");

        viewHolder.afterQuantityChanged(viewModel, "100");
        assertThat(viewHolder.tvStockOnHandInInventoryTip.getText().toString()).contains("Quantity expected");

        viewHolder.afterQuantityChanged(viewModel, "120");
        assertThat(viewHolder.tvStockOnHandInInventoryTip.getText().toString()).isEqualTo("Larger quantity entered than expected. Please recheck it");
    }

    @Test
    public void shouldChangePopUIWhenPopulate() throws Exception {
        UnpackKitViewHolder spyViewHolder = spy(viewHolder);

        InventoryViewModel viewModel = new StockCardViewModelBuilder(product)
                .setQuantity("100")
                .setKitExpectQuantity(100l)
                .setChecked(true)
                .setType("Embalagem")
                .build();
        spyViewHolder.populate(viewModel);

        verify(spyViewHolder).updatePop(viewModel, "100");
    }
}